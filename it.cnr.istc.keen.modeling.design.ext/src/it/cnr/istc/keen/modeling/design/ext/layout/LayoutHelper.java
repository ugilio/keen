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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;

public class LayoutHelper {

    public static final int PADDING = 50;
    public static final int MINOR_PADDING = 10;

    public static final String DIAGNAME_ROOT = "rootModel";
    public static final String DIAGNAME_DOMAINMODEL = "DomainModel";
    public static final String DIAGNAME_TYPECONSTRAINTS = "TypeConstraints";
    public static final String DIAGNAME_COMPDIAGRAM = "componentDiagram";
    public static final String DIAGNAME_SYNCDIAGRAM = "synchronizationDiagram";

    private HashSet<String> diagramNames = new HashSet<>(Arrays.asList(
    		DIAGNAME_ROOT, DIAGNAME_DOMAINMODEL, DIAGNAME_TYPECONSTRAINTS,
    		DIAGNAME_COMPDIAGRAM, DIAGNAME_SYNCDIAGRAM));
	
    private HashSet<String> gridDiagrams = new HashSet<>(Arrays.asList(
    		DIAGNAME_DOMAINMODEL, DIAGNAME_COMPDIAGRAM));
    
    private HashSet<String> treeDiagrams = new HashSet<>(Arrays.asList(
    		DIAGNAME_TYPECONSTRAINTS, DIAGNAME_SYNCDIAGRAM));
    
    public boolean useGridLayout(String diagName) {
    	return gridDiagrams.contains(diagName);
    }
    
    public boolean useTreeLayout(String diagName) {
    	return treeDiagrams.contains(diagName);
    }
    
    public boolean isSupportedDiagram(String diagName) {
    	return diagramNames.contains(diagName);
    }
    
	protected DDiagram getDDiagramSingle(Object object) {
		if (object instanceof DiagramEditor) {
			EObject tmp = ((DiagramEditor)object).getDiagram().getElement();
			if (tmp instanceof DDiagram) {
				return (DDiagram)tmp;
			}
		}
		else if (object instanceof IGraphicalEditPart) {
			Object tmp = ((IGraphicalEditPart)object).getModel();
			if (tmp instanceof Node)
				tmp = ((Node)tmp).getDiagram();
			if (tmp instanceof Diagram)
				tmp = ((Diagram)tmp).getElement();
			if (tmp instanceof DDiagram) {
				return (DDiagram)tmp;
			}
		}
		else if (object instanceof DDiagramElement) {
			DDiagram diag = ((DDiagramElement)object).getParentDiagram();
			return diag;
		}
		return null;
	}
	
	public DDiagram getDDiagram(Object object) {
	    if (object instanceof Collection) {
	        for (Object o : (Collection<?>) object) {
	        	DDiagram diag = getDDiagramSingle(o);
	            if (diag != null)
	            	return diag;
	        }
	        return null;
	    }

	    return getDDiagramSingle(object);
	}
	
	protected DiagramEditPart getDiagramEditPartSingle(Object object) {
		if (object instanceof DiagramEditor) {
			return ((DiagramEditor)object).getDiagramEditPart();
		}
		else if (object instanceof EditPart) {
			while (object != null && object instanceof EditPart) {
				object = ((EditPart)object).getParent();
				if (object instanceof DiagramEditPart)
					return (DiagramEditPart)object;
			}
		}
		return null;
	}	
	public DiagramEditPart getDiagramEditPart(Object object) {
	    if (object instanceof Collection) {
	        for (Object o : (Collection<?>) object) {
	        	DiagramEditPart ep = getDiagramEditPartSingle(o);
	            if (ep != null)
	            	return ep;
	        }
	        return null;
	    }

	    return getDiagramEditPartSingle(object);
	}	
}
