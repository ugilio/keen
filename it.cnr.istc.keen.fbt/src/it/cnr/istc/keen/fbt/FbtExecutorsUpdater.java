/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *   
 * Inspired by org.eclipse.jdt.internal.debug.ui.jres.JREsUpdater
 */
package it.cnr.istc.keen.fbt;

import it.cnr.istc.keen.epsl.BasePlannersUpdater;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.fbt.launchers.IFbtExecLaunchConfigurationConstants;

public class FbtExecutorsUpdater extends BasePlannersUpdater
{
	@Override
	protected String getXmlInstallPreferenceKey()
	{
		return IFbtExecLaunchConfigurationConstants.FBTEXEC_INSTALL_XML; 
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
	
}
