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

public class EpslRunShortcut extends BaseEpslRunShortcut
{
	public EpslRunShortcut() {
		super();
		launchConfigurationType = IEpslLaunchConfigurationConstants.ID;
	}

	@Override
	protected ConfigurationData getcConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
