/*
 * Copyright (c) 2010-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   AndreA Orlandini - Initial implementation
 *   Giulio Bernardi  - Adaptation to Keen
 */
 
package it.cnr.istc.mc.translator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.cnr.istc.mc.langinterface.Component;
import it.cnr.istc.mc.langinterface.Domain;
import it.cnr.istc.mc.langinterface.Synchronization;
import it.cnr.istc.mc.langinterface.TemporalConstraint;
import it.cnr.istc.mc.langinterface.TemporalRelationType;
import it.cnr.istc.mc.langinterface.Timeline;
import it.cnr.istc.mc.langinterface.TransitionConstraint;
import it.cnr.istc.mc.langinterface.Value;

/**
 * This class encodes APSI plan and domain into a UPPAAL-TIGA file.
 * This process is composed by several parts. It extracts from domain object
 * components description, Consistency Features and Domain Theory constraints.
 * While it extracts plan information from the timelines processed.
 * 
 * Then produce an XTA file composed by: 
 * - a set of PLAN processes that represents the plan that is to be verified;
 * - a set of STATE VARIABLE processes that represents the free component behaviors 
 * - a set of DOMAIN THEORY MONITOR processes
 * 
 * - a set of specification that are to be tested by SMV.
 * 
 *  @author AndreA Orlandini
 * 	 
 */
public class MCTranslator {
	public static final String MAINCOMPONENT_KEY = "MainComponentKey";
	
	private PrintStream ps;
	private int lineNum; 
	
	private List<Component> comps;
	private List<Timeline> timelines;
	private Domain domain;
	private String basefileName;
	
	private Map<String, ? extends Object> options;
	
	public MCTranslator (Domain dom) throws IOException
	{
		this(dom,null);
	}

	public MCTranslator (Domain dom, Map<String, ? extends Object> options) throws IOException
	{
		if (options == null)
			options = new HashMap<>();
		this.options = options;
		this.domain = dom;
		this.comps = domain.getComponents();
		this.timelines = this.comps.stream().
				flatMap(c -> c.getTimelines().stream()).collect(Collectors.toList());
	}

	private List<String> generateParamNames(int len)
	{
		List<String> params = new ArrayList<>(len);
		for (int i = 1; i <= len; i++)
			params.add("p"+i);
		return params;
	}
	
	private String getDefaultValue(Component comp)
	{
		List<Value> values = comp.getAllowedValues();
		if (values.size()>0)
			return values.get(0).getName();
		return "";
	}
	
	private boolean isMainComponent(Component comp)
	{
		Object obj = options.get(MAINCOMPONENT_KEY);
		if (obj != null)
		{
			if (obj instanceof String)
				return comp.getName().equals(""+obj);
			else if (obj instanceof Collection<?>)
				return ((Collection<?>)obj).contains(comp.getName());
		}
		return false;
	}
	
	private String join(String... str)
	{
		return
		Arrays.stream(str).
		collect(Collectors.joining("_"));
		
		//Camel case:
//		return
//		Arrays.stream(str).
//		map(s -> (s != null && s.length()>0) ? s.substring(0,1).toUpperCase()+s.substring(1) : s).
//		collect(Collectors.joining());
	}
	
	private String normalize(String s)
	{
		return s != null ? s.replaceAll("\\.", "_") : s;
	}
	
