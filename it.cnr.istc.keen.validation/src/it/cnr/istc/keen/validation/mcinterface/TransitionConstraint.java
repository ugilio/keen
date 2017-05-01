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

import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.mc.utils.Cache;

public class TransitionConstraint implements it.cnr.istc.mc.langinterface.TransitionConstraint {
	private it.cnr.istc.keen.ddl.TransitionConstraint cons;
	private static Value nullValue = new Value(null);

	public TransitionConstraint(it.cnr.istc.keen.ddl.TransitionConstraint cons) {
		this.cons = cons;
	}

	@Override
	public Value getValue() {
		ComponentDecisionType value = cons.getValue().getValue();
		
		return Cache.instance.getFromCache(value, v -> new Value(v));
	}

	@Override
	public boolean hasDuration() {
		return pattern.matcher(cons.getRange()).matches();
	}
	
	@Override
	public boolean isTransitionElement() {
		return false;
	}
	
	//FIXME: refactor
	private static Pattern pattern = Pattern.compile("\\[\\s*(%d+)\\s*,\\s*(%d+)\\s*\\]");
	private long[] parseRange(String str)
	{
		if (str==null)
			str="";
		str=str.trim();
		Matcher m = pattern.matcher(str);
		try
		{
			if (m.matches())
			{
				long lb = Long.parseLong(m.group(1));
				long ub = Long.parseLong(m.group(2));
				return new long[]{lb,ub};
			}
		}
		catch (NumberFormatException e) {};
		return new long[]{0,0};
	}

	@Override
	public long getMinimum() {
		return parseRange(cons.getRange())[0];
	}

	@Override
	public long getMaximum() {
		return parseRange(cons.getRange())[1];
	}

	@Override
	public it.cnr.istc.mc.langinterface.Value getTo() {
		return nullValue;
	}
}
