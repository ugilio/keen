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

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentType;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.LValue;
import it.cnr.istc.keen.ddl.RValue;
import it.cnr.istc.keen.ddl.RenewableResourceComponentType;
import it.cnr.istc.keen.ddl.TemporalConstraint;
import it.cnr.istc.keen.ddl.TransitionConstraint;
import it.cnr.istc.keen.ddl.URange;
import it.cnr.istc.keen.ddl.VarRef;
import it.cnr.istc.keen.modeling.design.ext.selection.GenericSelectionWizard;
import it.cnr.istc.keen.modeling.design.ext.selection.SelectionWizard;

public class PropUtils {
	
    //aql:eStructuralFeature.name.toUpperFirst()+': '
    public static String propGetLabel(EObject self, EStructuralFeature feature) {
    	if (feature==DdlPackage.Literals.URANGE__LB)
    		return "Lower bound";
    	else if (feature==DdlPackage.Literals.URANGE__UB)
    		return "Upper bound";
    	else if (feature==DdlPackage.Literals.COMPONENT_DECISION_TYPE__ARGS)
    		return "Arguments";
    	else if (feature==DdlPackage.Literals.CONSUMABLE_RESOURCE_COMPONENT_TYPE__VAL1)
    		return "Min";
    	else if (feature==DdlPackage.Literals.CONSUMABLE_RESOURCE_COMPONENT_TYPE__VAL2)
    		return "Max";
    	else if (feature==DdlPackage.Literals.TIMELINE__PARAMS)
    		return "Tags";
    	else if (feature==DdlPackage.Literals.TEMPORAL_CONSTRAINT__REL_TYPE)
    		return "Temporal Relation";
    	
    	
    	String name = feature.getName();
    	if (name==null || name.length()==0)
    		return name;
    	return name.substring(0,1).toUpperCase()+name.substring(1);
    }
    
    //aql:input.emfEditServices(self).setValue(eStructuralFeature, newValue)
    public static EObject propSetValue(EObject self, EStructuralFeature feature, Object value,
    		TemporalRelationsUtils temporalRelationsUtils) {
    	if (feature==DdlPackage.Literals.COMPONENT_DECISION_TYPE__NAME) {
    		((ComponentDecisionType)self).setName(ParsingUtils.parseFunctionName(self, ""+value));
    		CdtUtils.setCdTypeArgs((ComponentDecisionType)self, ""+value);
    	}
    	//Generic name
    	else if ("name".equals(feature.getName()))
    		EditingUtils.uniqueName(self, (String)value);
    	//URange
    	else if (feature==DdlPackage.Literals.URANGE__LB)
    		((URange)self).setLb(ParsingUtils.checkUNumber(self, (String)value, ((URange)self).getLb()));
    	else if (feature==DdlPackage.Literals.URANGE__UB)
    		((URange)self).setUb(ParsingUtils.checkUNumber(self, (String)value, ((URange)self).getUb()));
    	//Renewable Resource
    	else if (feature==DdlPackage.Literals.RENEWABLE_RESOURCE_COMPONENT_TYPE__VALUE)
    		((RenewableResourceComponentType)self).setValue(ParsingUtils.parseNumProp("a: "+value));
    	//Consumable Resource
    	else if (feature==DdlPackage.Literals.CONSUMABLE_RESOURCE_COMPONENT_TYPE__VAL1)
    		((ConsumableResourceComponentType)self).setVal1(ParsingUtils.parseNumProp("a: "+value));
    	else if (feature==DdlPackage.Literals.CONSUMABLE_RESOURCE_COMPONENT_TYPE__VAL2)
    		((ConsumableResourceComponentType)self).setVal2(ParsingUtils.parseNumProp("a: "+value));
    	//Transition Constraint
    	else if (feature==DdlPackage.Literals.TRANSITION_CONSTRAINT__RANGE)
    		((TransitionConstraint)self).setRange(ParsingUtils.parseRange(""+value,((TransitionConstraint)self).getRange()));
    	//Temporal Constraint
    	else if (feature==DdlPackage.Literals.TEMPORAL_CONSTRAINT__REL_TYPE)
    		((TemporalConstraint)self).setRelType(
    				temporalRelationsUtils.parseTemporalRelationType(
    						(TemporalConstraint)self,""+value,((TemporalConstraint)self).getRelType()));
    	//Timeline
    	if (feature==DdlPackage.Literals.TIMELINE__PARAMS && (value instanceof Boolean))
    		VirtualPropUtils.setExternalTimeline(self,value);
    	
    	//Default case
    	else
    		self.eSet(feature, value);
    	return self;
    }
    
    //aql:self.eGet(eStructuralFeature.name)
    public static Object propGetValue(EObject self, EStructuralFeature feature) {
    	if (feature==DdlPackage.Literals.COMPONENT_DECISION_TYPE__NAME) {
    		//aql:self.name+'('+self.args->collect(a|a.name)->sep(', ')->toString()+')';
    		ComponentDecisionType cdt = (ComponentDecisionType)self;
    		return cdt.getName()+"("+
    		cdt.getArgs().stream().map(pt -> pt.getName()).collect(Collectors.joining(", "))
    		+")";
    	}
    		
    	//default case
    	return self.eGet(feature);
    }

    public static boolean propIsEnabled(EObject self, EStructuralFeature feature) {
    	//all reference features but Component.type are read-only
    	if (feature instanceof EReference && !feature.isMany() && feature!=DdlPackage.Literals.COMPONENT__TYPE)
    		return false;
    	
    	return feature.isChangeable();
    }
    
    public static boolean hideFromProperties(EObject self, EStructuralFeature feature) {
    	if (feature==DdlPackage.Literals.COMPONENT_DECISION_TYPE__ARGS)
    		return true;
    	if (feature==DdlPackage.Literals.TIMELINE__PARAMS)
    		return true;
    	if (feature==DdlPackage.Literals.PARAMETER_CONSTRAINT__OP)
    		return true;
    	if (self instanceof LValue || self instanceof RValue || self instanceof VarRef)
    		return true;
    	return false;
    }
    
    public static EObject selectReference(EObject self, EStructuralFeature feature) {
    	if (feature==DdlPackage.Literals.COMPONENT__TYPE) {
    		Component comp = (Component)self;
    		SelectionWizard wiz = 
    				new GenericSelectionWizard(ComponentType.class,
    				"Select a component type",
    				"Select the component type you want to use for this component",
    				null,
    				false);
    		Collection<EObject> result = wiz.openSelectionWizard(self);
    		if (result.size()==1) {
    			ComponentType ct = (ComponentType)result.iterator().next();
    			EditingUtils.editComponentLabel(comp, comp.getName()+": "+ct.getName());
    		}
    	}
    	return self;
    }    
}
