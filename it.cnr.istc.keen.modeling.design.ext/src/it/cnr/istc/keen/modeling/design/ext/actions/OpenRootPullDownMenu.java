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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.diagram.description.DiagramDescription;
import org.eclipse.sirius.ui.tools.internal.views.common.navigator.SiriusCommonLabelProvider;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

import it.cnr.istc.keen.modeling.design.ext.actions.SelectionHelper.ResourceContext;
import it.cnr.istc.keen.modeling.design.ext.services.HyperlinkUtils;
import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

@SuppressWarnings("restriction")
public class OpenRootPullDownMenu extends CompoundContributionItem {

	private SelectionHelper selectionHelper = new SelectionHelper();
	
	private ILabelProvider labelProvider = new SiriusCommonLabelProvider();
	
	public OpenRootPullDownMenu() {
		super();
	}

	public OpenRootPullDownMenu(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		ArrayList<IContributionItem> additions = new ArrayList<>();
		updateMenu(getResourceContext(),additions);
		return additions.toArray(new IContributionItem[additions.size()]);
	}
	
	private Session getSession(ResourceContext rc) {
		SessionManager sm = SessionManager.INSTANCE;
		URI uri = VirtualResource.getVirtualResourceURI(rc.theURI);
		if (uri != null) {
			for (Session s : sm.getSessions())
				for (Resource r : s.getSemanticResources())
					if (uri.equals(r.getURI()))
						return s;
		}
		return null;
	}
	
	private List<DSemanticDiagram> getRepresentations(Session session) {
		List<DSemanticDiagram> diagrams = new ArrayList<DSemanticDiagram>();
		if (session == null)
			return diagrams;
		Resource aird = session.getSessionResource();
		for (EObject o : aird.getContents())
			if (o instanceof DRepresentation)
				diagrams.add((DSemanticDiagram)o);
		return diagrams;
	}
	
	
	private void updateMenu(ResourceContext rc, List<IContributionItem> additions) {
		Session session = getSession(rc);
		List<DSemanticDiagram> diagrams = getRepresentations(session);
		for (DSemanticDiagram diag : diagrams) {
			String text = labelProvider.getText(diag);
			Image img = labelProvider.getImage(diag);
			ActionContributionItem item = new ActionContributionItem(
					new OpenDiagramAction(text,ImageDescriptor.createFromImage(img),diag){
			});
			additions.add(item);
		}
		if (diagrams.isEmpty())
			additions.add(new ActionContributionItem(new OpenDiagramAction()));
	}
	
	private ResourceContext getResourceContext() {
		IWorkbench wb = PlatformUI.getWorkbench();
		ResourceContext rc[] = new ResourceContext[1];
		wb.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				ISelection sel = wb.getActiveWorkbenchWindow().getActivePage().getSelection();
				if (sel instanceof IStructuredSelection && !sel.isEmpty())
					rc[0]=selectionHelper.getResourceContextFromSelection((IStructuredSelection)sel);
				if (rc[0]==null)
				{
					IEditorPart part = wb.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if (part!=null)
						rc[0]=selectionHelper.getResourceContextFromEditor(part.getEditorInput());
				}
			}
		});
		return rc[0];
	}
	
	private class OpenDiagramAction extends Action {
		
		private DSemanticDiagram diagram;
		
		public OpenDiagramAction() {
			super("No diagrams to display");
			setEnabled(false);
		}
		
		public OpenDiagramAction(String text, ImageDescriptor image, DSemanticDiagram diagram) {
			super(text, image);
			this.diagram = diagram;
		}
		
		@Override
		public void run() {
			EObject root = diagram.getTarget();
			DiagramDescription desc = diagram.getDescription();
			Session session = SessionManager.INSTANCE.getSession(root);
			HyperlinkUtils.openTargetDiagram(root, desc, session, false);
			
		}
		
	}
}
