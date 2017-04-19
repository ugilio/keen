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
package it.cnr.istc.keen.epsl.launchers;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.preferences.EPSLPreferencesPage;

public class EPSLComboBlock extends BaseEPSLComboBlock {

	public static final String PROPERTY_EPSL = "PROPERTY_EPSL";
	
	public EPSLComboBlock(boolean defaultFirst) {
		super(defaultFirst);
	}
	
	@Override
	protected String getPropertyName()
	{
		return PROPERTY_EPSL;
	}
	
	@Override
	protected String getPreferencesPageID()
	{
		return EPSLPreferencesPage.ID;
	}
	
	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
