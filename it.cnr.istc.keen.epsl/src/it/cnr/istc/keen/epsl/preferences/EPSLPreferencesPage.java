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
package it.cnr.istc.keen.epsl.preferences;

import it.cnr.istc.keen.epsl.BasePlannersUpdater;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.EpslPlannersUpdater;

public class EPSLPreferencesPage extends BaseEPSLPreferencesPage
{
	public static final String ID = "it.cnr.istc.keen.epsl.preferences.EPSLPreferencesPage";
	
	@Override
	protected String getPreferencesID()
	{
		return ID;
	}
	
	@Override
	protected BaseInstalledEPSLsBlock createInstalledBlock()
	{
		return new InstalledEPSLsBlock();		
	}

	@Override
	protected BasePlannersUpdater createPlannersUpdater()
	{
		return new EpslPlannersUpdater();
	}
	
	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
