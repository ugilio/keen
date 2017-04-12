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
package it.cnr.istc.keen.modeling.design.ext.contentassist;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DNodeContainerNameEditPart;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DNodeListNameEditPart;
import org.eclipse.sirius.diagram.ui.part.SiriusVisualIDRegistry;
import org.eclipse.sirius.viewpoint.DRepresentationElement;

@SuppressWarnings("restriction")
public class ContentAssistAwareEditPartProvider extends AbstractEditPartProvider {

	@Override
	protected Class<?> getNodeEditPartClass(View view ) {
		EObject element = view.getElement();
		if (element instanceof DRepresentationElement) {
			List<EObject> semantic = ((DRepresentationElement)element).getSemanticElements();
			for (EObject o : semantic)
				if (ContentAssistFactory.INSTANCE.hasContentAssistFor(o)) {
					switch (SiriusVisualIDRegistry.getVisualID(view)) {
						case DNodeContainerNameEditPart.VISUAL_ID:
							return ContentAssistAwareNodeContainerNameEditPart.class;
						case DNodeListNameEditPart.VISUAL_ID:
							return ContentAssistAwareNodeListNameEditPart.class;
						default:
							break;
					}
				}
		}
			
		return super.getNodeEditPartClass(view);
	}

}
