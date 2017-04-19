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

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.launchers.BaseEPSLComboBlock;
import it.cnr.istc.keen.epsl.launchers.EPSLComboBlock;

public class EPSLTab extends BaseEPSLTab
{
	@Override
    protected BaseEPSLComboBlock createEPSLComboBlock(boolean defaultFirst)
    {
    	return new EPSLComboBlock(defaultFirst);
    }

	@Override
	public String getName() {
		return "EPSL";
	}
	
	@Override
	public String getId() {
		return "it.cnr.istc.keen.epsl.preferences.EPSLTab";
	}

	@Override
	protected ConfigurationData getConfigurationData()
	{
		return EpslConfigurationData.getInstance();
	}
}
