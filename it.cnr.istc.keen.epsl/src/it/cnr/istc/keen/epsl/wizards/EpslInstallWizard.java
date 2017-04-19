/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Copy of org.eclipse.jdt.internal.debug.ui.jres.VMInstallWizard (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.IEPSLInstall;

public abstract class EpslInstallWizard extends Wizard {

	private EpslInstallImpl fEditInstall;
	private String[] fExistingNames;

	public EpslInstallWizard(EpslInstallImpl editInstall, IEPSLInstall[] currentInstalls) {
		fEditInstall = editInstall;
		List<String> names = new ArrayList<String>(currentInstalls.length);
		for (int i = 0; i < currentInstalls.length; i++) {
			IEPSLInstall install = currentInstalls[i];
			if (!install.equals(editInstall))
				names.add(install.getName());
		}
		fExistingNames = names.toArray(new String[names.size()]);
	}

	protected EpslInstallImpl getEpslInstall() {
		return fEditInstall;
	}

	public abstract EpslInstallImpl getResult();

    protected BaseEpslRegistry getEpslRegistry()
    {
    	return getConfigurationData().getEpslRegistry();
    }
    
    protected abstract ConfigurationData getConfigurationData();
    
	@Override
	public boolean performFinish() {
		return getResult() != null;
	}

	public AbstractEpslInstallPage getPage() {
		StandardEpslPage standardPage = new StandardEpslPage(getConfigurationData());
		standardPage.setExistingNames(fExistingNames);
		return standardPage;
	}
}