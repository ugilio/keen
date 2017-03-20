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
package it.cnr.istc.keen.formatting;

import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.util.Pair;

import it.cnr.istc.keen.services.DdlGrammarAccess;
import it.cnr.istc.keen.services.DdlGrammarAccess.ComponentElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.ConsumableResourceComponentTypeElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.DomainElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.NegNumberElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.PosNumberElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.ProblemElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.RenewableResourceComponentTypeElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.StateVariableComponentTypeElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.SynchronizationElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.TimelineElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.TimelineSynchronizationElements;
import it.cnr.istc.keen.services.DdlGrammarAccess.TransitionConstraintElements;

public class DdlFormatter extends AbstractDeclarativeFormatter {

	@Override
	protected void configureFormatting(FormattingConfig config) {
		configure((DdlGrammarAccess) getGrammarAccess(), config);
	}

	protected void configure(DdlGrammarAccess ga, FormattingConfig c) {
		configureGeneral(ga,c);
		
		configure(ga,ga.getStateVariableComponentTypeAccess(),c);
		configure(ga,ga.getTransitionConstraintAccess(),c);
		configure(ga,ga.getRenewableResourceComponentTypeAccess(),c);
		configure(ga,ga.getConsumableResourceComponentTypeAccess(),c);
		configure(ga,ga.getComponentAccess(),c);
		configure(ga,ga.getTimelineAccess(),c);
		configure(ga,ga.getPosNumberAccess(),c);
		configure(ga,ga.getNegNumberAccess(),c);
	}
	
	protected void configureGeneral(DdlGrammarAccess ga, FormattingConfig c) {
		c.setAutoLinewrap(1000);
		
		//Preserve lines around comments
		c.setLinewrap(0, 1, 2).before(ga.getSL_COMMENTRule());
		c.setLinewrap(0, 1, 2).before(ga.getML_COMMENTRule());
		c.setLinewrap(0, 1, 2).after(ga.getML_COMMENTRule());
		
		//Indent contents of {...}, { on same line
		for(Pair<Keyword, Keyword> p : ga.findKeywordPairs("{", "}")) {
            c.setLinewrap().after(p.getFirst());
            c.setIndentationIncrement().after(p.getFirst());
            c.setIndentationDecrement().before(p.getSecond());
            c.setLinewrap().around(p.getSecond());
        }
		
		//No space around ( )
		for(Pair<Keyword, Keyword> p : ga.findKeywordPairs("(", ")")) {
            c.setNoSpace().around(p.getFirst());
            c.setNoSpace().before(p.getSecond());
        }
		
		//No space around [ ]
		for(Pair<Keyword, Keyword> p : ga.findKeywordPairs("[", "]")) {
            c.setNoSpace().after(p.getFirst());
            c.setNoSpace().before(p.getSecond());
        }
		
		//No space around < >
		for(Pair<Keyword, Keyword> p : ga.findKeywordPairs("<", ">")) {
            c.setNoSpace().after(p.getFirst());
            c.setNoSpace().before(p.getSecond());
        }
		
		//Comma: no space before, one space after
		for (Keyword e: ga.findKeywords(",")) {
			c.setNoSpace().before(e);
			c.setSpace(" ").after(e);
		}
		
		//No space before semicolon, new line after
		for (Keyword e: ga.findKeywords(";")) {
			c.setNoSpace().before(e);
			c.setLinewrap().after(e);
		}
		
		//No space around dots
		for (Keyword e: ga.findKeywords(".")) {
			c.setNoSpace().before(e);
			c.setNoSpace().after(e);
		}
		
		//one space around comparison operators?
		
		configureNewlinesBetweenGroups(ga, c);
	}
	
	protected void configureNewlinesBetweenGroups(DdlGrammarAccess ga, FormattingConfig c) {
		//Empty line between temporal module and other elements
		DomainElements dom = ga.getDomainAccess();
		c.setLinewrap(2).between(dom.getModuleAssignment_3(), dom.getElementsAssignment_4());
		//Empty line between different domain elements
		c.setLinewrap(2).between(dom.getElementsAssignment_4(),dom.getElementsAssignment_4());
		//Empty line between transition constraints
		StateVariableComponentTypeElements sv = ga.getStateVariableComponentTypeAccess();
		c.setLinewrap(2).between(sv.getTransConstraintAssignment_7(),sv.getTransConstraintAssignment_7());
		//Empty line between synchronizations
		TimelineSynchronizationElements ts = ga.getTimelineSynchronizationAccess();
		c.setLinewrap(2).between(ts.getSynchronizationsAssignment_3(),ts.getSynchronizationsAssignment_3());

		//Preserve new lines between synchronization elements
		SynchronizationElements synch = ga.getSynchronizationAccess();
		c.setLinewrap(1,1,2).between(synch.getElementsAssignment_3(),synch.getElementsAssignment_3());
//		SynchronizationElementElements se = ga.getSynchronizationElementAccess();
//		c.setLinewrap(1).range(se.getInstantiatedComponentDecisionParserRuleCall_0_0(), se.getInstantiatedComponentDecisionParserRuleCall_0_0());
		//Preserve new lines between problem elements
		ProblemElements pe = ga.getProblemAccess();
		c.setLinewrap(1,1,2).between(pe.getElementsAssignment_4(),pe.getElementsAssignment_4());
	}
	
	protected void configure(DdlGrammarAccess ga, StateVariableComponentTypeElements e, FormattingConfig c) {
		c.setLinewrap().before(e.getCOMP_TYPEKeyword_0());
		c.setLinewrap().after(e.getLeftParenthesisKeyword_3());
		c.setIndentationIncrement().after(e.getLeftParenthesisKeyword_3());
		c.setLinewrap().after(e.getCommaKeyword_4_1_0());
		c.setIndentationDecrement().after(e.getRightParenthesisKeyword_5());
		c.setLinewrap().after(e.getRightParenthesisKeyword_5());
	}

	protected void configure(DdlGrammarAccess ga, TransitionConstraintElements e, FormattingConfig c) {
		c.setLinewrap().before(e.getMEETSKeyword_3());
	}

	protected void configure(DdlGrammarAccess ga, RenewableResourceComponentTypeElements e, FormattingConfig c) {
		c.setLinewrap().before(e.getCOMP_TYPEKeyword_0());
		c.setLinewrap().after(e.getRightParenthesisKeyword_5());
	}

	protected void configure(DdlGrammarAccess ga, ConsumableResourceComponentTypeElements e, FormattingConfig c) {
		c.setLinewrap().before(e.getCOMP_TYPEKeyword_0());
		c.setLinewrap().after(e.getRightParenthesisKeyword_7());
	}

	protected void configure(DdlGrammarAccess ga, ComponentElements e, FormattingConfig c) {
		c.setNoSpace().after(e.getLeftCurlyBracketKeyword_2());
		c.setNoSpace().before(e.getRightCurlyBracketKeyword_4());
		c.setNoSpace().before(e.getColonKeyword_5());
	}

	protected void configure(DdlGrammarAccess ga, TimelineElements e, FormattingConfig c) {
		c.setNoSpace().around(e.getRightParenthesisKeyword_4());
	}

	protected void configure(DdlGrammarAccess ga, PosNumberElements e, FormattingConfig c) {
		c.setNoSpace().after(e.getPlusSignKeyword_0());
	}

	protected void configure(DdlGrammarAccess ga, NegNumberElements e, FormattingConfig c) {
		c.setNoSpace().after(e.getHyphenMinusKeyword_0());
	}
}
