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
package it.cnr.istc.keen.fbt;

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.fbt.launchers.IFbtExecLaunchConfigurationConstants;

public class FbtExecRegistry extends BaseEpslRegistry
{
    public static final String FBT_JAR_NAME = "fbt-console.jar";
	
    private static FbtExecRegistry instance = new FbtExecRegistry();
    
    public static FbtExecRegistry getInstance()
    {
    	return instance;
    }
    
    protected String getPreferencesKey()
    {
    	return IFbtExecLaunchConfigurationConstants.FBTEXEC_INSTALL_XML;
    }

	@Override
	public String getJarName() {
		return FBT_JAR_NAME;
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
}
