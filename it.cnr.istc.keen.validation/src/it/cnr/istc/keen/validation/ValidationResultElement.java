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
package it.cnr.istc.keen.validation;

import it.cnr.istc.mc.langinterface.Domain;
import it.cnr.istc.mc.langinterface.Timeline;

public class ValidationResultElement
{
	public enum StatusType {OK, MAYBE, FAILED, ERR};
	
	public class ResultValue
	{
		public String value;
		public StatusType status;
		public long duration;
		public String output;
		
		public ResultValue(String value, boolean status)
		{
			this.value = value;
			this.status = status ? StatusType.OK : StatusType.FAILED;
		}
		
		public ResultValue(String value, StatusType status)
		{
			this.value = value;
			this.status = status;
		}
		
		public ValidationResultElement getParent()
		{
			return ValidationResultElement.this;
		}
	}
	
	private String componentName;
	private String domainName;
	private ResultValue[] values;
	
	public ValidationResultElement(QueryComponentInfo info)
	{
		componentName = info.timelineName;
		domainName = info.domainName;
		values = info.values.stream().
				map(s -> new ResultValue(s, false)).toArray(ResultValue[]::new);
	}
	
	public ValidationResultElement(Domain domain, Timeline timeline)
	{
		componentName = timeline.getName();
		domainName = domain.getName();
		values = timeline.getComponent().getAllowedValues().stream().
				map(v -> new ResultValue(v.getName(), StatusType.ERR)).toArray(ResultValue[]::new);
	}
	
	public void setStatus(int index, boolean result)
	{
		values[index].status = result ? StatusType.OK : StatusType.FAILED;
	}
	
	public void setStatus(int index, StatusType status, long duration)
	{
		setStatus(index, status, duration, null);
	}
	public void setStatus(int index, StatusType status, long duration, String output)
	{
		values[index].status = status;
		values[index].duration = duration;
		values[index].output = status == StatusType.ERR ? output : "";
	}
	
	public StatusType getStatus()
	{
		StatusType result = StatusType.OK;
		for (ResultValue v : values)
			if (v.status.compareTo(result)>0)
				result=v.status;
		return result;
	}

	public String getComponentName()
	{
	    return componentName;
	}
	
    public String getDomainName()
    {
        return domainName;
    }

	public ResultValue[] getValues()
	{
		return values;
	}
}
