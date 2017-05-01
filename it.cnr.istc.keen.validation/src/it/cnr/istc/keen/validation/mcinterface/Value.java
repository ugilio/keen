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
package it.cnr.istc.keen.validation.mcinterface;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.mc.langinterface.TransitionConstraint;
import it.cnr.istc.mc.utils.Cache;

public class Value implements it.cnr.istc.mc.langinterface.Value {
	private it.cnr.istc.keen.ddl.ComponentDecisionType value;
	
	public Value(it.cnr.istc.keen.ddl.ComponentDecisionType value) {
		this.value = value;
	}

	@Override
	public String getName() {
		return value.getName();
	}

	@Override
	public int getParamSize() {
		return value.getArgs().size();
	}
	
	private it.cnr.istc.keen.ddl.TransitionConstraint getTransitionConstraint()
	{
		String name = getName();
		ComponentType ct = (ComponentType)value.eContainer();
		if (ct instanceof StateVariableComponentType)
		{
			Optional<it.cnr.istc.keen.ddl.TransitionConstraint> tc =
			((StateVariableComponentType)ct).getTransConstraint().stream().
			filter(t -> t.getValue().getValue().getName().equals(name)).
			findAny();
			if (tc.isPresent())
				return tc.get();
		}
		return null;
	}
	
	@Override
	public boolean isUncontrollable()
	{
		it.cnr.istc.keen.ddl.TransitionConstraint tc = getTransitionConstraint();
		if (tc != null)
		{
			return
					tc.getValue().getParams().stream().
					filter(p -> p.getName().equals("u")).
					findAny().isPresent();
		}
		return false;
	}

	@Override
	public List<it.cnr.istc.mc.langinterface.Value> getSuccessors(List<TransitionConstraint> constraints) {
		String name = getName();
		ComponentType ct = (ComponentType)value.eContainer();
		if (ct instanceof StateVariableComponentType)
		{
			return
			((StateVariableComponentType)ct).getTransConstraint().stream().
			filter(t -> t.getValue().getValue().getName().equals(name)).
			flatMap(t -> t.getMeets().stream()).
			filter(te -> te instanceof SVComponentDecision).
			map(te -> ((SVComponentDecision)te).getValue()).
			map(cdt -> Cache.instance.getFromCache(cdt, c -> new Value(c))).
			collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
}
