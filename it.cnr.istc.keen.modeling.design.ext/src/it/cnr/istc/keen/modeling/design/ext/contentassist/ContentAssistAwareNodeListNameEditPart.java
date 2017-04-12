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

import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DNodeListNameEditPart;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.SiriusEditPartFactory;

@SuppressWarnings("restriction")
public class ContentAssistAwareNodeListNameEditPart extends DNodeListNameEditPart {

	public ContentAssistAwareNodeListNameEditPart(View view) {
		super(view);
	}
	
	@Override
    protected DirectEditManager getManager() {
        if (manager == null) {
            setManager(new ContentAssistAwareDirectEditManager(this, SiriusEditPartFactory.getTextCellEditorClass(this), SiriusEditPartFactory.getTextCellEditorLocator(this)));
        }
        return manager;
	}
}
