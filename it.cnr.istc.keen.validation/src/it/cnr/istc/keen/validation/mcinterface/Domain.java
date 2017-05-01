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

import java.util.List;
import java.util.stream.Collectors;

import it.cnr.istc.keen.ddl.TimelineSynchronization;
import it.cnr.istc.mc.langinterface.Component;
import it.cnr.istc.mc.langinterface.Synchronization;

public class Domain implements it.cnr.istc.mc.langinterface.Domain {
	
	private it.cnr.istc.keen.ddl.Domain domain;
	
	public Domain(it.cnr.istc.keen.ddl.Domain domain)
	{
		this.domain = domain;
	}
	
	@Override
	public List<Synchronization> getAllSynchronizations()
	{
		return
		domain.getElements().stream().filter(e -> e instanceof TimelineSynchronization).
		map(t -> (TimelineSynchronization)t).
		flatMap(ts -> ts.getSynchronizations().stream()).
		map(s -> new it.cnr.istc.keen.validation.mcinterface.Synchronization(s)).
		collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return domain.getName();
	}

	@Override
	public List<Component> getComponents() {
		return
		domain.getElements().stream().filter(e -> e instanceof it.cnr.istc.keen.ddl.Component).
		map(c -> (new it.cnr.istc.keen.validation.mcinterface.Component((it.cnr.istc.keen.ddl.Component)c))).
		collect(Collectors.toList());
	}
}
