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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;
import it.cnr.istc.keen.validation.preferences.Preferences;
import it.cnr.istc.mc.langinterface.Domain;
import it.cnr.istc.mc.langinterface.Timeline;
import it.cnr.istc.mc.translator.MCTranslator.QueryComponentInfoEx;

public class TigaLauncher
{
	private static String regularOptions = "-s";
	private static String approxOptions = "-s -A";
	
	private String baseFileName;
	private Domain domain;
	private List<QueryComponentInfoEx> queries;
	private IProgressMonitor monitor;
	private String binPath;
	
	public TigaLauncher(String baseFileName, Domain domain, List<QueryComponentInfoEx> queries, IProgressMonitor monitor)
	{
		this.baseFileName = baseFileName;
		this.domain = domain;
		if (queries==null)
		    queries=new ArrayList<QueryComponentInfoEx>();
		this.queries = queries;
		this.monitor = SubMonitor.convert(monitor, queries.size());
	}
	
	private ProcessBuilder buildProcessBuilder(String query, boolean relax) throws IOException
	{
		if (relax)
			return buildProcessBuilder(query, approxOptions);			
		else
			return buildProcessBuilder(query, regularOptions);
	}
	
	private ProcessBuilder buildProcessBuilder(String query, String options) throws IOException
	{
		File qFile = new File(baseFileName+".q");
		try(FileWriter w = new FileWriter(qFile))
		{
			qFile.deleteOnExit();
			w.write(String.format("%s%n", query));
		}
		ProcessBuilder pb = new ProcessBuilder(binPath, options,
				baseFileName+".xta", baseFileName+".q");
		return pb;
	}
	
	public ValidationResult execute() throws CoreException
	{
	    IStatus status = null;
		List<ValidationResultElement> result = new ArrayList<ValidationResultElement>(queries.size());

		try
		{
			binPath = Preferences.getString(Preferences.VERIFYTGAPATH);
			if (binPath.equals(""))
				throw new IOException("Path of verifytga executable has not been specified.");
			
			long timeout = Preferences.getLong(Preferences.VALIDATIONTIMEOUT);
			Timeline curTml = null;
	        ValidationResultElement resultElement = null;
			int valueIdx = 0;
			for (QueryComponentInfoEx q : queries)
			{
				if (monitor.isCanceled())
					break;
				if (q.timeline != curTml)
				{
					curTml = q.timeline;
					valueIdx = 0;
					resultElement = new ValidationResultElement(domain,curTml);
					result.add(resultElement);
				}
				Process p = null;
				boolean terminated = false;
				long elapsed = 0;
				long start = System.currentTimeMillis();
				for (int retry = 0; retry < 2; retry++)
				{
					if (monitor.isCanceled())
						break;
					ProcessBuilder pb = buildProcessBuilder(q.query,retry>0);
					p = pb.start();
					terminated = false;
					try
					{
						long toWait = TimeUnit.SECONDS.toMillis(timeout);
						long endTime = System.currentTimeMillis()+toWait;
						while (!terminated && toWait>0)
						{
							terminated=p.waitFor(Math.min(toWait, 100),TimeUnit.MILLISECONDS);
							toWait=endTime-System.currentTimeMillis();
							if (monitor.isCanceled())
								break;
						}
					}
					catch (InterruptedException e1) {}
					elapsed = System.currentTimeMillis()-start;
					if (terminated)
						break;
					if (!terminated)
					{
						p.destroyForcibly();
						if (retry>0)
							resultElement.setStatus(valueIdx, StatusType.ERR, elapsed);
					}
				}
			    if (terminated)
			    {
				    try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())))
				    {
				        String err = "";
				        for (String line = br.readLine(); line != null; line = br.readLine())
				        {
				            //System.out.println("TIGA output: "+line);
				            err=String.format("%s%n%s",err,line);

				            if (!line.startsWith("Verifying property "))
				                continue;
				            err="";
				            line = br.readLine();
				            StatusType propStatus = StatusType.ERR;
				            if (" -- Property is satisfied.".equals(line))
				            	propStatus = StatusType.OK;
				            else if (" -- Property MAY be satisfied.".equals(line))
				            	propStatus = StatusType.MAYBE;
				            else if (" -- Property is NOT satisfied.".equals(line))
				            	propStatus = StatusType.FAILED;
				            resultElement.setStatus(valueIdx, propStatus, elapsed);

				            //System.out.println(q.timeline.getName()+": "+propStatus.name());
				        }
				        if (!err.replaceAll("\\s", "").equals(""))
			                status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"verifytga output was: "+err);
				    }
				}
			    else
			    	resultElement.setStatus(valueIdx, StatusType.ERR, elapsed);
			    valueIdx++;
			    monitor.worked(1);
			}
		}
		catch (IOException e)
		{
			status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
					"There was an error executing verifytga:", e);
		}			    	

        if (status != null)
            throw new CoreException(status);
		return new ValidationResult(result);
	}
}
