/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 */
package it.cnr.istc.keen.modeling.design.ext.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.business.api.session.DefaultLocalSessionCreationOperation;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionCreationOperation;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.business.api.session.ViewpointSelector;
import org.eclipse.sirius.tools.api.command.semantic.AddSemanticResourceCommand;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import it.cnr.istc.keen.ddl.Ddl;
import it.cnr.istc.keen.modeling.design.ext.services.HyperlinkUtils;

public class RootDiagramOpener {

	private static final String DEFAULT_REPRESENTATIONS_FILE = "representations.aird";
	private static final String VIEWPOINT_NAME = "ddl";
	
	private URI modelURI = null;
	
	private Session createSession(IProject project, IProgressMonitor monitor) {
		URI uri = URI.createPlatformResourceURI("/" + project.getName() + "/" + DEFAULT_REPRESENTATIONS_FILE, false);
		SessionCreationOperation op = new DefaultLocalSessionCreationOperation(uri, monitor);
		try {
			op.execute();
		} catch (CoreException e) {
			throw new TmpException(e);
		}
		return op.getCreatedSession();
	}

	private Session findSessionForSemanticResource(URI uri) {
		SessionManager sm = SessionManager.INSTANCE;
		for (Session s : sm.getSessions())
			for (Resource r : s.getSemanticResources())
				if (uri.equals(r.getURI()))
					return s;
		return null;
	}

	private Session getSession(IProject project, URI uri, IProgressMonitor monitor) {
		SubMonitor sm = SubMonitor.convert(monitor, "Opening session", 100);
		Session session = findSessionForSemanticResource(uri);
		if (session != null)
			return session;
		session = findSessionForSemanticResource(modelURI);
		if (session == null) {
			Collection<Session> sessions = SessionManager.INSTANCE.getSessions();
			if (sessions.isEmpty())
				session = createSession(project, sm.split(30));
			else {
				sm.setWorkRemaining(70);
				for (Session s : sessions) {
					session = s;
					break;
				}
			}
			assert session != null;
			Command cmd = new AddSemanticResourceCommand(session, modelURI, sm.split(20));
			session.getTransactionalEditingDomain().getCommandStack().execute(cmd);
		}
		sm.setWorkRemaining(50);
		Command cmd = new AddSemanticResourceCommand(session, uri, sm.split(20));
		session.getTransactionalEditingDomain().getCommandStack().execute(cmd);
		session.save(sm.split(30));
		sm.done();
		return session;
	}

	private Resource findResourceFromSession(Session session, URI uri) {
		for (Resource r : session.getSemanticResources())
			if (uri.equals(r.getURI()))
				return r;
		return null;
	}

	private Viewpoint getViewpoint(Session session, IProgressMonitor monitor) {
		SubMonitor sm = SubMonitor.convert(monitor, "Applying viewpoint", 3);
		// Try in session first
		for (Viewpoint v : session.getSelectedViewpoints(false))
			if (VIEWPOINT_NAME.equals(v.getName()))
				return v;
		// Nope, add it
		Viewpoint vp = null;
		for (Viewpoint v : ViewpointRegistry.getInstance().getViewpoints())
			if (VIEWPOINT_NAME.equals(v.getName())) {
				vp = v;
				break;
			}
		if (vp != null) {
			final Viewpoint viewpoint = vp;
			TransactionalEditingDomain ted = session.getTransactionalEditingDomain();
			ted.getCommandStack().execute(new RecordingCommand(ted, "Select viewpoint") {
				@Override
				protected void doExecute() {
					new ViewpointSelector(session).selectViewpoint(viewpoint, false, sm.split(1));
				}
			});
			session.save(sm.split(2));

			// Get it from session again
			for (Viewpoint v : session.getSelectedViewpoints(false))
				if (VIEWPOINT_NAME.equals(v.getName()))
					return v;
		}
		sm.done();
		return null;
	}

	private Ddl getDdl(Resource res) {
		if (res.getContents().isEmpty())
			return null;
		EObject root = res.getContents().get(0);
		if (!(root instanceof Ddl))
			return null;
		return (Ddl) root;
	}
	
	public RootDiagramOpener configure(URI modelURI) {
		this.modelURI = modelURI;
		return this;
	}

	public void openRootDiagram(IProject project, URI uri) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		Session[] result = new Session[1];
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					result[0] = prepareSessionAndViewpoint(project, uri, monitor);
				}

			});
		} catch (InvocationTargetException | InterruptedException e) {
			throw new TmpException(e);
		}
		Session session = result[0];
		if (session != null) {
			EObject obj = getDdl(findResourceFromSession(session, uri));
			if (obj == null)
				return;
			
			HyperlinkUtils.openHyperLink(obj, (EObject)session, obj);
		}
	}
	
	protected Session prepareSessionAndViewpoint(IProject project, URI uri, IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		SubMonitor sm = SubMonitor.convert(monitor, "Opening root diagram", 60);
		Session session = getSession(project, uri, sm.split(50));
		if (session == null)
			throw new TmpException("Cannot create Sirius session");
		Resource res = findResourceFromSession(session, uri);
		if (res == null)
			throw new TmpException("Cannot find semantic resource in Sirius session");

		Viewpoint vp = getViewpoint(session, sm.split(10));
		if (vp == null)
			throw new TmpException("Cannot select the needed viewpoint");
		sm.done();
		
		return session;
	}	
	
	
	//FIXME: Implement a proper exception
	public class TmpException extends RuntimeException {
		private static final long serialVersionUID = -7298139543495436061L;

		public TmpException(String msg) {
			super(msg);
		}

		public TmpException(Throwable t) {
			super(t);
		}

	}
}