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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditorInput;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sirius.viewpoint.DRepresentationElement;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.util.Pair;

import com.google.inject.Inject;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;
import it.cnr.istc.keen.ui.internal.KeenActivator;

public class SelectionHelper {
	
	@Inject
	private IStorage2UriMapper mapper;
	
	protected class ResourceContext {
		URI theURI;
		IProject theProject;
	}
	
	public SelectionHelper() {
		KeenActivator.getInstance().getInjector(KeenActivator.IT_CNR_ISTC_KEEN_DDL).injectMembers(this);
	}
	
	protected ResourceContext getResourceContext(IFile file) {
		ResourceContext rc = new ResourceContext();
		rc.theURI = mapper.getUri(file);
		rc.theProject = file.getProject();
		if (rc.theURI != null && rc.theProject != null)
			return rc;
		return null;
	}
	
	protected ResourceContext getResourceContext(URI uri) {
		ResourceContext rc = new ResourceContext();
		uri = VirtualResource.getRealResourceURI(uri);
		rc.theURI = uri;
		for (Pair<IStorage, IProject> o : mapper.getStorages(uri))
			if (o.getSecond() != null) {
				rc.theProject = o.getSecond();
				return rc;
			}
		return null;
	}
	
	protected ResourceContext getResourceContextFromEvent(ExecutionEvent event) {
		IStructuredSelection sSel = HandlerUtil.getCurrentStructuredSelection(event);
		ResourceContext rc = getResourceContextFromSelection(sSel);
		if (rc != null)
			return rc;
		IEditorInput ei = HandlerUtil.getActiveEditorInput(event);
		return getResourceContextFromEditor(ei);
	}

	protected ResourceContext getResourceContextFromSelection(IStructuredSelection sSel) {
		if (sSel != null && !sSel.isEmpty()) {
			Object obj = sSel.getFirstElement();
			if (obj instanceof IFile)
				return getResourceContext((IFile)obj);
			if (obj instanceof EObject)
				obj = ((EObject)obj).eResource();
			if (obj instanceof Resource)
				return getResourceContext(((Resource)obj).getURI());
			if (obj instanceof IGraphicalEditPart)
				obj = ((IGraphicalEditPart)obj).getModel();
			if (obj instanceof View)
				obj = ((View)obj).getElement();
			if (obj instanceof DSemanticDecorator) {
				EObject target = ((DSemanticDecorator)obj).getTarget();
				if (target != null && target.eResource() != null)
					return getResourceContext(target.eResource().getURI());
			}
		}
		return null;
	}
	
	protected ResourceContext getResourceContextFromEditor(IEditorInput ei) {
		if (ei != null) {
			if (ei instanceof IFileEditorInput)
				return getResourceContext(((IFileEditorInput)ei).getFile());
			if (ei instanceof IDiagramEditorInput) {
				EObject obj = ((IDiagramEditorInput)ei).getDiagram().getElement();
				if (obj instanceof DRepresentationElement)
					return getResourceContext(((DRepresentationElement)obj).getTarget().eResource().getURI());
			}
		}
		return null;
	}
}
