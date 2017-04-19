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
package it.cnr.istc.keen.epsl;

import it.cnr.istc.keen.epsl.launchers.IEpslLaunchConfigurationConstants;

public class EpslPlannersUpdater extends BasePlannersUpdater
{
	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
	
	@Override
	protected String getXmlInstallPreferenceKey()
	{
		return IEpslLaunchConfigurationConstants.EPSL_INSTALL_XML; 
	}

}
