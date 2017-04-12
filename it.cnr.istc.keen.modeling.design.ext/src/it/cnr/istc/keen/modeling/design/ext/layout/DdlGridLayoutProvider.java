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
package it.cnr.istc.keen.modeling.design.ext.layout;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.layout.AbstractLayoutEditPartProvider;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.ui.edit.api.part.AbstractDDiagramEditPart;
import org.eclipse.sirius.diagram.ui.tools.api.layout.provider.GridLayoutProvider;
import org.eclipse.sirius.diagram.ui.tools.api.layout.provider.LayoutProvider;

public class DdlGridLayoutProvider implements LayoutProvider {

    private GridLayoutProvider gridLayoutProvider;
    private LayoutHelper helper = new LayoutHelper();
    
    public AbstractLayoutEditPartProvider getLayoutNodeProvider(IGraphicalEditPart container) {
    	if (provides(container)) {
    		gridLayoutProvider = null;
    		if (gridLayoutProvider == null) {
    			gridLayoutProvider = new GridLayoutProvider();
    			gridLayoutProvider.setColumnSizeMode(GridLayoutProvider.DIMENSION_BY_LINE_OR_COLUMN);
    			gridLayoutProvider.setLineSizeMode(GridLayoutProvider.DIMENSION_BY_LINE_OR_COLUMN);
    			//            layoutProvider.setColumnSizeMode(GridLayoutProvider.FREE_DIMENSION);
    			//            layoutProvider.setLineSizeMode(GridLayoutProvider.FREE_DIMENSION);
    			gridLayoutProvider.getPadding().top = LayoutHelper.MINOR_PADDING;
    			gridLayoutProvider.getPadding().bottom = LayoutHelper.PADDING;
    			gridLayoutProvider.getPadding().left = LayoutHelper.MINOR_PADDING;
    			gridLayoutProvider.getPadding().right = LayoutHelper.PADDING;
    		}
    		return gridLayoutProvider;
    	}
    	return null;
    }

    public boolean provides(IGraphicalEditPart container) {
    	return (helper.useGridLayout(getDiagramName(container)));
    }

    private String getDiagramName(IGraphicalEditPart container) {
        if (container instanceof AbstractDDiagramEditPart) {
            EObject obj = container.resolveSemanticElement();
            if (obj instanceof DDiagram) {
                DDiagram diagram = (DDiagram) obj;
                return diagram.getDescription().getName();
            }
        }
        return "";
    }

    public boolean isDiagramLayoutProvider() {
        return true;
    }

}
