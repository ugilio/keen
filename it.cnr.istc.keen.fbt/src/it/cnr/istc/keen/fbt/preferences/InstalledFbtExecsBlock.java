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

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.preferences.BaseInstalledEPSLsBlock;
import it.cnr.istc.keen.epsl.wizards.BaseAddEpslInstallWizard;
import it.cnr.istc.keen.epsl.wizards.EpslInstallWizard;
import it.cnr.istc.keen.fbt.FbtExecRegistry;
import it.cnr.istc.keen.fbt.wizards.AddFbtExecInstallWizard;
import it.cnr.istc.keen.fbt.wizards.EditFbtExecInstallWizard;

public class InstalledFbtExecsBlock extends BaseInstalledEPSLsBlock
{
	@Override
    protected BaseEpslRegistry getEpslRegistry()
    {
    	return FbtExecRegistry.getInstance();
    }

	@Override
	public String getTitle() {
		return "Installed &FbT executors:";
	}
	
	@Override
	protected BaseAddEpslInstallWizard createAddWizard(IEPSLInstall[] currentInstalls) {
		return new AddFbtExecInstallWizard(currentInstalls);
	}
	
	@Override
	protected EpslInstallWizard createEditWizard(EpslInstallImpl vm, IEPSLInstall[] allVMs) {
		return new EditFbtExecInstallWizard(vm, allVMs);
	}
}
