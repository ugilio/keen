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
package it.cnr.istc.keen.fbt.launchers;

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.launchers.BaseEPSLComboBlock;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;
import it.cnr.istc.keen.fbt.FbtExecRegistry;
import it.cnr.istc.keen.fbt.preferences.FbtExecPreferencesPage;

public class FbtExecComboBlock extends BaseEPSLComboBlock {

	public static final String PROPERTY_FBTEXEC = "PROPERTY_FBTEXEC";
	
	public FbtExecComboBlock(boolean defaultFirst) {
		super(defaultFirst);
	}
	
	@Override
	protected String getPropertyName()
	{
		return PROPERTY_FBTEXEC;
	}
	
	@Override
	protected String getPreferencesPageID()
	{
		return FbtExecPreferencesPage.ID;
	}
	
	@Override
	protected BaseEpslRegistry getEpslRegistry()
	{
		return FbtExecRegistry.getInstance();
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
}
