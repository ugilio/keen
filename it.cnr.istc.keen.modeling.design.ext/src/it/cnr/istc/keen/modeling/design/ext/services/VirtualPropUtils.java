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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.Parameter;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.keen.ddl.Timeline;
import it.cnr.istc.keen.ddl.TransitionConstraint;

public class VirtualPropUtils {
    private static final String EXTERNAL_TAG = "external"; 
    private static final String CONTROLLABLE_TAG = "c";
    private static final String UNCONTROLLABLE_TAG = "u";
    public enum Controllability {Unspecified, Controllable, Uncontrollable};
    
	
    public static boolean isExternalTimelineFeature(EObject self, EStructuralFeature feature) {
    	if (feature==DdlPackage.Literals.TIMELINE__PARAMS)
    		return true;
    	else
    		return false;
    }
    
    public static Boolean isExternalTimeline(EObject self) {
    	if (self instanceof Timeline) {
    		return ((Timeline)self).getParams().stream().filter(s -> EXTERNAL_TAG.equals(s)).findAny().isPresent();
    	}
    	return false;
    }
    
    protected static void setExternalTimeline(EObject self, Object value) {
    	boolean enable = ((Boolean)value).booleanValue();
    	if (enable == isExternalTimeline(self))
    		return;
    	Timeline tml = (Timeline)self;
    	if (enable)
    		tml.getParams().add(EXTERNAL_TAG);
    	else
    		tml.getParams().remove(EXTERNAL_TAG);
    }
    
    private static SVComponentDecision getSVCDInstance(EObject self) {
    	if (! (self instanceof ComponentDecisionType))
    		return null;
    	EObject cont = self.eContainer();
    	if (! (cont instanceof StateVariableComponentType))
    		return null;
    	Optional<TransitionConstraint> opt =
    			((StateVariableComponentType)cont).getTransConstraint().stream().
    			filter(tc -> tc.getValue()!=null && tc.getValue().getValue()==self).
    			findAny();
    	if (!opt.isPresent())
    		return null;
    	return opt.get().getValue();
    }
    
    public static boolean canBeControllable(EObject self, EStructuralFeature feature) {
    	return feature==DdlPackage.Literals.COMPONENT_DECISION_TYPE__NAME && getSVCDInstance(self)!=null;
    }    
    
    public static List<Controllability> getControllabilityValues(EObject self) {
    	return Arrays.asList(Controllability.values());
    }
    
    public static Controllability getControllability(EObject self) {
    	SVComponentDecision dec = getSVCDInstance(self);
    	if (dec==null)
    		return Controllability.Unspecified;
    	boolean isControllable = dec.getParams().stream().filter(p -> CONTROLLABLE_TAG.equals(p.getName())).findAny().isPresent();
    	boolean isUncontrollable = dec.getParams().stream().filter(p -> UNCONTROLLABLE_TAG.equals(p.getName())).findAny().isPresent();
    	if (isControllable == isUncontrollable)
    		return Controllability.Unspecified;
    	if (isControllable)
    		return Controllability.Controllable;
    	
    	return Controllability.Uncontrollable;
    }
    
    public static EObject setControllability(EObject self, Controllability value) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	SVComponentDecision dec = getSVCDInstance(self);
    	if (dec==null)
    		return self;
    	
    	if (value!=Controllability.Controllable)
    		dec.getParams().removeAll(
    				dec.getParams().stream().
    				filter(p -> CONTROLLABLE_TAG.equals(p.getName())).collect(Collectors.toList()));
    	if (value!=Controllability.Uncontrollable)
    		dec.getParams().removeAll(
    				dec.getParams().stream().
    				filter(p -> UNCONTROLLABLE_TAG.equals(p.getName())).collect(Collectors.toList()));
    	//In case of unchanged value, don't remove and re-add
    	if (getControllability(self)==value)
    		return self;
    	//Else we must add the new value
    	if (value!=Controllability.Unspecified) {
    		Parameter par = factory.createParameter();
    		if (value==Controllability.Controllable)
    			par.setName(CONTROLLABLE_TAG);
    		else if (value==Controllability.Uncontrollable)
    			par.setName(UNCONTROLLABLE_TAG);
			dec.getParams().add(par);
    	}
		
    	return self;
    }
}
