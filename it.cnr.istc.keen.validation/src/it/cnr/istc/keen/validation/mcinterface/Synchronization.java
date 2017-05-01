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

public class Synchronization implements it.cnr.istc.mc.langinterface.Synchronization {
	
	private it.cnr.istc.keen.ddl.Synchronization sync;
	
	public Synchronization(it.cnr.istc.keen.ddl.Synchronization sync)
	{
		this.sync = sync;
	}
	
	public List<it.cnr.istc.mc.langinterface.TemporalConstraint> getTemporalConstraints()
	{
		return
		sync.getElements().stream().filter(e -> e instanceof it.cnr.istc.keen.ddl.TemporalConstraint).
		map(e -> (it.cnr.istc.keen.ddl.TemporalConstraint)e).
		map(t -> new TemporalConstraint(t)).
		collect(Collectors.toList());
	}
}
