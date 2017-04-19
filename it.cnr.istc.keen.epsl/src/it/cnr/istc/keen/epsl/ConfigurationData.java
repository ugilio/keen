package it.cnr.istc.keen.epsl;

import java.util.HashMap;

@SuppressWarnings("serial")
public abstract class ConfigurationData extends HashMap<String,String>
{
	public static final String HOME_NOTEXIST = "HOME_NOTEXIST";
	public static final String HOME_NOTFOUND = "HOME_NOTFOUND";
	public static final String INSTALLED_PLANNERS = "INSTALLED_PLANNERS";
	public static final String NO_PLANNERS = "NO_PLANNERS";
	public static final String PLANNER_NOTFOUND = "PLANNER_NOTFOUND";
	public static final String PLANNER_TO_RUN = "PLANNER_TO_RUN";
	public static final String SPECIFIC_PLANNER = "SPECIFIC_PLANNER";
	
	public static final String DEFAULT_PLANNER_NOT_FOUND = "DEFAULT_PLANNER_NOT_FOUND";
	public static final String ENTRY_REMOVED_1 = "ENTRY_REMOVED_1";
	public static final String ENTRY_REMOVED_2 = "ENTRY_REMOVED_2";
	public static final String ENTRY_REMOVED_3 = "ENTRY_REMOVED_3";
	public static final String INSTALLED_PLANNERS2 = "INSTALLED_PLANNERS";
	
	public static final String ADD_REMOVE_EDIT = "ADD_REMOVE_EDIT";
	public static final String AT_LEAST_ONE_PLANNER = "AT_LEAST_ONE_PLANNER";
	public static final String DEFAULT_NEEDED = "DEFAULT_NEEDED";
	
	public static final String PLANNER_ARGUMENTS = "PLANNER_ARGUMENTS";
	
	public static final String NO_PLANNER_FOUND = "NO_PLANNER_FOUND";
	
	public static final String NO_RESOURCES = "NO_RESOURCES";
	
	public static final String EXEC_ERROR = "EXEC_ERROR";
	
	public static final String PROJECT_PLANNER="PROJECT_PLANNER";
	public static final String WORKSPACE_DEFAULT_PLANNER="WORKSPACE_DEFAULT_PLANNER";
	
	public static final String PAGENAME = "PAGENAME";
	public static final String PLANNER_HOME = "PLANNER_HOME";
	public static final String PLANNER_NAME = "PLANNER_NAME";
	public static final String ROOT_DIR = "ROOT_DIR";
	public static final String HOME_DIR = "HOME_DIR";
	public static final String DEFAULT = "DEFAULT";
	public static final String DEFINITION = "DEFINITION";
	public static final String ATTRIBUTES = "ATTRIBUTES";
	public static final String ENTER_NAME = "ENTER_NAME";
	public static final String NAME_USED = "NAME_USED"; 
	public static final String INVALID_FNAME = "INVALID_FNAME";
	
	public abstract boolean needsPdl();
	public abstract boolean needsClosePlanner();
	
	public abstract BaseEpslRegistry getEpslRegistry();
}
