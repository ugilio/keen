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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;
import it.cnr.istc.mc.langinterface.Domain;
import it.cnr.istc.mc.langinterface.Timeline;
import it.cnr.istc.mc.translator.MCTranslator.QueryComponentInfoEx;

public class TigaLauncher extends BaseTigaLauncher
{
	private Domain domain;
	private List<QueryComponentInfoEx> queries;
	
	public TigaLauncher(String baseFileName, Domain domain, List<QueryComponentInfoEx> queries, IProgressMonitor monitor)
	{
		super(baseFileName,queries,monitor);
		this.domain = domain;
		if (queries==null)
		    queries=new ArrayList<QueryComponentInfoEx>();
		this.queries = queries;
	}
	
	@Override
	protected ProcessBuilder buildProcessBuilder(String query, String options) throws IOException
	{
		File qFile = new File(baseFileName+".q");
		try(FileWriter w = new FileWriter(qFile))
		{
			qFile.deleteOnExit();
			w.write(String.format("%s%n", query));
		}
		return super.buildProcessBuilder(query, options);
	}
	
	public ValidationResult execute() throws CoreException
	{
	    IStatus status = null;
		List<ValidationResultElement> result = new ArrayList<ValidationResultElement>(queries.size());

		try
		{
			getBinPath();
			
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
				long elapsed = 0;
				Process[] p = new Process[1];
				long start = System.currentTimeMillis();
				boolean terminated = executeQuery(q.query,p);
				elapsed = System.currentTimeMillis()-start;
			    if (terminated)
			    {
				    try(BufferedReader br = new BufferedReader(new InputStreamReader(p[0].getInputStream())))
				    {
				    	StringBuilder output = new StringBuilder(80*30);
				        String err[] = new String[1];
				        boolean eof[] = new boolean[1];
				        eof[0] = false;
				        while (true)
				        {
				        	StatusType propStatus = readProperty(br, output, eof, err);
				        	if (eof[0])
				        		break;
				        	resultElement.setStatus(valueIdx, propStatus, elapsed, output.toString());
				        }
				        if (!err[0].replaceAll("\\s", "").equals(""))
			                status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"verifytga output was: "+err[0]);
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
