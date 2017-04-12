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
package it.cnr.istc.keen.modeling.design.ext.selection;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentDecision;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentDecision;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentType;
import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.RenewableResourceComponentDecision;
import it.cnr.istc.keen.ddl.RenewableResourceComponentType;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.SynchronizationElement;
import it.cnr.istc.keen.ddl.Timeline;

public class SyncTargetSelectionWizard extends SelectionWizard {

    private final AdapterFactory factory = new SyncTargetAdapterFactory();
	
	public SyncTargetSelectionWizard() {
		super(
				"Select targets",
				"Select the values you want to use as targets of the synchronization.",
				null,
				true
				);
	}
	
    private InstantiatedComponentDecision createSynchronizationElement(Timeline tml, EObject cdType) {
    	DdlFactory fact = DdlFactory.eINSTANCE;

    	ComponentDecision dec;
    	if (cdType instanceof ComponentDecisionType) {
    		SVComponentDecision svdec = fact.createSVComponentDecision();
    		svdec.setValue((ComponentDecisionType)cdType);
    		dec=svdec;
    	} else if (cdType instanceof RenewableResourceComponentDecision) {
    		dec=(ComponentDecision)cdType;
    	} else if (cdType instanceof ConsumableResourceComponentDecision) {
    		dec=(ComponentDecision)cdType;
    	}
    	else
    		throw new InvalidParameterException("Unknown Component Decision Type: "+
    				cdType.getClass().getName());
    	InstantiatedComponentDecision icd = fact.createInstantiatedComponentDecision();
    	icd.setDecision(dec);
    	icd.setTimeline(tml);
    	
    	return icd;
    }
    
    private Stream<? extends EObject> getDeclaredValues(Component c) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ComponentType ct = c.getType();
    	if (ct instanceof StateVariableComponentType) {
    		StateVariableComponentType svt = (StateVariableComponentType)ct;
    		return svt.getDecTypes().stream();
    	}
    	else if (ct instanceof RenewableResourceComponentType) {
    		return Stream.of(factory.createRenewableResourceComponentDecision());
    	}
    	else if (ct instanceof ConsumableResourceComponentType) {
    		return Stream.<EObject>builder().
    			add(factory.createCRConsumptionComponentDecision()).
    			add(factory.createCRProductionComponentDecision()).
    			build();
    	}
    	throw new InvalidParameterException("Unknown Component Type: "+ct.getClass().getName());
    }
    
    private boolean componentEquals(Component c1, Component c2) {
    	if (c1 == null || c2 == null)
    		return c1 == c2;
    	return c1.getName().equals(c2.getName());
    }
    
    private boolean timelineEquals(Timeline t1, Timeline t2) {
    	if (t1 == null || t2 == null)
    		return t1 == t2;
    	return
    			t1.getName().equals(t2.getName()) &&
    			componentEquals((Component)t1.eContainer(), (Component)t2.eContainer());
    }
    
    private boolean cdEquals(ComponentDecision cd1, ComponentDecision cd2) {
    	try
    	{
    		if (cd1 == null || cd2 == null)
    			return cd1 == cd2;
    		if (cd1.getClass() != cd2.getClass())
    			return false;
    		if (cd1 instanceof SVComponentDecision)
    			return ((SVComponentDecision)cd1).getValue().getName().
    					equals(((SVComponentDecision)cd2).getValue().getName());
    		else if (cd1 instanceof RenewableResourceComponentDecision)
    			return true;
    		else if (cd1 instanceof ConsumableResourceComponentDecision)
    			return true;
    		else
        		throw new InvalidParameterException("Unknown Component Decision: "+
        				cd1.getClass().getName());
    	}
    	catch (NullPointerException e)
    	{
    		return false;
    	}
    }
    
    private boolean icdEquals(Object o1, Object o2) {
    	if (o1 == null || o2 == null)
    		return o1 == o2;
    	if (o1.getClass() != o2.getClass())
    		return false;
    	if (! (o1 instanceof InstantiatedComponentDecision))
    		return false;
    	InstantiatedComponentDecision e1 = (InstantiatedComponentDecision)o1;
    	InstantiatedComponentDecision e2 = (InstantiatedComponentDecision)o2;
    	return
    			cdEquals(e1.getDecision(),e2.getDecision()) &&
    			timelineEquals(e1.getTimeline(), e2.getTimeline());
    }
    
    protected Collection<EObject> computeInput(EObject self) {
    	final List<SynchronizationElement> existing;
    	if (self instanceof Synchronization)
    		existing = ((Synchronization)self).getElements().stream().
    		filter(e -> e instanceof InstantiatedComponentDecision).
    		collect(Collectors.toList());
    	else if (self instanceof InstantiatedComponentDecision)
    		existing = Collections.singletonList((InstantiatedComponentDecision)self);
    	else
    		existing = Collections.emptyList();

    	Domain dom = EcoreUtil2.getContainerOfType(self, Domain.class);
    	if (dom == null)
    		return Collections.emptyList();
    	return
    	dom.getElements().stream().
    		filter(o -> o instanceof Component).map(o -> (Component)o).
    		flatMap(comp -> comp.getTimelines().stream()).
    		flatMap(tml -> getDeclaredValues((Component)tml.eContainer()).
    				map(cdt -> createSynchronizationElement(tml,cdt))).
    		filter(e1 -> !existing.stream().anyMatch(e2 -> icdEquals(e1,e2))).
    		collect(Collectors.toList());
    }
    
    protected AdapterFactory getAdapterFactory() {
    	return factory;
    }
}
