package it.cnr.istc.keen.fbt;

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;

@SuppressWarnings("serial")
public class FbtExecConfigurationData extends ConfigurationData {

	private static ConfigurationData instance = new FbtExecConfigurationData();
	
	public static ConfigurationData getInstance()
	{
		return instance;
	}
	
	private FbtExecConfigurationData() {
		put(HOME_NOTEXIST,"Executor home directory does not exist");
		put(HOME_NOTFOUND,"Executor home directory not specified");
		put(INSTALLED_PLANNERS,"&Installed executors...");
		put(NO_PLANNERS,"No executors defined in workspace");
		put(PLANNER_NOTFOUND,"Unable to resolve executor install");
		put(PLANNER_TO_RUN,"Executor to run:");
		put(SPECIFIC_PLANNER,"Specific Executor:");
		
		put(DEFAULT_PLANNER_NOT_FOUND,"Default executor with id \"%s\" not found.");
		put(ENTRY_REMOVED_1,"Executor entry %s was removed because it has no id.");
		put(ENTRY_REMOVED_2,"Executor entry %s was removed because it has no name.");
		put(ENTRY_REMOVED_3,"Executor entry %s was removed because it has no installation path.");
		put(INSTALLED_PLANNERS2,"Installed Fbt Executors");
		
		put(ADD_REMOVE_EDIT,"Add, remove or edit executor definitions. By default, the checked executor is used for the newly created DDL projects.");
		put(AT_LEAST_ONE_PLANNER,"You must provide at least one executor to act as the workspace default");
		put(DEFAULT_NEEDED,"You must select a default executor for the workspace");
		
		put(PLANNER_ARGUMENTS,"Executor &arguments:");
		
		put(NO_PLANNER_FOUND,"No Fbt executor found!");
		
		put(NO_RESOURCES,"There are no resources to run the executor on");
		
		put(EXEC_ERROR,"An internal error occurred. %nExecutor said: %n%s");
		
		put(PROJECT_PLANNER,"&Project executor (%s)");
		put(WORKSPACE_DEFAULT_PLANNER,"Workspace &default executor (%s)");
		
		put(ENTER_NAME,	"Enter a name for the executor.");
		put(NAME_USED, "The executor name is already in use.");
		put(INVALID_FNAME,"Executor name must be a valid file name: ");
		
		put(PAGENAME,"Add Fbt Executor Installation");
		put(PLANNER_HOME,"Executor home:");
		put(PLANNER_NAME,"Executor &name:");
		put(ROOT_DIR,"Select the root directory of the Fbt executor installation:");
		put(HOME_DIR,"Enter the home directory of the Fbt executor installation.");
		put(DEFAULT,"Default Fbt executor");
		put(DEFINITION,"Executor Definition");
		put(ATTRIBUTES,"Specify attributes for a Fbt executor installation");
	}

	@Override
	public boolean needsPdl() {
		return false;
	}

	@Override
	public boolean needsClosePlanner() {
		return false;
	}

	@Override
	public BaseEpslRegistry getEpslRegistry() {
		return FbtExecRegistry.getInstance();
	}

}