	/**
	 * This method creates the main XTA declaration
	 */
	private void writeXTAFlexGeneralDeclarationParam() throws IOException
	{
		println("// Sync Clock channel");
		println();

		// UNIQUE TIME
		println("clock clockPlan;");
		
		// STATE VARIABLE LOCAL CLOCK
		for (Timeline t : timelines)
			if (!isMainComponent(t.getComponent()))
				println("clock clock"+ normalize(t.getName()) +";");
		
		println();
		println("// parameters for values");
		
		// PARAMETERS DEFINITION

		for (Timeline tml : timelines)
		{
			Component comp = tml.getComponent();
			if (isMainComponent(comp))
				continue;
			
			List<Value> values = comp.getAllowedValues();

			if (values.size()>0)
			{
				println("// Timeline : " + tml.getName());
				for (Value value : values)
					for (String par : generateParamNames(value.getParamSize()))
						println("int param_" + join(normalize(tml.getName()),value.getName()) +"_"+par+" = 0;");
				println();
			}
		}
		
		println();
		println("// boolean value to state which are the current component status");

		for (Timeline t : timelines)
		{
			Component comp = t.getComponent();
			String defaultValue = getDefaultValue(comp);
			if (!isMainComponent(comp))
			{
				for (Value value : comp.getAllowedValues())
				{
					boolean initialValue = value.getName().equals(defaultValue);
					
					println("bool " + join(normalize(t.getName()),value.getName())+ " := "+initialValue+";");
				}

				println();
			}
		}
	}


	/**
	 * This method writes the Components Modules 
	 * @throws IOException 
	 */
	private void writeXTAFlexComponentsVal () throws IOException
	{
		println();
		println("// Timelines --");
		println();
		
		for (Timeline t : timelines)
			if (!isMainComponent(t.getComponent()))
				writeAXTAFlexComponentVal(t);
	}

	
	private void writeAXTAFlexComponentVal(Timeline tml) throws IOException
	{
		Component comp = tml.getComponent();
		// Print Component Module
		println("// Timeline Template : " + tml.getName());
		println();
		
		String tmlName = normalize(tml.getName());
		println("process " + tmlName + "() {");
		println();
		
		// Value Variable
		List<Value> values = comp.getAllowedValues();
		List<TransitionConstraint> cf = comp.getTransitionConstraints();
		
		String state = "  state "+
		values.stream().map(new Function<Value,String>() {
			@Override
			public String apply(Value value) {
				String name = value.getName();
				long max = getMax(cf,name);
				if (max != Long.MAX_VALUE) 
					return name+" {clock" + tmlName + " <= " + max + "}";
				return name;
			}
		}).collect(Collectors.joining(", "))+
		";";
		println(state);
		println();

		String name = getDefaultValue(comp);
		
		// Init Value
		println("  init " + name + ";");
		println();
				
		println("trans");
		
		// consistency features
		int valSize = values.size();
		for (int i = 0; i < valSize; i++)
		{
			String valueName = values.get(i).getName();
			List<Value> successors = values.get(i).getSuccessors(cf);
					
			long min = getMin(cf,valueName);
			//long max = getMax(cf,valueName);
			
			int numSucc = successors.size();
			
			// min < t < max the wait for plan to change
			for (int cont = 0; cont < numSucc; cont++)
			{
				Value succ = successors.get(cont);
				String succName = succ.getName();
				String arrow = succ.isUncontrollable() ? " -u-> " : " -> "; 

				boolean last = (i == valSize-1) && (cont == numSucc-1);
				String sep = last ? ";" : ",";

				println("\t" + valueName + arrow + succName + " {guard clock" + tmlName + " >= " + min 
						+ "; assign clock" + tmlName + " := 0, " + boolValue(tml,succName) + "}"+sep); 
			}
			
			println();
		}
		
		println("}");
		println();
	}

