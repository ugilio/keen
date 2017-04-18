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
package it.cnr.istc.keen.ui.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class DdlNature implements IProjectNature {

	public static String ID = "it.cnr.istc.keen.ui.DdlNature";
	
	private IProject project;

	@Override
	public void configure() throws CoreException
	{

	}

	@Override
	public void deconfigure() throws CoreException
	{

	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}

}
