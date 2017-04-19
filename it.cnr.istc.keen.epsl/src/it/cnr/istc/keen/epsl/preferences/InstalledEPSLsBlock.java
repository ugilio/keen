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

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.EpslRegistry;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.wizards.AddEpslInstallWizard;
import it.cnr.istc.keen.epsl.wizards.BaseAddEpslInstallWizard;
import it.cnr.istc.keen.epsl.wizards.EditEpslInstallWizard;
import it.cnr.istc.keen.epsl.wizards.EpslInstallWizard;

public class InstalledEPSLsBlock extends BaseInstalledEPSLsBlock
{
	@Override
    protected BaseEpslRegistry getEpslRegistry()
    {
    	return EpslRegistry.getInstance();
    }

	@Override
	public String getTitle() {
		return "Installed &EPSL planners:";
	}

	@Override
	protected BaseAddEpslInstallWizard createAddWizard(IEPSLInstall[] currentInstalls) {
		return new AddEpslInstallWizard(currentInstalls);
	}
	
	@Override
	protected EpslInstallWizard createEditWizard(EpslInstallImpl vm, IEPSLInstall[] allVMs) {
		return new EditEpslInstallWizard(vm, allVMs);
	}
}
