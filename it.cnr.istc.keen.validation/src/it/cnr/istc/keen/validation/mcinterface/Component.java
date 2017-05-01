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
import java.util.stream.Collectors;

import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.mc.langinterface.Timeline;
import it.cnr.istc.mc.langinterface.TransitionConstraint;
import it.cnr.istc.mc.langinterface.Value;
import it.cnr.istc.mc.utils.Cache;

public class Component implements it.cnr.istc.mc.langinterface.Component {

	private it.cnr.istc.keen.ddl.Component comp;

	public Component(it.cnr.istc.keen.ddl.Component comp) {
		this.comp = comp;
	}

	@Override
	public String getName()
	{
		return comp.getName();
	}

	@Override
	public List<Value> getAllowedValues() {
		ComponentType ct = comp.getType();
		if (ct instanceof StateVariableComponentType)
		{
			List<it.cnr.istc.keen.ddl.ComponentDecisionType> decs =
					((StateVariableComponentType) ct).getDecTypes();
			return
					Cache.instance.getFromCache(decs, arr -> arr.stream().
							map(d -> new it.cnr.istc.keen.validation.mcinterface.Value(d)).
							collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}

	@Override
	public List<TransitionConstraint> getTransitionConstraints() {
		ComponentType ct = comp.getType();
		if (ct instanceof StateVariableComponentType)
		{
			List<it.cnr.istc.keen.ddl.TransitionConstraint> constr =
					((StateVariableComponentType) ct).getTransConstraint();
			return
					Cache.instance.getFromCache(constr, arr -> arr.stream().
							map(t -> new it.cnr.istc.keen.validation.mcinterface.TransitionConstraint(t)).
							collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}

	@Override
	public List<Timeline> getTimelines() {
		List<it.cnr.istc.keen.ddl.Timeline> timelines = comp.getTimelines();
		return Cache.instance.getFromCache(timelines, tml -> tml.stream().
				map(t -> new it.cnr.istc.keen.validation.mcinterface.Timeline(t))
				.collect(Collectors.toList()));
	}

}
