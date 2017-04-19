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

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.launchers.BaseEPSLComboBlock;
import it.cnr.istc.keen.epsl.preferences.BaseEPSLTab;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;
import it.cnr.istc.keen.fbt.launchers.FbtExecComboBlock;

public class FbtExecTab extends BaseEPSLTab
{
	@Override
    protected BaseEPSLComboBlock createEPSLComboBlock(boolean defaultFirst)
    {
    	return new FbtExecComboBlock(defaultFirst);
    }

	@Override
	public String getName() {
		return "Executor";
	}
	
	@Override
	public String getId() {
		return "it.cnr.istc.keen.fbt.preferences.FbtExecTab";
	}

	@Override
	protected ConfigurationData getConfigurationData()
	{
		return FbtExecConfigurationData.getInstance();
	}
}
