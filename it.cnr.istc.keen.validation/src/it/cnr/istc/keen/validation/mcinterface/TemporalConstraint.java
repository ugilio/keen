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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.ComponentDecision;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.SVComponentDecision;
import it.cnr.istc.keen.ddl.TimelineSynchronization;
import it.cnr.istc.mc.langinterface.TemporalRelationType;
import it.cnr.istc.mc.utils.Cache;

public class TemporalConstraint implements it.cnr.istc.mc.langinterface.TemporalConstraint
{
	private it.cnr.istc.keen.ddl.TemporalConstraint cons;
	
	public TemporalConstraint(it.cnr.istc.keen.ddl.TemporalConstraint cons)
	{
		this.cons = cons;
	}
	
	@Override
	public it.cnr.istc.mc.langinterface.Timeline getFromTimeline() {
		it.cnr.istc.keen.ddl.Timeline from = null;
		InstantiatedComponentDecision icd = cons.getFrom();
		if (icd != null)
			from = icd.getTimeline();
		if (from == null)
			from = EcoreUtil2.getContainerOfType(cons, TimelineSynchronization.class).getTimeline();
		return Cache.instance.getFromCache(from, t -> new Timeline(t));
	}

	@Override
	public String getFromValue() {
		ComponentDecision from = null;
		InstantiatedComponentDecision icd = cons.getFrom();
		if (icd != null)
			from = icd.getDecision();
		if (from == null)
			from = EcoreUtil2.getContainerOfType(cons, it.cnr.istc.keen.ddl.Synchronization.class).getValue();
		if (from instanceof SVComponentDecision)
			return ((SVComponentDecision)from).getValue().getName();
		return "";
	}

	@Override
	public Timeline getToTimeline() {
		it.cnr.istc.keen.ddl.Timeline to = cons.getTo().getTimeline();
		return Cache.instance.getFromCache(to, t -> new Timeline(t));
	}

	@Override
	public String getToValue() {
		ComponentDecision to = cons.getTo().getDecision();
		if (to instanceof SVComponentDecision)
			return ((SVComponentDecision)to).getValue().getName();
		return "";
	}

	//FIXME: refactor
	Pattern pattern = Pattern.compile("^(\\w*).*$");
	@Override
	public TemporalRelationType getType() {
		String inType = cons.getRelType();
		Matcher m = pattern.matcher(inType.trim());
		if (m.matches())
			inType = m.group(1);
		if ("EQUALS".equals(inType)) return TemporalRelationType.EQUALS;
		else if ("DURING".equals(inType)) return TemporalRelationType.DURING;
		else if ("BEFORE".equals(inType)) return TemporalRelationType.BEFORE;
		else if ("CONTAINS".equals(inType)) return TemporalRelationType.CONTAINS;
		else return TemporalRelationType.UNKNOWN;
	}
}
