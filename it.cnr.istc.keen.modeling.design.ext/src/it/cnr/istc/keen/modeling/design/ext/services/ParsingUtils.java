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
package it.cnr.istc.keen.modeling.design.ext.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;

import it.cnr.istc.keen.ddl.URange;

public class ParsingUtils {
	
    private static String[] internalParseRange(String arg) {
    	//aql:arg0.replace('^\\s*\\[?\\s*(\\d+)\\s*,(\\d+)\\s*\\]?\\s*$','$1')
    	
    	Matcher m =  Pattern.compile("^\\s*\\[?\\s*([^,\\s]+)\\s*,\\s*([^,\\s\\]]+)\\s*\\]?\\s*$").matcher(arg);
    	if (!m.matches())
    		throw new NumberFormatException("Invalid range");
    	String lb = m.replaceAll("$1");
    	String ub = m.replaceAll("$2");
    	//this throws a NumberFormatException if it is not valid
    	parseNum(lb);
    	parseNum(ub);
    	return new String[]{lb,ub};
    }
    
    public static URange parseURange(URange self, String arg) {
    	String range[] = internalParseRange(arg);
    	for (String s : range)
    		if (parseNum(s)<0)
    			throw new NumberFormatException("Number in URange must not be negative");
    	self.setLb(range[0]);
    	self.setUb(range[1]);
    	return self;
    }
    
    public static String parseRange(String arg, String orig) {
    	try
    	{
    		String range[] = internalParseRange(arg);
    		return "["+range[0]+", "+range[1]+"]";
    	}
    	catch (Exception e)
    	{
    		return orig;
    	}
    }
    
    public static String checkNumber(EObject self, String arg, String orig) {
    	try
    	{
    		return numToStr(parseNum(arg));
    	}
    	catch (Exception e)
    	{
    		return orig;
    	}
    }
    
    public static String checkUNumber(EObject self, String arg, String orig) {
    	try
    	{
    		Integer num = parseNum(arg);
    		if (num<0)
    			throw new NumberFormatException("Number must not be negative");
    		return numToStr(num);
    	}
    	catch (Exception e)
    	{
    		return orig;
    	}
    }
    
    //aql:arg0.replace('([^:]*: )?(\\d+)','$2')
    public static String parseProperty(String arg) {
    	return arg.replaceAll("^([^:]*:)?\\s*(.+)\\s*$", "$2").trim();
    }
    
    public static Integer parseNumProp(String arg) {
    	return parseNum(parseProperty(arg));
    }
    
    protected static Integer parseNum(String arg) {
    	if (arg.matches("(\\+\\s*)?INF"))
    		return Integer.MAX_VALUE;
    	else if (arg.matches("\\-\\s*INF"))
    		return Integer.MIN_VALUE;
    	return Integer.parseInt(arg);
    }
    
    protected static String numToStr(Integer i) {
    	if (i==Integer.MAX_VALUE)
    		return "+INF";
    	else if (i==Integer.MIN_VALUE)
    		return "-INF";
    	return ""+i;
    }
    
    public static String parseFunctionName(EObject self, String args) {
    	//aql:arg0.replace('([^(]*).*','$1').trim()
    	return args.replaceAll("([^(]*).*","$1").trim();
    }
}
