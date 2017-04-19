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
package it.cnr.istc.keen.fbt.wizards;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.wizards.BaseAddEpslInstallWizard;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;

public class AddFbtExecInstallWizard extends BaseAddEpslInstallWizard {

	public AddFbtExecInstallWizard(IEPSLInstall[] currentInstalls) {
		super(currentInstalls);
	}

	@Override
	protected String getTitle()
	{
		return "Add Executor Installation";
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
}
