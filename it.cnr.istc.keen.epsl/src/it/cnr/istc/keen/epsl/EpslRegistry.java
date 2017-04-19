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
package it.cnr.istc.keen.epsl;

import it.cnr.istc.keen.epsl.launchers.IEpslLaunchConfigurationConstants;

public class EpslRegistry extends BaseEpslRegistry
{
    public static final String EPSL_JAR_NAME = "epsl-cli.jar";
	
    private static EpslRegistry instance = new EpslRegistry();
    
    public static EpslRegistry getInstance()
    {
    	return instance;
    }
    
    protected String getPreferencesKey()
    {
    	return IEpslLaunchConfigurationConstants.EPSL_INSTALL_XML;
    }

	@Override
	public String getJarName() {
		return EPSL_JAR_NAME;
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
