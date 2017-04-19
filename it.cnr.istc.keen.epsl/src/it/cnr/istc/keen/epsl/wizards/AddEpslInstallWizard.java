/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Copy of org.eclipse.jdt.internal.debug.ui.jres.AddVMInstallWizard (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.wizards;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.IEPSLInstall;

public class AddEpslInstallWizard extends BaseAddEpslInstallWizard {

	public AddEpslInstallWizard(IEPSLInstall[] currentInstalls) {
		super(currentInstalls);
	}

	@Override
	protected String getTitle()
	{
		return "Add EPSL Installation";
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
