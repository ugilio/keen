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

import java.util.List;

import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;

public class ValidationResult
{
	private List<ValidationResultElement> list;
	private boolean status;
	
	public ValidationResult(List<ValidationResultElement> list)
	{
		this.list = list;
		status = true;
		for (ValidationResultElement el : list)
			status &= el.getStatus()==StatusType.OK;
	}
	
	public boolean getStatus()
	{
		return status;
	}
	
	public List<ValidationResultElement> getList()
	{
		return list;
	}
}
