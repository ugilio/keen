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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentDecision;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentDecision;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentType;
import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.ParValue;
import it.cnr.istc.keen.ddl.ParameterConstraint;
import it.cnr.istc.keen.ddl.RValue;
import it.cnr.istc.keen.ddl.RenewableResourceComponentDecision;
import it.cnr.istc.keen.ddl.RenewableResourceComponentType;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.SynchronizationElement;
import it.cnr.istc.keen.ddl.TemporalConstraint;
import it.cnr.istc.keen.ddl.TimelineSynchronization;
import it.cnr.istc.keen.ddl.VarRef;
import it.cnr.istc.keen.modeling.design.ext.services.ParamUtils.NameCollector;
import it.cnr.istc.keen.utils.DomainUtils;

public class SynchronizationUtils {
	
    private static NameCollector<Synchronization> icdNameCollector = new NameCollector<Synchronization>() {
		@Override
		public HashSet<String> getNames(Synchronization object) {
			HashSet<String> names = new LinkedHashSet<>();
			for (SynchronizationElement e : object.getElements())
				if (e instanceof InstantiatedComponentDecision)
					names.add(((InstantiatedComponentDecision)e).getName());
			return names;
		}
	};
	
    private static NameCollector<Synchronization> icdParValueCollector = new NameCollector<Synchronization>() {
		@Override
		public HashSet<String> getNames(Synchronization object) {
	    	HashSet<String> names = new LinkedHashSet<>();
	    	Supplier<Stream<ComponentDecision>> streamSupp  = () ->
	    	object.getElements().stream(). 
		    		filter(e -> e instanceof InstantiatedComponentDecision).
		    		map(e->((InstantiatedComponentDecision)e).getDecision());

	    	//SVComponentDecision
	    	streamSupp.get().
    		filter(dec -> dec != null && (dec instanceof SVComponentDecision)).
    		forEach(dec -> ParamUtils.addNames(names,((SVComponentDecision)dec).getParamValues()));
	    	//RenewableResourceComponentDecision
	    	streamSupp.get().
	    	filter(dec -> dec != null && (dec instanceof RenewableResourceComponentDecision)).
	    	forEach(dec -> names.add(((RenewableResourceComponentDecision)dec).getParamValue().getName()));
	    	//ConsumableResourceComponentDecision
	    	streamSupp.get().
	    	filter(dec -> dec != null && (dec instanceof ConsumableResourceComponentDecision)).
	    	forEach(dec -> names.add(((ConsumableResourceComponentDecision)dec).getParamValue().getName()));
			return names;
		}
	};
	
	protected static HashSet<String> getAllParValueNames(Synchronization sync) {
		return icdParValueCollector.getNames(sync);		
	}
	
    private static EObject getSemanticRoot(DDiagramElement view) {
    	if (view == null)
    		return null;
    	DDiagram diagram = view.getParentDiagram();
    	if (diagram instanceof DSemanticDiagram)
    		return ((DSemanticDiagram)diagram).getTarget();
    	return null;
    }
		
    private static void setMulti(ModelAccessor accessor, EObject obj, Object value, int featureID) {
    	ModelUtils.setMulti(accessor, obj, value, featureID);
    }
	
    private static EObject addSynchronizationElement(InstantiatedComponentDecision icd, Synchronization sync,
    		InstantiatedComponentDecision source) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ModelAccessor accessor = ModelUtils.getAccessor(sync);
    	
