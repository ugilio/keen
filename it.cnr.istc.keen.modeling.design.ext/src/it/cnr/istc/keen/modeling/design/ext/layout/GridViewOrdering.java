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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.ui.tools.api.layout.ordering.AbstractViewOrdering;
import org.eclipse.sirius.diagram.ui.tools.api.layout.ordering.GridView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class GridViewOrdering extends AbstractViewOrdering {

	@Override
	public boolean isAbleToManageView(View view) {
		return true;
	}

	@Override
	protected List<View> sortViews(List<View> views) {
		return views;
	}
	
	private View[][] calcSquareGrid(List<View> views) {
		int tot = views.size();
		double width = Math.ceil(Math.sqrt(tot));
		double height = Math.ceil(tot/width);
		int w = (int)width;
		int h = (int)height;
		View[][] grid = new View[w][h];
		return grid;
	}
	
	private static DiagramEditor getCurrentDiagramEditor() {
		IWorkbench wb = PlatformUI.getWorkbench();
		DiagramEditor[] result = new DiagramEditor[1];
		wb.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
		    	IEditorPart part = wb.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    	if (part instanceof DiagramEditor)
		    		result[0]=(DiagramEditor)part;
			}
		});
    	return result[0];
	}
	
	private Rectangle getDiagramBounds(DiagramEditor e) {
		if ( e != null) {
			EditPart ep = e.getDiagramGraphicalViewer().getContents();
			ep = ep.getRoot();
			if (ep instanceof GraphicalEditPart)
				return ((GraphicalEditPart)ep).getFigure().getBounds();
		}
		return null;
	}
	
	private GraphicalEditPart getEditPart(DiagramEditor e, View view) {
		if (e==null)
			return null;
		@SuppressWarnings("unchecked")
		List<EditPart> parts = e.getDiagramGraphicalViewer().getContents().getChildren();
		for (EditPart p : parts)
			if (p.getModel() == view && p instanceof GraphicalEditPart)
				return (GraphicalEditPart)p;
		return null;
	}
	
	private boolean exceeds(List<Integer> sizes, int maxSize) {
		if (sizes.size()<=1)
			return false;
		Optional<Integer> optSum = sizes.stream().reduce( (i1,i2) -> i1+i2);
		int sum = optSum.isPresent() ? optSum.get() : 0;
		sum+=Math.max(0,sizes.size()-1)*LayoutHelper.PADDING;
		return (sum > maxSize);
	}
	
	private int internalCalcBestFitGrid(DiagramEditor e, List<View> views, int width, int maxCols) {
		int padding = LayoutHelper.PADDING;
		int firstPadding = LayoutHelper.MINOR_PADDING; 
		List<Integer> colSizes = new ArrayList<Integer>();
		int x = firstPadding; 
		int idx = 0;
		boolean firstLine = true;
		if (maxCols <= 0)
			maxCols = Integer.MAX_VALUE;
		for (View v : views) {
			GraphicalEditPart ep = getEditPart(e, v);
			int w = ep != null ? ep.getFigure().getBounds().width : 10;
			if (!firstLine && idx<colSizes.size())
				w = Math.max(w, colSizes.get(idx));
			if ((x+w<=width || idx==0) && firstLine && idx<maxCols) {
				colSizes.add(w);
			}
			else if (idx>=colSizes.size() || x+w>width) { //next row
				firstLine = false;
				idx = 0;
				x = firstPadding;
			}
			int colW = colSizes.get(idx);
			if (colW<w) {
				colSizes.set(idx, w);
				if (exceeds(colSizes, width))
					return internalCalcBestFitGrid(e, views, width, colSizes.size()-1);
				colW=w;
			}
			idx++;
			x+=colW+padding;
		}
		return colSizes.size();
	}
		
	
	private View[][] calcBestFitGrid(List<View> views) {
		DiagramEditor e = getCurrentDiagramEditor();
		Rectangle bounds = getDiagramBounds(e);
		if (bounds == null)
			return calcSquareGrid(views);

		double tot = views.size();
		int w = internalCalcBestFitGrid(e, views, bounds.width, -1);
		int h = (int)Math.ceil(tot/w);
		View[][] grid = new View[w][h];
		return grid;
	}
	
	private View[][] calcBestGrid(List<View> views) {
		return calcBestFitGrid(views);
	}
	
	private void insertIntoGrid(View[][] grid, List<View> views) {
		int w = grid.length;
		int h = w>0? grid[0].length : 0;
		Iterator<View> it = views.iterator();
		out: for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				if (!it.hasNext())
					break out;
				grid[i][j]=it.next();
			}
		}
	}
	
    public GridView getSortedViewsAsGrid() {
        final List<View> views = this.getSortedViews();
        final View[][] grid = calcBestGrid(views);
        insertIntoGrid(grid, views);
        final GridView gridView = GridView.create(grid);
        return gridView;
    }
}