	/**
	 * This method writes a Consistency Functions Monitor.
	 * @throws IOException 
	 */
	private void writeXTAFlexMonitor () throws IOException
	{
		println("// Monitor --");
		println();
		println("process monitor() {");
		println("  state OK, ERR;");
		println("  init OK;");
		println();
		println("  trans");
		
		println("    // -- DT -- ");
		
		List<Synchronization> dt = this.domain.getAllSynchronizations();

		// Relation(# clock)
		// AFTER(1), BEFORE(1), CONTAINS(2), EQUALS(0)
		for (int i=0; i < dt.size(); i++)
		{
			Synchronization sync = dt.get(i);				

			for (TemporalConstraint cons : sync.getTemporalConstraints())
			{
				TemporalRelationType type = cons.getType();

				Timeline fromTLtimeline = cons.getFromTimeline();

				String fromTL = normalize(fromTLtimeline.getName());
				String fromVL = cons.getFromValue();
				String toTL = normalize(cons.getToTimeline().getName());
				String toVL = cons.getToValue();

				if (!isMainComponent(fromTLtimeline.getComponent()))
				{							
					// EQUALS
					if (type == TemporalRelationType.EQUALS)
					{
						print("    OK -u-> ERR {guard (clock" + fromTL + " > 0) and (clock" + toTL + " > 0) and " 
								+ " (" + "clock" + fromTL + " == clock" + toTL + ") and " 
								+ " (" + join(fromTL,fromVL) + ") and " + "not (" + join(toTL,toVL) + "); ");
						println(" },");
						println();
					}
					// DURING
					else if (type == TemporalRelationType.DURING)
					{
						print("    OK -u-> ERR {guard (clock" + fromTL + " > 0) and (clock" + toTL 
								+ " > 0) and " + " (" + join(fromTL,fromVL) + ") and " +
								"not (" + join(toTL,toVL) + "); ");

						println(" },");
						println();
					} 
					else if (type == TemporalRelationType.BEFORE)
					{
						// OK -u-> ERRDT {guard (clockPOINTING_MODE >= 1) and (clockDSS_STATIONS >= 1) and (POINTING_MODEEarth_Comm) and (DSS_STATIONSAvailable);  },
						//							print("    OK -u-> ERR {guard (clock" + fromTL + " > 0) and (clock" + toTL 
						//							+ " > 0) and " + " (" + cc(fromTL,fromVL) + ") and " +
						//							"(" + toTL + toVL + "); ");
						//							
						//							println(" },");
						//							println();
					}
					else if (type == TemporalRelationType.CONTAINS)
					{
						print("    OK -u-> ERR {guard (clock" + toTL + " > 0) and (clock" + fromTL 
								+ " > 0) and " + " (" + join(toTL,toVL) + ") and " +
								"not (" + join(fromTL,fromVL) + "); ");

						println(" },");
						println();
					}
				}
			}
		}

		println("    ERR -u-> ERR { };");
		println();

		println("}");

	}


	private void writeXTAFlexSystemDomVal()
	{
		String systemStr = "system "+
		this.timelines.stream().
		filter(t -> !isMainComponent(t.getComponent())).
		map(t -> normalize(t.getName())+", ").collect(Collectors.joining())
		+"monitor;";

		println();
		println(systemStr);
		println();
	}

	private void internalWriteXTAFlexDomValfileParam() throws IOException
	{			
		writeXTAFlexGeneralDeclarationParam();
		writeXTAFlexComponentsVal();
		writeXTAFlexMonitor(); 
		writeXTAFlexSystemDomVal();
	}
	
	public List<QueryComponentInfo> writeXTAFlexDomValfileParam() throws IOException
	{
		basefileName = new File(new File(System.getProperty("java.io.tmpdir")),domain.getName()).getAbsolutePath();
		FileOutputStream xta = new FileOutputStream(basefileName + ".xta");
		FileOutputStream q = new FileOutputStream(basefileName + ".q");
		
		return validateDomain(xta, q);
	}
	
	
	public List<QueryComponentInfo> validateDomain(OutputStream xta, OutputStream q) throws IOException
	{
		newPrintStream(xta);
		try
		{
			internalWriteXTAFlexDomValfileParam();
			newPrintStream(q);
			return writeXTAFlexQueryDomVal();
		}
		finally
		{
			closeStream();
		}
	}
	
	public List<QueryComponentInfoEx> validateDomain(OutputStream xta) throws IOException
	{
		newPrintStream(xta);
		try
		{
			internalWriteXTAFlexDomValfileParam();
			return getDomValQueries();
		}
		finally
		{
			closeStream();
		}
		
	}
	
