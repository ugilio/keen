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
package it.cnr.istc.keen.validation.singlelaunch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import it.cnr.istc.keen.validation.Activator;
import it.cnr.istc.keen.validation.QueryComponentInfo;
import it.cnr.istc.keen.validation.ValidationResult;
import it.cnr.istc.keen.validation.ValidationResultElement;
import it.cnr.istc.keen.validation.preferences.Preferences;

public class TigaLauncher
{
	private String options = "-s";
	
	private String baseFileName;
	private List<QueryComponentInfo> components;
	
	public TigaLauncher(String baseFileName, List<QueryComponentInfo> components)
	{
		this.baseFileName = baseFileName;
		if (components==null)
		{
		    components=new ArrayList<QueryComponentInfo>();
		    QueryComponentInfo q = new QueryComponentInfo(1, "Unnamed", "");
		    components.add(q);
		}
		this.components = components;
	}
	
	private ProcessBuilder buildProcessBuilder() throws IOException
	{
		String binPath = Preferences.getString(Preferences.VERIFYTGAPATH);
		if (binPath.equals(""))
			throw new IOException("Path of verifytga executable has not been specified.");
		ProcessBuilder pb = new ProcessBuilder(binPath, options,
				baseFileName+".xta", baseFileName+".q");
		return pb;
	}
	
	/*
	 * Note: separate each property in a string, create a temp file for each.
	 */
	
	public ValidationResult execute() throws CoreException
	{
	    IStatus status = null;
		List<ValidationResultElement> result = new ArrayList<ValidationResultElement>(components.size());

		try
		{
		    ProcessBuilder pb = buildProcessBuilder();
		    Process p = pb.start();
		    try
		    {
				p.waitFor();
			} catch (InterruptedException e1) {}
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    //CustomBufferedReader br = new CustomBufferedReader(p);

		    try
		    {
		        Iterator<QueryComponentInfo> cit = components.iterator();
		        ValidationResultElement resultElement = null;
		        int valueIdx = 0;
		        QueryComponentInfo prev = cit.hasNext() ? cit.next() : null;
		        QueryComponentInfo next = cit.hasNext() ? cit.next() : null;
		        String err = "";
		        for (String line = br.readLine(); line != null; line = br.readLine())
		        {
		            System.out.println("TIGA output: "+line);
		            err=String.format("%s%n%s",err,line);

		            if (prev == null)
		                break;
		            if (!line.startsWith("Verifying property "))
		                continue;
		            //if we are here there were no parsing errors till now
		            err="";
		            int spacePos = line.lastIndexOf(' ');
		            int lineNum = 0;
		            try
		            {
		                lineNum = Integer.parseInt(line.substring(spacePos+1));
		            }
		            catch (NumberFormatException e) {}
		            if (lineNum <= 0)
		                continue;
		            line = br.readLine();
		            boolean ok = " -- Property is satisfied.".equals(line);
		            while (next!=null && next.startLine<=lineNum)
		            {
		                prev = next;
		                next = cit.hasNext() ? cit.next() : null;
		                valueIdx = 0;
		                resultElement = null;
		            }
		            if (resultElement == null)
		            {
		                resultElement = new ValidationResultElement(prev);
		                result.add(resultElement);
		            }
		            resultElement.setStatus(valueIdx, ok);
		            valueIdx++;

		            String res = ok ? "ok" : "FAILED";
		            System.out.println(prev.timelineName+": line "+lineNum+", "+res);
		        }
		        if (!err.replaceAll("\\s", "").equals(""))
	                status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,"verifytga output was: "+err);
		    }
		    finally
		    {
		        br.close();
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
