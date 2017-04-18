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
 * Copy of org.eclipse.jdt.internal.debug.ui.jres.EditVMInstallWizard (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.wizards;

import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.IEPSLInstall;

public class EditEpslInstallWizard extends EpslInstallWizard {

	private AbstractEpslInstallPage fEditPage;

	public EditEpslInstallWizard(EpslInstallImpl vm, IEPSLInstall[] allVMs) {
		super(vm, allVMs);
		setWindowTitle("Edit EPSL Installation");
	}

	@Override
	public void addPages() {
		fEditPage = getPage();
		fEditPage.setSelection(new EpslInstallImpl(getEpslInstall()));
		addPage(fEditPage);
	}

	@Override
	public boolean performFinish() {
		if (fEditPage.finish()) {
			return super.performFinish();
		}
		return false;
	}

	@Override
	public EpslInstallImpl getResult() {
		return fEditPage.getSelection();
	}
}