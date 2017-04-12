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
package it.cnr.istc.keen.modeling.design;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.diagram.DDiagram;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentDecision;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.ParameterConstraint;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.TemporalConstraint;
import it.cnr.istc.keen.ddl.URange;
import it.cnr.istc.keen.modeling.design.ext.selection.SyncTargetSelectionWizard;
import it.cnr.istc.keen.modeling.design.ext.services.CdtUtils;
import it.cnr.istc.keen.modeling.design.ext.services.EditingUtils;
import it.cnr.istc.keen.modeling.design.ext.services.HyperlinkUtils;
import it.cnr.istc.keen.modeling.design.ext.services.ParsingUtils;
import it.cnr.istc.keen.modeling.design.ext.services.PropUtils;
import it.cnr.istc.keen.modeling.design.ext.services.SynchronizationUtils;
import it.cnr.istc.keen.modeling.design.ext.services.TemporalRelationsUtils;
import it.cnr.istc.keen.modeling.design.ext.services.TransitionConstraintUtils;
import it.cnr.istc.keen.modeling.design.ext.services.VirtualPropUtils;
import it.cnr.istc.keen.modeling.design.ext.services.VirtualPropUtils.Controllability;

/**
 * The services class used by VSM.
 */
public class Services {
	
	TemporalRelationsUtils temporalRelationsUtils;
	SyncTargetSelectionWizard syncTargetSelectionWizard;
	
    private TemporalRelationsUtils getTemporalRelationsUtils() {
    	if (temporalRelationsUtils==null)
    		temporalRelationsUtils = new TemporalRelationsUtils();
    	return temporalRelationsUtils;
    }

    public URange parseURange(URange self, String arg) {
    	return ParsingUtils.parseURange(self, arg);
    }
    
    public String parseRange(String arg, String orig) {
    	return ParsingUtils.parseRange(arg, orig);
    }
    
    public String checkNumber(EObject self, String arg, String orig) {
    	return ParsingUtils.checkNumber(self, arg, orig);
    }
    
    public String checkUNumber(EObject self, String arg, String orig) {
    	return ParsingUtils.checkUNumber(self, arg, orig);
    }
    
    public Integer parseNumProp(String arg) {
    	return ParsingUtils.parseNumProp(arg);
    }
    
    public String parseFunctionName(EObject self, String args) {
    	return ParsingUtils.parseFunctionName(self, args);    	
    }
    
    public EObject setCdTypeArgs(ComponentDecisionType self, String args) {
    	return CdtUtils.setCdTypeArgs(self, args);
    }

    public String parseTemporalRelationType(EObject self, String args, String orig) {
    	return getTemporalRelationsUtils().parseTemporalRelationType(self, args, orig);
    }
    
    public SVComponentDecision createTransitionConstraint(EObject self, ComponentDecisionType source, ComponentDecisionType target) {
    	return TransitionConstraintUtils.createTransitionConstraint(self, source, target);
    }
    
    public InstantiatedComponentDecision getExtraParameterConstraintsSource(ParameterConstraint self) {
    	return SynchronizationUtils.getExtraParameterConstraintsSource(self);
    }
    
    public List<InstantiatedComponentDecision> getExtraParameterConstraintsTarget(ParameterConstraint self) {
    	return SynchronizationUtils.getExtraParameterConstraintsTarget(self);
    }
    
    public List<Component> getTimelineDependencies(Component self) {
    	return SynchronizationUtils.getTimelineDependencies(self);
    }

    public EObject createSynchronization(EObject self, EObject head, Collection<Object> selection) {
    	return SynchronizationUtils.createSynchronization(self, head, selection);
    }
    
    public EObject connectSynchronizationElements(EObject self, EObject srcObj, InstantiatedComponentDecision dest) {
    	return SynchronizationUtils.connectSynchronizationElements(self, srcObj, dest);
    }
    
    public EObject getSynchronizationSource(TemporalConstraint self) {
    	return SynchronizationUtils.getSynchronizationSource(self);
    }
    
    public Boolean canCreateTrigger(EObject self, EObject element, EObject view) {
    	return SynchronizationUtils.canCreateTrigger(self, element, view);
    }

    public Synchronization createSyncValueBlock(EObject self, EObject element, EObject view) {
    	return SynchronizationUtils.createSyncValueBlock(self, element, view);
    }
    
    public String getComponentDecisionName(ComponentDecision dec) {
    	return SynchronizationUtils.getComponentDecisionName(dec);
    }
    
    public String getICDName(InstantiatedComponentDecision icd) {
    	return SynchronizationUtils.getICDName(icd);
    }
    
    public EObject getComponentDecisionSourceType(ComponentDecision dec) {
    	return SynchronizationUtils.getComponentDecisionSourceType(dec);
    }
    
    public List<Synchronization> getComponentDecisionsForType(EObject self, DDiagram diagram) {
    	return SynchronizationUtils.getComponentDecisionsForType(self,diagram);
    }
    
    public EObject editComponentLabel(Component comp, String arg) {
    	return EditingUtils.editComponentLabel(comp, arg);
    }
    
    public EObject delete(EObject self) {
    	return EditingUtils.delete(self);
    }
    
    public EObject uniqueName(EObject self, String name) {
    	return EditingUtils.uniqueName(self, name);
    }
    
    public Collection<EObject> syncTargetSelectionWizard(EObject self) {
    	if (syncTargetSelectionWizard == null)
    		syncTargetSelectionWizard = new SyncTargetSelectionWizard();
    	return syncTargetSelectionWizard.openSelectionWizard(self);
    }
    
    public String propGetLabel(EObject self, EStructuralFeature feature) {
    	return PropUtils.propGetLabel(self, feature);
    }
    
    public EObject propSetValue(EObject self, EStructuralFeature feature, Object value) {
    	return PropUtils.propSetValue(self, feature, value, getTemporalRelationsUtils());
    }
    
    public Object propGetValue(EObject self, EStructuralFeature feature) {
    	return PropUtils.propGetValue(self, feature);
    }

    public boolean propIsEnabled(EObject self, EStructuralFeature feature) {
    	return PropUtils.propIsEnabled(self, feature);
    }
    
    public boolean hideFromProperties(EObject self, EStructuralFeature feature) {
    	return PropUtils.hideFromProperties(self, feature);
    }
    
    public EObject selectReference(EObject self, EStructuralFeature feature) {
    	return PropUtils.selectReference(self, feature);
    }
    
    public boolean isExternalTimelineFeature(EObject self, EStructuralFeature feature) {
    	return VirtualPropUtils.isExternalTimelineFeature(self, feature);
    }
    
    public Boolean isExternalTimeline(EObject self) {
    	return VirtualPropUtils.isExternalTimeline(self);
    }
    
    public boolean canBeControllable(EObject self, EStructuralFeature feature) {
    	return VirtualPropUtils.canBeControllable(self, feature);
    }    
    
    public List<Controllability> getControllabilityValues(EObject self) {
    	return VirtualPropUtils.getControllabilityValues(self);
    }
    
    public Controllability getControllability(EObject self) {
    	return VirtualPropUtils.getControllability(self);
    }
    
    public EObject setControllability(EObject self, Controllability value) {
    	return VirtualPropUtils.setControllability(self, value); 
    }
    
    public EObject testThis(EObject self, EObject session, EObject selection) {
    	return HyperlinkUtils.openHyperLink(self, session, selection);
    }
}
