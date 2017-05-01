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

import it.cnr.istc.mc.utils.Cache;

public class Timeline implements it.cnr.istc.mc.langinterface.Timeline
{
	private it.cnr.istc.keen.ddl.Timeline timeline;

	public Timeline(it.cnr.istc.keen.ddl.Timeline timeline)
	{
		this.timeline = timeline;
	}

	@Override
	public String getName()
	{
		return getComponent().getName()+"."+timeline.getName();
	}

	@Override
	public Component getComponent() {
		it.cnr.istc.keen.ddl.Component comp = (it.cnr.istc.keen.ddl.Component)timeline.eContainer();
		return Cache.instance.getFromCache(comp, c -> new Component(c));
	}
}
