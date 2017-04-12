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
package it.cnr.istc.keen.modeling.design.ext.services;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;

import it.cnr.istc.keen.ddl.ComponentDecision;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.keen.ddl.TransitionConstraint;
import it.cnr.istc.keen.modeling.design.ext.services.ParamUtils.NameCollector;

public class TransitionConstraintUtils {
    private static NameCollector<TransitionConstraint> tcNameCollector = new NameCollector<TransitionConstraint>() {
		@Override
		public HashSet<String> getNames(TransitionConstraint tc) {
	    	HashSet<String> usedNames = new LinkedHashSet<>();
	    	ComponentDecision src = tc.getValue();
	    	if (src != null && (src instanceof SVComponentDecision)) {
	    		ParamUtils.addNames(usedNames,((SVComponentDecision)src).getParamValues());
	    		tc.getMeets().stream().filter(t->t instanceof SVComponentDecision).
	    			forEach(t->ParamUtils.addNames(usedNames,((SVComponentDecision)t).getParamValues()));
	    	}
	    	return usedNames;
		}
	};
	
	protected static HashSet<String> getAllNames(TransitionConstraint constraint) {
		return tcNameCollector.getNames(constraint);		
	}
	
    public static SVComponentDecision createTransitionConstraint(EObject self, ComponentDecisionType source, ComponentDecisionType target) {
    	assert source != null;
    	assert target != null;
    	StateVariableComponentType sv = (StateVariableComponentType)source.eContainer();
    	assert sv != null;
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ModelAccessor accessor = ModelUtils.getAccessor(self);
    	TransitionConstraint tc = null;
    	SVComponentDecision cd = null;
    	for (TransitionConstraint t : sv.getTransConstraint())
    	{
    		cd = (SVComponentDecision)t.getValue();
    		ComponentDecisionType cdtype = cd != null ? cd.getValue() : null;
    		if (cdtype == source)
    		{
    			tc = t;
    			break;
    		}
    	}
    	if (tc == null)
    	{
    		tc = factory.createTransitionConstraint();
    		cd = factory.createSVComponentDecision();
    		cd.setValue(source);
    		ParamUtils.generateParamValues(source.getArgs(), cd.getParamValues());
    		tc.setValue(cd);
    		tc.setRange("[0,+INF]");
    		//sv.getTransConstraint().add(tc);
    		ModelUtils.setMulti(accessor, sv, tc, DdlPackage.STATE_VARIABLE_COMPONENT_TYPE__TRANS_CONSTRAINT);
    	}
    	SVComponentDecision dest = factory.createSVComponentDecision();
    	dest.setValue(target);
    	ParamUtils.generateParamValues(target.getArgs(), dest.getParamValues(), tcNameCollector.getNames(tc));
    	//tc.getMeets().add(dest);
		ModelUtils.setMulti(accessor, tc, dest, DdlPackage.TRANSITION_CONSTRAINT__MEETS);
    	return dest;
    }	
}
