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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;
import it.cnr.istc.keen.validation.preferences.Preferences;
import it.cnr.istc.mc.translator.MCTranslator.QueryComponentInfoEx;

public abstract class BaseTigaLauncher
{
	protected static String regularOptions = "-s";
	protected static String approxOptions = "-s -A";
	
	protected String baseFileName;
	protected IProgressMonitor monitor;
	protected String binPath;
	protected long timeout;
	
	public BaseTigaLauncher(String baseFileName, List<QueryComponentInfoEx> queries, IProgressMonitor monitor)
	{
		this.baseFileName = baseFileName;
		int count = queries==null ? 1 : queries.size();
		this.monitor = SubMonitor.convert(monitor, count);
	}
	
	protected ProcessBuilder buildProcessBuilder(String query, boolean relax) throws IOException
	{
		if (relax)
			return buildProcessBuilder(query, approxOptions);			
		else
			return buildProcessBuilder(query, regularOptions);
	}
	
	protected ProcessBuilder buildProcessBuilder(String query, String options) throws IOException
	{
		ProcessBuilder pb = new ProcessBuilder(binPath, options,
				baseFileName+".xta", baseFileName+".q");
		pb.redirectErrorStream(true);
		return pb;
	}
	
	protected void getBinPath() throws IOException
	{
		binPath = Preferences.getString(Preferences.VERIFYTGAPATH);
		if (binPath.equals(""))
			throw new IOException("Path of verifytga executable has not been specified.");
		timeout = Preferences.getLong(Preferences.VALIDATIONTIMEOUT);
	}
	
	protected boolean executeQuery(String query, Process proc[]) throws IOException
	{
		Process p = null;
		boolean terminated = false;
		for (int retry = 0; retry < 2; retry++)
		{
			if (monitor.isCanceled())
				break;
			ProcessBuilder pb = buildProcessBuilder(query,retry>0);
			p = pb.start();
			proc[0] = p;
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
			if (terminated)
				break;
			if (!terminated)
				p.destroyForcibly();
		}
		return terminated;
	}
	
	protected StatusType readProperty(BufferedReader br, StringBuilder output, boolean end[], String err[]) throws IOException
	{
		err[0] = "";
		end[0]=false;
		for (String line = br.readLine(); line != null; line = br.readLine())
		{
			output.append(String.format("%s%n", line));
			//System.out.println("TIGA output: "+line);
			err[0]=String.format("%s%n%s",err[0],line);

			if (!line.startsWith("Verifying property "))
				continue;
			err[0]="";
			line = br.readLine();
			output.append(String.format("%s%n", line));
			StatusType propStatus = StatusType.ERR;
			if (" -- Property is satisfied.".equals(line))
				propStatus = StatusType.OK;
			else if (" -- Property MAY be satisfied.".equals(line))
				propStatus = StatusType.MAYBE;
			else if (" -- Property is NOT satisfied.".equals(line))
				propStatus = StatusType.FAILED;
			return propStatus;
		}
		end[0]=true;
		return StatusType.OK;
	}
	
}
