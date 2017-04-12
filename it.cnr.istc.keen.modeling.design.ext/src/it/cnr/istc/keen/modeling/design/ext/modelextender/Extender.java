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
package it.cnr.istc.keen.modeling.design.ext.modelextender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.ecore.extender.business.internal.accessor.ecore.EcoreIntrinsicExtender;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.ConsumableResourceComponentType;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.EnumerationParameterType;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.NumericParameterType;
import it.cnr.istc.keen.ddl.ParType;
import it.cnr.istc.keen.ddl.ParameterConstraint;
import it.cnr.istc.keen.ddl.RenewableResourceComponentType;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.TemporalConstraint;
import it.cnr.istc.keen.ddl.TimelineSynchronization;

@SuppressWarnings("restriction")
public class Extender extends EcoreIntrinsicExtender {
	
	private class RuleBuilder
	{
		Map<Map<Class<?>,Integer>,Map<Class<?>,List<Class<?>>>> theRules = new HashMap<>();
		
		public RuleBuilder() {
			theRules = new HashMap<>();
		}
		
		public Map<Map<Class<?>,Integer>,Map<Class<?>,List<Class<?>>>> build() {
			return theRules;
		}
		
		public FWrapper feature(Class<?> cls, int id) {
			Map<Class<?>,Integer> key = Collections.singletonMap(cls, id);
			Map<Class<?>,List<Class<?>>> fm = 
					new HashMap<>();
			theRules.put(key, fm);
			return new FWrapper(fm);
		}
		
		public class FWrapper
		{
			private Map<Class<?>,List<Class<?>>> fm;
			HashSet<Class<?>> resolved = new HashSet<>();
			
			public FWrapper(Map<Class<?>,List<Class<?>>> fm) {
				this.fm = fm;
			}
			
			public FWrapper feature(Class<?> cls, int id) {
				finish();
				return RuleBuilder.this.feature(cls, id);
			}
			
			public Map<Map<Class<?>,Integer>,Map<Class<?>,List<Class<?>>>> build() {
				finish();
				return RuleBuilder.this.build();
			}
			
			public LWrapper element(Class<? extends Object> element) {
				List<Class<? extends Object>> list = new ArrayList<>();
				fm.put(element, list);
				return new LWrapper(list);
			}
			
			protected List<Class<?>> resolveDeps(Class<?> cls, HashSet<Class<?>> resolving)
					throws CyclicResolutionException {
				List<Class<?>> deps = fm.get(cls);
				if (deps == null) {
					deps = Collections.singletonList(cls);
					return deps;
				}
				if (resolved.contains(cls))
					return deps;
				if (resolving.contains(cls))
					throw new CyclicResolutionException(resolving, cls);
				resolving.add(cls);
				try
				{
					ArrayList<Class<?>> result = new ArrayList<>();
					result.add(cls);
					for (Class<?> c : deps) {
						if (c != cls) {
							List<Class<?>> partial = resolveDeps(c,resolving);
							result.addAll(partial);
						}
					}
					deps.clear();
					deps.addAll(result);
					resolved.add(cls);
					return deps;
				}
				finally
				{
					resolving.remove(cls);
				}
			}
			
			protected void finish() {
				for (Class<?> k : fm.keySet())
					resolveDeps(k, new HashSet<>());
			}
			
			public class LWrapper
			{
				private List<Class<?>> list;
				public LWrapper(List<Class<? extends Object>> list) {
					this.list = list;
				}
				
				public FWrapper after(Class<?>... classes) {
					list.addAll(Arrays.asList(classes));
					return FWrapper.this;
				}
			}
		}
	}
		
	private Map<Map<Class<?>,Integer>,Map<Class<?>,List<Class<?>>>> rules;
	
	private void initRules() {
		rules = 
		new RuleBuilder().
			feature(Domain.class,DdlPackage.DOMAIN__ELEMENTS).
				element(NumericParameterType.class).after().
				element(EnumerationParameterType.class).after(NumericParameterType.class).
				element(StateVariableComponentType.class).after(ParType.class).
				element(RenewableResourceComponentType.class).after(StateVariableComponentType.class).
				element(ConsumableResourceComponentType.class).after(RenewableResourceComponentType.class).
				element(Component.class).after(ComponentType.class,ParType.class).
				element(TimelineSynchronization.class).after(Component.class).
			feature(Synchronization.class,DdlPackage.SYNCHRONIZATION__ELEMENTS).
				element(InstantiatedComponentDecision.class).after().
				element(TemporalConstraint.class).after(InstantiatedComponentDecision.class).
				element(ParameterConstraint.class).after(TemporalConstraint.class).
			build();
	}
	
	public Extender() {
		super();
		initRules();
	}
	
	private Map<Class<?>,List<Class<?>>> getRulesForFeature(EObject instance, int id) {
		Class<?> cls = instance.getClass();
		for (Map<Class<?>, Integer> key: rules.keySet())
			if (key.keySet().iterator().next().isAssignableFrom(cls) && key.values().contains(id))
				return rules.get(key);
		return null;
	}
	
	private List<Class<?>> getRulesForClass(Map<Class<?>,List<Class<?>>> map, Class<?> cls) {
		if (map==null)
			return null;
		for (Class<?> c : map.keySet()) {
			if (c.isAssignableFrom(cls))
				return map.get(c);
		}
		return null;
	}
	
	@Override
	public Object eAdd(EObject instance, String name, Object value) {
		EStructuralFeature feat = instance.eClass().getEStructuralFeature(name);
		if (feat == null || !feat.isMany())
			return super.eAdd(instance, name, value);
		int id = instance.eClass().getEAllStructuralFeatures().indexOf(feat);
		if (id < 0)
			return super.eAdd(instance, name, value);
		List<Class<?>> rule = getRulesForClass(getRulesForFeature(instance,id), value.getClass());
		
		if (rule == null)
			return super.eAdd(instance, name, value);
		
		@SuppressWarnings("unchecked")
		EList<Object> list = (EList<Object>)instance.eGet(feat);
		for (Class<?> c : rule)
			for (int i = list.size()-1; i>=0; i--) {
				Object o = list.get(i);
				if (c.isAssignableFrom(o.getClass())) {
					list.add(i+1, value);
					return instance;
				}
			}
		//no rule found! add as first
		list.add(0,value);
		
		return instance;
	}
}
