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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.elk.alg.mrtree.properties.MrTreeOptions;
import org.eclipse.elk.conn.gmf.GmfDiagramLayoutConnector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.FixedLayouterOptions;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.util.ObjectAdapter;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.layout.LayoutType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.ui.tools.api.figure.SiriusWrapLabel;
import org.eclipse.sirius.diagram.ui.tools.api.layout.provider.DefaultLayoutProvider;
import org.eclipse.ui.IWorkbenchPart;

public class ElkLayoutConnector extends GmfDiagramLayoutConnector {
	
	private LayoutHelper helper = new LayoutHelper();
	
	protected ElkLabel createNodeLabel(final LayoutMapping mapping,
			final IGraphicalEditPart labelEditPart,
            final IGraphicalEditPart nodeEditPart, final ElkNode elknode) {
		if (labelEditPart.getFigure() instanceof SiriusWrapLabel) {
			View v = nodeEditPart.getPrimaryView();
			//apply only to top-level nodes
			if (v.eContainer() == v.getDiagram()) {
				SiriusWrapLabel lab = (SiriusWrapLabel)labelEditPart.getFigure();
				Border border = lab.getBorder();
				if (border == null) {
					lab.setBorder(new MarginBorder(0, 10, 0, 10));
					lab.setMinimumSize(null);
					lab.setPreferredSize(null);
				}
			}
		}
		return super.createNodeLabel(mapping, labelEditPart, nodeEditPart, elknode);
	}
	
    public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
    	LayoutMapping mapping = super.buildLayoutGraph(workbenchPart, diagramPart);
    	DDiagram diagram = helper.getDDiagram(workbenchPart);
    	if (diagram == null)
    		diagram = helper.getDDiagram(diagramPart);
    	String diagramName = "";
    	if (diagram != null)
    		diagramName = diagram.getDescription().getName();
    	if (helper.useTreeLayout(diagramName)) {
    		mapping.getLayoutGraph()
    		.setProperty(CoreOptions.ALGORITHM, MrTreeOptions.ALGORITHM_ID)
    		.setProperty(CoreOptions.SPACING_NODE_NODE, 50.0d)
    		.setProperty(CoreOptions.DIRECTION, Direction.DOWN);
    	}
    	else { //Fallback to Sirius-defined layouts
    		mapping.getLayoutGraph().
    		setProperty(CoreOptions.ALGORITHM, FixedLayouterOptions.ALGORITHM_ID);
    	}
		
    	return mapping;
    }
    
    private boolean doApplyLayout(final LayoutMapping mapping) {
    	DiagramEditPart part = helper.getDiagramEditPart(mapping.getWorkbenchPart());
    	if (part == null) {
    		part = helper.getDiagramEditPart(mapping.getGraphMap().values());
    	}
    	DDiagram diag = helper.getDDiagram(part);
    	if (diag != null && !helper.useTreeLayout(diag.getDescription().getName())) {
    		List<Object> hints = new ArrayList<>(2);
    		hints.add(LayoutType.DEFAULT);
    		hints.add(part);
    		IAdaptable layoutHints = new ObjectAdapter(hints);
    		
    		DefaultLayoutProvider siriusProvider = new DefaultLayoutProvider();

//    		List<?> selectedParts = part == null ? Collections.emptyList() : part.getViewer().getSelectedEditParts(); 
//    		Command cmd = (selectedParts.isEmpty()) ?
//    				siriusProvider.layoutEditParts(part, layoutHints) :
//    				siriusProvider.layoutEditParts(selectedParts, layoutHints);
    		Command cmd = siriusProvider.layoutEditParts(part, layoutHints);
    		if (cmd != null && cmd.canExecute())
    			cmd.execute();
    		return true;
    	}
    	return false;
    }
    
    protected void applyLayout(final LayoutMapping mapping) {
    	if (!doApplyLayout(mapping))
    		super.applyLayout(mapping);
    }
}
