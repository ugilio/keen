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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sirius.business.api.query.EObjectQuery;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.ParType;
import it.cnr.istc.keen.ddl.ParValue;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.TransitionConstraint;
import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator;
import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator.Differences;
import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator.Operation;

public class CdtUtils {
    public static EObject setCdTypeArgs(ComponentDecisionType self, String args) {
    	//aql:arg0.replace('[^(]*\\(([^)]*)\\)','$1').tokenize(',')->collect(s|s.trim())->collect(s|self.eContainerOrSelf(ddl::Domain).elements->filter(ddl::ParType)->any(o|o.name=s))
    	String regex = "[^(]*\\(([^)]*)\\)";
    	String tmp = null;
    	if (args.matches(regex)) {
    		tmp = args.replaceAll(regex,"$1").trim();
    	}
    	
    	List<ParType> params;
    	
    	if (tmp==null || tmp.length()<=0)
    		params = Collections.emptyList();
    	else
    	{
    		String[] strings = tmp.split(",");

    		ParType[] types = EcoreUtil2.getContainerOfType(self, Domain.class).getElements().
    				stream().filter(o -> o instanceof ParType).toArray(ParType[]::new);

    		params = 
    				Stream.of(strings).map(n->n.trim()).
    				map(n->Stream.of(types).filter(t->n.equals(t.getName())).findAny().get()).
    				collect(Collectors.toList());
    	}
    	
    	EList<ParType> plist = self.getArgs();
    	Differences<ParType> diff = computeParTypeDifferences(plist, params);
    	plist.clear();
    	//EObjectResolvingEList doesn't allow to add duplicate entries.
    	//So, use this trick (as Xtext does)
    	for (ParType p : params)
    		((InternalEList<ParType>)plist).addUnique(p);
    	
    	//update the auto-generated calls
    	updateComponentDecisionReferences(self, diff);
    	
    	return self;
    }
    
    private static void updateComponentDecisionReferences(ComponentDecisionType cdt, Differences<ParType> diff) {
    	DdlFactory factory = DdlFactory.eINSTANCE;
    	List<SVComponentDecision> cds =
    			new EObjectQuery(cdt).getInverseReferences(DdlPackage.eINSTANCE.getSVComponentDecision_Value()).
    			stream().map(o -> (SVComponentDecision)o).collect(Collectors.toList());
    	for (SVComponentDecision cd : cds) {
    		EList<ParValue> pl = cd.getParamValues();
    		EObject cont = cd.eContainer();
    		//delete what's to be deleted
    		List<ParValue> toDelete = new ArrayList<>(diff.toDelete.size());
    		for (int i = diff.toDelete.size()-1; i >=0; i--)
    			toDelete.add(pl.get(diff.toDelete.get(i).position));
    		HashSet<EObject> deleted = new HashSet<>();
    		deleted.add(cd);
    		for (ParValue v : toDelete)
    			EditingUtils.deleteExcluding(v, deleted);
    		
    		HashSet<String> usedNames = null; 
    		if (cont instanceof TransitionConstraint)
    			usedNames = TransitionConstraintUtils.getAllNames((TransitionConstraint)cont);
    		else if (cont instanceof Synchronization)
    			usedNames = SynchronizationUtils.getAllParValueNames((Synchronization)cont);
    		else if (cont instanceof InstantiatedComponentDecision) {
    			EObject contcont = cont.eContainer();
    			if (contcont instanceof Synchronization)
    				usedNames = SynchronizationUtils.getAllParValueNames((Synchronization)contcont);
    		}
    		assert usedNames != null : "Found an unknown container in updateComponentDecisionReferences";
    		
    		//add what's to be added
    		for (Operation<ParType> o : diff.toAdd)
    			pl.add(o.position, factory.createParValue());
    		ParamUtils.updateParamValues(cdt.getArgs(), pl, usedNames);
    	}
    	
    }
    
    private static Differences<ParType> computeParTypeDifferences(List<ParType> before, List<ParType> after) {
    	return new DiffCalculator<ParType>().computeDifferences(before, after);
    }
}
