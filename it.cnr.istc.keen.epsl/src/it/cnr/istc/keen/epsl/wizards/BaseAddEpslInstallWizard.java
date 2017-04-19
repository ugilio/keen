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

import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.IEPSLInstall;

public abstract class BaseAddEpslInstallWizard extends EpslInstallWizard {

	private AbstractEpslInstallPage fAddPage = null;

	private EpslInstallImpl fResult = null;
	
	protected abstract String getTitle();
	
	public BaseAddEpslInstallWizard(IEPSLInstall[] currentInstalls) {
		super(null, currentInstalls);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(getTitle());
	}

	@Override
	public void addPages() {
		EpslInstallImpl epsl = new EpslInstallImpl(createUniqueId());
		epsl.setName("");

		fAddPage = getPage();
		fAddPage.setSelection(epsl);
		addPage(fAddPage);
	}

	@Override
	public EpslInstallImpl getResult() {
		return fResult;
	}

	@Override
	public boolean performFinish() {
		boolean finish = fAddPage.finish();
		fResult = fAddPage.getSelection();
		return finish;
	}
	
	protected String createUniqueId() {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (getEpslRegistry().findById(id) != null);
		return id;
	}
}