    	icd.setName(ParamUtils.generateUniqueName("cd", icdNameCollector.getNames(sync), false));
    	setCdValues(sync, icd);
    	//it's ok if icd is already in Synchronization.elements (elements in list are unique)
    	//sync.getElements().add(icd);
		setMulti(accessor, sync, icd, DdlPackage.SYNCHRONIZATION__ELEMENTS);

    	
    	//now create the temporal relation: "MEETS" if SV, "EQUALS" if resource
    	TemporalConstraint tc = factory.createTemporalConstraint();
    	tc.setRelType((icd.getDecision() instanceof SVComponentDecision) ? "MEETS" : "EQUALS");
    	tc.setTo(icd);
    	if (source != null)
    		tc.setFrom(source);
    	//sync.getElements().add(tc);
    	setMulti(accessor, sync, tc, DdlPackage.SYNCHRONIZATION__ELEMENTS);
    	return sync;
    }
    
    public static EObject createSynchronization(EObject self, EObject head, Collection<Object> selection) {
    	Synchronization sync = (head instanceof Synchronization)
    			? (Synchronization)head
    			: EcoreUtil2.getContainerOfType(head, Synchronization.class);
    	InstantiatedComponentDecision src = (head instanceof InstantiatedComponentDecision)
    			? (InstantiatedComponentDecision)head
    			: null;
    	for (Object o : selection) {
    		if (o instanceof InstantiatedComponentDecision)
    			addSynchronizationElement((InstantiatedComponentDecision)o, sync, src);
    	}
    	return sync;
    }
    
    public static EObject connectSynchronizationElements(EObject self, EObject srcObj, InstantiatedComponentDecision dest) {
    	if (!(srcObj instanceof InstantiatedComponentDecision))
    		return null;
    	InstantiatedComponentDecision source = (InstantiatedComponentDecision)srcObj;
    	Synchronization sync1 = EcoreUtil2.getContainerOfType(source, Synchronization.class);
    	Synchronization sync2 = EcoreUtil2.getContainerOfType(dest, Synchronization.class);
    	assert sync1 == sync2 : "Cannot connect existing InstantiatedComponentDecisions belonging to different synchronizations";
    	return addSynchronizationElement(dest, sync1, source);
    }
    
    public static EObject getSynchronizationSource(TemporalConstraint self) {
    	InstantiatedComponentDecision icd = self.getFrom();
    	if (icd != null)
    		return icd;
    	return EcoreUtil2.getContainerOfType(self, Synchronization.class);
    }
    
    public static Synchronization createSyncValueBlock(EObject self, EObject element, EObject view) {
    	DDiagramElement dde = (DDiagramElement)view;
    	TimelineSynchronization ts = (TimelineSynchronization)getSemanticRoot(dde);
    	boolean consumption = (element instanceof ConsumableResourceComponentType) &&
    			"CONSUMPTION".equals(dde.getName().trim());
    	return createSyncValueBlock(self, element, ts, consumption);
    }
    
    private static Synchronization doCreateSyncValueBlock(EObject self, ComponentDecisionType sourceType, TimelineSynchronization ts) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ModelAccessor accessor = ModelUtils.getAccessor(self);
    	
    	Synchronization sync = factory.createSynchronization();
    	SVComponentDecision cd = factory.createSVComponentDecision();
    	cd.setValue(sourceType);
    	ParamUtils.generateParamValues(sourceType.getArgs(), cd.getParamValues());
    	sync.setValue(cd);
    	//ts.getSynchronizations().add(sync);
    	setMulti(accessor, ts, sync, DdlPackage.TIMELINE_SYNCHRONIZATION__SYNCHRONIZATIONS);
    	
    	return sync;
    }

    private static Synchronization doCreateSyncValueBlock(EObject self, RenewableResourceComponentType sourceType, TimelineSynchronization ts) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ModelAccessor accessor = ModelUtils.getAccessor(self);
    	
    	Synchronization sync = factory.createSynchronization();
    	RenewableResourceComponentDecision cd = factory.createRenewableResourceComponentDecision();
		ParValue value = factory.createParValue();
		value.setName("?amount");
		cd.setParamValue(value);
    	
    	sync.setValue(cd);
    	//ts.getSynchronizations().add(sync);
    	setMulti(accessor, ts, sync, DdlPackage.TIMELINE_SYNCHRONIZATION__SYNCHRONIZATIONS);
    	
    	return sync;
    }
    
    private static Synchronization doCreateSyncValueBlock(EObject self, ConsumableResourceComponentType sourceType,
    		TimelineSynchronization ts, boolean consumption) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	ModelAccessor accessor = ModelUtils.getAccessor(self);
    	
    	Synchronization sync = factory.createSynchronization();
    	ConsumableResourceComponentDecision cd = consumption
    			? factory.createCRConsumptionComponentDecision()
    			: factory.createCRProductionComponentDecision();
		ParValue value = factory.createParValue();
		value.setName("?amount");
		cd.setParamValue(value);
    	
    	sync.setValue(cd);
    	//ts.getSynchronizations().add(sync);
    	setMulti(accessor, ts, sync, DdlPackage.TIMELINE_SYNCHRONIZATION__SYNCHRONIZATIONS);
    	
    	return sync;
    }    

    private static Synchronization createSyncValueBlock(EObject self, EObject arg1, EObject arg2, boolean consumption) {
    	TimelineSynchronization ts = (TimelineSynchronization)arg2;
    	if (arg1 instanceof ComponentDecisionType) {
    		return doCreateSyncValueBlock(self, (ComponentDecisionType)arg1, ts);
    	} else if (arg1 instanceof RenewableResourceComponentType) {
    		return doCreateSyncValueBlock(self, (RenewableResourceComponentType)arg1, ts);
    	} else if (arg1 instanceof ConsumableResourceComponentType) {
    		return doCreateSyncValueBlock(self, (ConsumableResourceComponentType)arg1, ts, consumption);
    	}
    	else throw new InvalidParameterException("Unknown source type: "+arg1.getClass().getName());
    }
    
    private static void setCdValues(Synchronization sync, InstantiatedComponentDecision icd) {
    	ComponentDecision cd = icd.getDecision();
		ComponentType ctype = ((Component)icd.getTimeline().eContainer()).getType();
    	HashSet<String> usedNames = icdParValueCollector.getNames(sync);
    	if (cd instanceof SVComponentDecision) {
        	SVComponentDecision svcd = (SVComponentDecision)icd.getDecision();
        	ParamUtils.generateParamValues(svcd.getValue().getArgs(), svcd.getParamValues(), usedNames);
    	} else if (cd instanceof RenewableResourceComponentDecision) {
    		RenewableResourceComponentDecision rrcd = (RenewableResourceComponentDecision)cd;
    		ParValue value = ParamUtils.generateParamValue("?amount", usedNames);
    		value.setValue(ParsingUtils.numToStr(((RenewableResourceComponentType)ctype).getValue()));
    		rrcd.setParamValue(value);
    	} else if (cd instanceof ConsumableResourceComponentDecision) {
    		ConsumableResourceComponentDecision crcd = (ConsumableResourceComponentDecision)cd;
    		ParValue value = ParamUtils.generateParamValue("?amount", usedNames);
    		value.setValue(ParsingUtils.numToStr(((ConsumableResourceComponentType)ctype).getVal1()));
    		crcd.setParamValue(value);
    	}
    	else throw new InvalidParameterException("Unknown Component Decision: "+cd.getClass().getName());
    }
    
    public static Boolean canCreateTrigger(EObject self, EObject element, EObject view) {
    	DDiagramElement dde = (DDiagramElement)view;
    	if (!(getSemanticRoot(dde) instanceof TimelineSynchronization))
    		return false;
    	if (element instanceof ConsumableResourceComponentType)
    		return "CONSUMPTION".equals(dde.getName().trim()) || "PRODUCTION".equals(dde.getName().trim());
    	return (element instanceof ComponentDecisionType) || (element instanceof RenewableResourceComponentType); 
    }
    
    public static InstantiatedComponentDecision getExtraParameterConstraintsSource(ParameterConstraint self) {
    	if (self.getLeft() == null || self.getLeft().getVar() == null)
    		return null;
    	VarRef ref = self.getLeft().getVar();
    	ComponentDecision cd = (ComponentDecision)ref.getRef().eContainer();
    	EObject cont = cd.eContainer();
    	if (cont instanceof InstantiatedComponentDecision)
    		return (InstantiatedComponentDecision)cont;
    	return null;
    }
    
    public static List<InstantiatedComponentDecision> getExtraParameterConstraintsTarget(ParameterConstraint self) {
    	InstantiatedComponentDecision src = getExtraParameterConstraintsSource(self);
    	if (src == null)
    		return null;
    	RValue rval = self.getRight();
    	if (rval == null)
    		return null;
    	List<InstantiatedComponentDecision> list = new LinkedList<>();
    	TreeIterator<EObject> it = rval.eAllContents();
    	while (it.hasNext()) {
    		EObject obj = it.next();
    		if (!(obj instanceof VarRef))
    			continue;
    		VarRef ref = (VarRef)obj;
        	ComponentDecision cd = (ComponentDecision)ref.getRef().eContainer();
        	EObject cont = cd.eContainer();
        	if (cont instanceof InstantiatedComponentDecision)
        		list.add((InstantiatedComponentDecision)cont);
    	}
    	if (list.size()<=0)
    		return null;
    	Synchronization sync = (Synchronization)src.eContainer();
    	list = 
    	list.stream().filter(dest -> !sync.getElements().stream().
    			filter(t -> t instanceof TemporalConstraint).map(t -> (TemporalConstraint)t).
    	    	filter(t -> (t.getFrom()==src && t.getTo()==dest) || (t.getTo()==src && t.getFrom()==dest)).
    	    	findAny().isPresent()).
    			collect(Collectors.toList());
    	return list;
    }
    
    public static String getComponentDecisionName(ComponentDecision dec) {
    	TimelineSynchronization ts = EcoreUtil2.getContainerOfType(dec, TimelineSynchronization.class);
		return DomainUtils.getFullDecisionName(dec,ts.getTimeline());
    }
    
    public static String getICDName(InstantiatedComponentDecision icd) {
    	return icd.getName()+": "+DomainUtils.getFullDecisionName(icd.getDecision(),icd.getTimeline());
    }
    
    public static EObject getComponentDecisionSourceType(ComponentDecision dec) {
    	//ComponentDecisionType for SVComponentDecision
    	if (dec instanceof SVComponentDecision)
    		return ((SVComponentDecision)dec).getValue();
    	//RenewableResourceComponentType or ConsumableResourceComponentType for resources
    	TimelineSynchronization ts = EcoreUtil2.getContainerOfType(dec, TimelineSynchronization.class);
    	return ((Component) ts.getTimeline().eContainer()).getType();
    }
    
    private static boolean isOfType(ComponentDecision d, Object self) {
    	return getComponentDecisionSourceType(d)==self;
    }
    
    public static List<Synchronization> getComponentDecisionsForType(EObject self, DDiagram diagram) {
    	if (diagram instanceof DSemanticDiagram) {
    		EObject tmp = ((DSemanticDiagram)diagram).getTarget();
    		if (tmp instanceof TimelineSynchronization) {
    			TimelineSynchronization ts = (TimelineSynchronization)tmp;
    			return ts.getSynchronizations().stream().
    	    	filter(s -> isOfType(s.getValue(),self)).
        	collect(Collectors.toList());
    		}
    	}
    	return Collections.emptyList();
    }
    
    public static List<Component> getTimelineDependencies(Component self) {
    	Domain dom = EcoreUtil2.getContainerOfType(self, Domain.class);
    	if (dom != null) {
    		return
    		dom.getElements().stream().
    		filter(t -> t instanceof TimelineSynchronization).
    		map(t -> (TimelineSynchronization)t).
    		filter(t -> t.getTimeline().eContainer()==self).
    		flatMap(t -> t.getSynchronizations().stream()).
    		flatMap(s -> s.getElements().stream()).
    		filter(s -> s instanceof TemporalConstraint).
    		map(tc -> ((TemporalConstraint)tc).getTo().getTimeline()).
    		map(tml -> (Component)tml.eContainer()).
    		distinct().filter(tm -> tm != self).
    		collect(Collectors.toList());
    	}
    	return Collections.emptyList();
    }
    
    
}
