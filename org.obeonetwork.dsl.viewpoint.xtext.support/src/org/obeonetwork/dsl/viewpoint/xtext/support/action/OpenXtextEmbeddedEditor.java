/******************************************************************************
 * Copyright (c) 2011 Obeo  and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Obeo  - initial API and implementation
 ****************************************************************************/

package org.obeonetwork.dsl.viewpoint.xtext.support.action;

import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.obeonetwork.dsl.viewpoint.xtext.support.XtextEmbeddedEditor;

import com.google.inject.Injector;

public abstract class OpenXtextEmbeddedEditor implements IExternalJavaAction {

	public boolean canExecute(Collection<? extends EObject> arg0) {
		return true;
	}

	public void execute(Collection<? extends EObject> context,
			Map<String, Object> parameters) {
		DiagramEditPart diagramEditPart = ((DiagramEditor) getActiveEditor())
				.getDiagramEditPart();
		EObject semanticElement = null;
		for (EObject o : context)
			if (!(o instanceof DRepresentation)) {
				semanticElement = o;
				break;
			}
		for (EObject o : context) {
			EditPart editPart = diagramEditPart
					.findEditPart(diagramEditPart, o);
			if (editPart == null)
				editPart = findConnectionEditPart(diagramEditPart, o);
			if (editPart != null && (editPart instanceof IGraphicalEditPart)) {
				openEmbeddedEditor(semanticElement,(IGraphicalEditPart) editPart);
				break;
			}
		}
	}
	
	private ConnectionEditPart findConnectionEditPart(EditPart root, EObject obj) {
		if (root instanceof IGraphicalEditPart) {
			@SuppressWarnings("rawtypes")
			ListIterator connLi = ((IGraphicalEditPart)root).getSourceConnections().listIterator();
			while (connLi.hasNext()) {
				Object part = connLi.next();
				if (!(part instanceof ConnectionEditPart))
					continue;
				ConnectionEditPart cep = (ConnectionEditPart)part;
				View view = ((IAdaptable)part).getAdapter(View.class);
				if (view != null) {
					EObject semantic = ViewUtil.resolveSemanticElement(view);
					if (semantic != null && semantic.equals(obj))
						return cep;
				}
			}
		}
		@SuppressWarnings("rawtypes")
        ListIterator childrenLi = root.getChildren().listIterator();
        while (childrenLi.hasNext()) {
            EditPart epChild = (EditPart) childrenLi.next();

            ConnectionEditPart cep = findConnectionEditPart(epChild, obj);
            if (cep != null)
                return cep;
        }
        return null;
	}
	
	protected  void openEmbeddedEditor(EObject semanticElement, IGraphicalEditPart graphicalEditPart) {
			XtextEmbeddedEditor embeddedEditor = new XtextEmbeddedEditor(graphicalEditPart, getInjector());
			embeddedEditor.showEditor(semanticElement);	
	}
	
	/**
	 * Return the injector associated to you domain model plug-in. 
	 * @return
	 */
	protected abstract Injector getInjector();
	
	protected IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();
		return page.getActiveEditor();
	}

}
