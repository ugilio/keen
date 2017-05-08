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
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import it.cnr.istc.keen.validation.Activator;
import it.cnr.istc.keen.validation.preferences.Preferences;

public class Plan2TigaLauncher
{
	private String baseFileName;
	
	public Plan2TigaLauncher(String baseFileName)
	{
		this.baseFileName = baseFileName;
	}
	
	private ProcessBuilder buildProcessBuilder() throws IOException
	{
		String binPath = Preferences.getString(Preferences.PLAN2TIGAPATH);
		if (binPath.equals(""))
			throw new IOException("Path of plan2tiga executable has not been specified.");
		ProcessBuilder pb = new ProcessBuilder(binPath, baseFileName);
		return pb;
	}
	
	public void execute() throws CoreException
	{
	    IStatus status = null;
	    try
	    {
	        ProcessBuilder pb = buildProcessBuilder();
	        Process p = pb.start();
	        InputStream is = p.getInputStream();
	        String err = "";
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        try
	        {	
	            for (String line = br.readLine(); line != null; line = br.readLine())
	            {
	                System.out.println("PLAN2TIGA output: "+line);
	                err=String.format("%s%n%s",err,line);
	            }
	            if (!err.equals(""))
	                status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"plan2tiga output was: "+err);
	        }
	        finally
	        {
	            br.close();
	        }
	        int retCode = p.waitFor();
	        if (retCode != 0 && status == null)
	            status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"plan2tiga returned exit code "+retCode);
	    }
	    catch (Exception e)
	    {
	        status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
	                "There was an error executing plan2tiga:", e);
	    }
	    if (status != null)
	        throw new CoreException(status);
	}
}
