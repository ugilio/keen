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
 * Copy of org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.wizards;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslInstallImpl;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

public abstract class AbstractEpslInstallPage extends WizardPage {

	private String fOriginalName = null;
	private IStatus fNameStatus = Status.OK_STATUS;

	private String[] fExistingNames;
	
	protected ConfigurationData strings;

	protected AbstractEpslInstallPage(ConfigurationData strings) {
		super(strings.get(ConfigurationData.PAGENAME));
		this.strings = strings;
	}

	public abstract boolean finish();

	public abstract EpslInstallImpl getSelection();

	public void setSelection(EpslInstallImpl installation) {
		fOriginalName = installation.getName();
	}

	protected void nameChanged(String newName) {
		fNameStatus = Status.OK_STATUS;
		if (newName == null || newName.trim().length() == 0) {
			int sev = IStatus.ERROR;
			if (fOriginalName == null || fOriginalName.length() == 0) {
				sev = IStatus.WARNING;
			}
			fNameStatus = new Status(sev, Activator.PLUGIN_ID, strings.get(ConfigurationData.ENTER_NAME));
		} else {
			if (isDuplicateName(newName)) {
				fNameStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, strings.get(ConfigurationData.NAME_USED));
			} else {
				IStatus s = ResourcesPlugin.getWorkspace().validateName(newName, IResource.FILE);
				if (!s.isOK()) {
					fNameStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							strings.get(ConfigurationData.INVALID_FNAME) + s.getMessage());
				}
			}
		}
		updatePageStatus();
	}

	private boolean isDuplicateName(String name) {
		if (fExistingNames != null) {
			for (int i = 0; i < fExistingNames.length; i++) {
				if (name.equals(fExistingNames[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public void setExistingNames(String[] names) {
		fExistingNames = names;
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	protected void setStatusMessage(IStatus status) {
		if (status.isOK()) {
			setMessage(status.getMessage());
		} else {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				setMessage(status.getMessage(), IMessageProvider.ERROR);
				break;
			case IStatus.INFO:
				setMessage(status.getMessage(), IMessageProvider.INFORMATION);
				break;
			case IStatus.WARNING:
				setMessage(status.getMessage(), IMessageProvider.WARNING);
				break;
			default:
				break;
			}
		}
	}

	protected IStatus getNameStatus() {
		return fNameStatus;
	}

	protected void updatePageStatus() {
		IStatus max = Status.OK_STATUS;
		IStatus[] installStatus = getInstallStatus();
		for (int i = 0; i < installStatus.length; i++) {
			IStatus status = installStatus[i];
			if (status.getSeverity() > max.getSeverity()) {
				max = status;
			}
		}
		if (fNameStatus.getSeverity() > max.getSeverity()) {
			max = fNameStatus;
		}
		if (max.isOK()) {
			setMessage(null, IMessageProvider.NONE);
		} else {
			setStatusMessage(max);
		}
		setPageComplete(max.isOK() || max.getSeverity() == IStatus.INFO);
	}

	protected abstract IStatus[] getInstallStatus();
}
