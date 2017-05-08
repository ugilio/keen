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
package it.cnr.istc.keen.verification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import it.cnr.istc.keen.validation.Activator;
import it.cnr.istc.keen.validation.BaseTigaLauncher;
import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;

public class VerificationTigaLauncher extends BaseTigaLauncher
{
	public VerificationTigaLauncher(String baseFileName, IProgressMonitor monitor)
	{
		super(baseFileName,null,monitor);
	}
	
	public StatusType execute() throws CoreException
	{
	    IStatus status = null;

		try
		{
			getBinPath();
			
			if (monitor.isCanceled())
				return StatusType.ERR;
			Process[] p = new Process[1];
			boolean terminated = executeQuery(null,p);
			if (!terminated)
				return StatusType.ERR;
			try(BufferedReader br = new BufferedReader(new InputStreamReader(p[0].getInputStream())))
			{
				StringBuilder output = new StringBuilder(80*30);
				String err[] = new String[1];
				StatusType result = readProperty(br, output, new boolean[1], err);
				if (!err[0].replaceAll("\\s", "").equals(""))
					status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"verifytga output was: "+err[0]);
				monitor.worked(1);
				return result;
			}
		}
		catch (IOException e)
		{
			status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
					"There was an error executing verifytga:", e);
			throw new CoreException(status);
		}			    	
	}
	
}
