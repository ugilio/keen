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
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import it.cnr.istc.keen.ddl.DdlFactory;
import it.cnr.istc.keen.ddl.ParType;
import it.cnr.istc.keen.ddl.ParValue;

public class ParamUtils {
	
    protected static interface NameCollector<T extends EObject> {
    	public HashSet<String> getNames(T object);
    }
    
	protected static void addNames(HashSet<String> names,EList<ParValue> parValues) {
    	for (ParValue v : parValues)
    		names.add(v.getName());
    }
    
    protected static String generateUniqueName(String base, HashSet<String> usedNames, boolean firstNotNumbered) {
    	int count=0;
    	String name = base;
    	if (!firstNotNumbered)
    		name=base+(count++);
    	while (usedNames.contains(name))
    		name=base+(count++);
    	usedNames.add(name);
    	return name;
    }
	
    protected static void generateParamValues(EList<ParType> formal, EList<ParValue> actual, HashSet<String> usedNames) {
    	DdlFactory factory = DdlFactory.eINSTANCE; 
    	for (ParType t : formal) {
    		String base = "?"+t.getName();
    		String name = generateUniqueName(base, usedNames, true);
    		ParValue v = factory.createParValue();
    		v.setName(name);
    		actual.add(v);
    	}
    }

    protected static void generateParamValues(EList<ParType> formal, EList<ParValue> actual) {
    	generateParamValues(formal, actual, new LinkedHashSet<String>());
    }
    
    protected static ParValue generateParamValue(String base, HashSet<String> usedNames) {
    	String name = generateUniqueName(base, usedNames, true);
    	ParValue v = DdlFactory.eINSTANCE.createParValue();
    	v.setName(name);
    	return v;
    }

    protected static void updateParamValues(EList<ParType> formal, EList<ParValue> actual, HashSet<String> usedNames) {
    	Iterator<ParValue> it = actual.iterator();
    	for (ParType t : formal) {
    		ParValue v = it.next();
    		if (v.getName()!=null)
    			continue;
    		String base = "?"+t.getName();
    		String name = generateUniqueName(base, usedNames, true);
    		v.setName(name);
    	}
    }
}