	private void newPrintStream(OutputStream stream)
	{
		if (this.ps != null)
			ps.close();
		this.ps = new PrintStream( stream );
		lineNum = 1;
	}
	
	private void closeStream()
	{
		if (this.ps != null)
			ps.close();
		ps = null;
	}
	
	private void print(String text)
	{
		ps.print(text);
	}
	
	private void println(String text)
	{
		ps.println(text);
		lineNum++;
	}
	
	private void println()
	{
		println("");
	}
	
	public class QueryComponentInfo
	{
		public Timeline timeline;
		public int startLine;
		private String[] values;
		
		private QueryComponentInfo(int startLine, Timeline timeline)
		{
			this.timeline = timeline;
			this.startLine = startLine;
			List<Value> v = timeline.getComponent().getAllowedValues();
			values = new String[v.size()];
			for (int i = 0; i < v.size(); i++)
				values[i] = v.get(i).getName();
		}
	}

	public class QueryComponentInfoEx
	{
		public Timeline timeline;
		public String query;
		
		private QueryComponentInfoEx(String query, Timeline timeline)
		{
			this.timeline = timeline;
			this.query = query;
		}
	}
	
	private List<QueryComponentInfoEx> getDomValQueries() 
	{
		LinkedList<QueryComponentInfoEx> list = new LinkedList<QueryComponentInfoEx>();

		for (Timeline tml : timelines)
		{
			Component comp = tml.getComponent();
			if (!isMainComponent(comp))
			{
				List<Value> values = comp.getAllowedValues();
				String tmlName = normalize(tml.getName());

				for (Value value: values)
					list.add(
					new QueryComponentInfoEx(
							"E<> (" + tmlName + "." + value.getName() + " and monitor.OK)",
							tml)
					);
			}
		}

		return list;
	}	
	

	private List<QueryComponentInfo> writeXTAFlexQueryDomVal() 
	{
		LinkedList<QueryComponentInfo> list = new LinkedList<QueryComponentInfo>();

		for (Timeline tml : timelines)
		{
			Component comp = tml.getComponent();
			if (!isMainComponent(comp))
			{
				List<Value> values = comp.getAllowedValues();
				String tmlName = normalize(tml.getName());

				list.add(new QueryComponentInfo(lineNum, tml));
				println("// Timeline : " + tml.getName());

				for (Value value: values)
					println("E<> (" + tmlName + "." + value.getName() + " and monitor.OK)");
				
				println();
			}

		}

		return list;
	}

	/**
	 * Get the minimum duration for a value (name) from a vect of consistency features
	 */
	private long getMin(List<TransitionConstraint> vec, String name)
	{	
		for (TransitionConstraint f : vec)
			if (f.hasDuration())
				if (name.equals(f.getValue().getName()))
					return f.getMinimum();
		
		return (long)1;
	}

	/**
	 * Get the maximum duration for a value (name) from a vect of consistency features
	 */
	private long getMax(List<TransitionConstraint> vec, String name)
	{
		for (TransitionConstraint f : vec)
			if (f.hasDuration())
				if (name.equals(f.getValue().getName()))
					return f.getMaximum();
		
		return (long)Long.MAX_VALUE;
	}

	/**
	 *  To produce boolean values for transition
	 * @return
	 */
	private String boolValue(Timeline tml, String name)
	{
		Component comp = tml.getComponent();
		List<Value> values = comp.getAllowedValues();
		String tmlName = normalize(tml.getName());
		
		String res = 
		values.stream().map(new Function<Value,String>() {
			@Override
			public String apply(Value value) {
				String litName = value.getName();
				return join(tmlName,litName) + " := "+(litName.equals(name));
			}
		}).collect(Collectors.joining(", "))+
		"; ";
		
		return res;
	}
}
