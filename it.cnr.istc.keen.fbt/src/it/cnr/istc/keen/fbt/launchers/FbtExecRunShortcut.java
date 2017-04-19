package it.cnr.istc.keen.fbt.launchers;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.launchers.BaseEpslRunShortcut;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;

public class FbtExecRunShortcut extends BaseEpslRunShortcut {

	public FbtExecRunShortcut() {
		super();
		launchConfigurationType = IFbtExecLaunchConfigurationConstants.ID;
	}

	@Override
	protected ConfigurationData getcConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}

}
