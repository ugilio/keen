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

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sirius.diagram.description.DiagramDescription;
import org.eclipse.sirius.diagram.description.DiagramElementMapping;
import org.eclipse.sirius.diagram.ui.tools.api.layout.ordering.ViewOrdering;
import org.eclipse.sirius.diagram.ui.tools.api.layout.ordering.ViewOrderingProvider;
import org.eclipse.xtext.EcoreUtil2;

public class GridViewOrderingProvider implements ViewOrderingProvider {
	private ConcurrentHashMap<DiagramDescription, ViewOrdering> cache =
			new ConcurrentHashMap<>();
	private LayoutHelper helper = new LayoutHelper();
	
    public ViewOrdering getViewOrdering(DiagramElementMapping mapping) {
        if (supports(mapping)) {
        	DiagramDescription desc = getDiagramDescription(mapping);
        	ViewOrdering vo = cache.get(desc);
        	if (vo == null) {
        		vo = new GridViewOrdering();
        		cache.put(desc, vo);
        	}
        	return vo;
        }
        return null;
    }

    public boolean provides(DiagramElementMapping mapping) {
        return supports(mapping);
    }
    
    private DiagramDescription getDiagramDescription(DiagramElementMapping mapping) {
    	return EcoreUtil2.getContainerOfType(mapping, DiagramDescription.class);
    }
    
    private boolean supports(DiagramElementMapping mapping) {
    	DiagramDescription desc = getDiagramDescription(mapping);
    	return desc != null && helper.isSupportedDiagram(desc.getName());
    }
}
