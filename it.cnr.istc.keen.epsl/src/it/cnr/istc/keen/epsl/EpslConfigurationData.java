package it.cnr.istc.keen.epsl;

@SuppressWarnings("serial")
public class EpslConfigurationData extends ConfigurationData
{
	private static ConfigurationData instance = new EpslConfigurationData();
	
	public static ConfigurationData getInstance()
	{
		return instance;
	}
	
	private EpslConfigurationData() {
		put(HOME_NOTEXIST,"Planner home directory does not exist");
		put(HOME_NOTFOUND,"Planner home directory not specified");
		put(INSTALLED_PLANNERS,"&Installed planners...");
		put(NO_PLANNERS,"No planners defined in workspace");
		put(PLANNER_NOTFOUND,"Unable to resolve EPSL install");
		put(PLANNER_TO_RUN,"Planner to run:");
		put(SPECIFIC_PLANNER,"Specific Planner:");
		
		put(DEFAULT_PLANNER_NOT_FOUND,"Default planner with id \"%s\" not found.");
		put(ENTRY_REMOVED_1,"Planner entry %s was removed because it has no id.");
		put(ENTRY_REMOVED_2,"Planner entry %s was removed because it has no name.");
		put(ENTRY_REMOVED_3,"Planner entry %s was removed because it has no installation path.");
		put(INSTALLED_PLANNERS2,"Installed EPSL Planners");
		
		put(ADD_REMOVE_EDIT,"Add, remove or edit planner definitions. By default, the checked planner is used for the newly created DDL projects.");
		put(AT_LEAST_ONE_PLANNER,"You must provide at least one planner to act as the workspace default");
		put(DEFAULT_NEEDED,"You must select a default planner for the workspace");
		
		put(PLANNER_ARGUMENTS,"Planner &arguments:");
		
		put(NO_PLANNER_FOUND,"No EPSL planner found!");
		
		put(NO_RESOURCES,"There are no resources to run the planner on");
		
		put(EXEC_ERROR,"An EPSL problem occurred. %nPlanner said: %n%s");

		put(PROJECT_PLANNER,"&Project planner (%s)");
		put(WORKSPACE_DEFAULT_PLANNER,"Workspace &default planner (%s)");
		
		put(ENTER_NAME,	"Enter a name for the planner.");
		put(NAME_USED, "The planner name is already in use.");
		put(INVALID_FNAME,"Planner name must be a valid file name: ");
		
		put(PAGENAME,"Add EPSL Installation");
		put(PLANNER_HOME,"Planner home:");
		put(PLANNER_NAME,"Planner &name:");
		put(ROOT_DIR,"Select the root directory of the EPSL installation:");
		put(HOME_DIR,"Enter the home directory of the EPSL installation.");
		put(DEFAULT,"Default EPSL planner");
		put(DEFINITION,"EPSL Definition");
		put(ATTRIBUTES,"Specify attributes for a EPSL installation");
	}

	@Override
	public boolean needsPdl() {
		return true;
	}

	@Override
	public boolean needsClosePlanner() {
		return true;
	}

	@Override
	public BaseEpslRegistry getEpslRegistry() {
		return EpslRegistry.getInstance();
	}

}
