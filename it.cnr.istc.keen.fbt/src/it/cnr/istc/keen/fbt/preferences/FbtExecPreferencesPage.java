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
package it.cnr.istc.keen.fbt.preferences;

import it.cnr.istc.keen.epsl.BasePlannersUpdater;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.preferences.BaseEPSLPreferencesPage;
import it.cnr.istc.keen.epsl.preferences.BaseInstalledEPSLsBlock;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;
import it.cnr.istc.keen.fbt.FbtExecutorsUpdater;

public class FbtExecPreferencesPage extends BaseEPSLPreferencesPage
{
	public static final String ID = "it.cnr.istc.keen.fbt.preferences.FbtExecPreferencesPage";
	
	@Override
	protected String getPreferencesID()
	{
		return ID;
	}
	
	@Override
	protected BaseInstalledEPSLsBlock createInstalledBlock()
	{
		return new InstalledFbtExecsBlock();		
	}
	
	@Override
	protected BasePlannersUpdater createPlannersUpdater()
	{
		return new FbtExecutorsUpdater();
	}
	
	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
}
