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
package it.cnr.istc.keen.ui.syntaxcoloring

import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultSemanticHighlightingCalculator
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.util.CancelIndicator
import it.cnr.istc.keen.ddl.EnumLiteral
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import it.cnr.istc.keen.ddl.RValue
import it.cnr.istc.keen.ddl.DdlPackage
import it.cnr.istc.keen.ddl.Timeline
import it.cnr.istc.keen.ddl.ParType
import it.cnr.istc.keen.ddl.ComponentDecisionType
import org.eclipse.emf.ecore.EStructuralFeature

class DdlSemanticHighlightingCalculator extends DefaultSemanticHighlightingCalculator {
	
	override highlightElement(EObject object, IHighlightedPositionAcceptor acceptor,
			CancelIndicator cancelIndicator) {
		return doHighlight(object,acceptor);
	}
	
	protected dispatch def doHighlight(EnumLiteral object, IHighlightedPositionAcceptor acceptor) {
		val node = NodeModelUtils.findActualNodeFor(object);
		highlightNode(acceptor,node,DdlHighlightingConfiguration.ENUM_LITERAL);
		return true;
	}
	
	//Highlight enum references
	protected dispatch def doHighlight(RValue object, IHighlightedPositionAcceptor acceptor) {
		if (object.enumeration!=null)
		{
			highlightFeature(acceptor,object,DdlPackage.Literals.RVALUE__ENUMERATION,
				DdlHighlightingConfiguration.ENUM_LITERAL);
		}
		return true;
	}
	
	//Highlight parameter types
	protected dispatch def doHighlight(ParType object, IHighlightedPositionAcceptor acceptor) {
		if (object.name!=null)
		{
			highlightFeature(acceptor,object,DdlPackage.Literals.PAR_TYPE__NAME,
				DdlHighlightingConfiguration.PARAMETER_TYPE);
		}
		return true;
	}
	
	//Highlight parameter type references
	protected dispatch def doHighlight(ComponentDecisionType object, IHighlightedPositionAcceptor acceptor) {
		if (!object.args.isEmpty())
		{
			highlightAllChildren(acceptor,object,DdlPackage.Literals.COMPONENT_DECISION_TYPE__ARGS,
				DdlHighlightingConfiguration.PARAMETER_TYPE);
		}
		return true;
	}
	
	//Highlight timeline parameters
	protected dispatch def doHighlight(Timeline object, IHighlightedPositionAcceptor acceptor) {
		if (!object.params.isEmpty())
		{
			highlightAllChildren(acceptor,object,DdlPackage.Literals.TIMELINE__PARAMS,
				DdlHighlightingConfiguration.TIMELINE_PARAMS);
		}
		return true;
	}
	
	
	
	protected dispatch def doHighlight(EObject object, IHighlightedPositionAcceptor acceptor) {
		return false;
	}


	private def highlightAllChildren(IHighlightedPositionAcceptor acceptor, EObject object, EStructuralFeature feature,
			String... styleIds) {
		val children = NodeModelUtils.findNodesForFeature(object, feature);
		for (c : children)
			highlightNode(acceptor, c, styleIds);
	}

	
	
}