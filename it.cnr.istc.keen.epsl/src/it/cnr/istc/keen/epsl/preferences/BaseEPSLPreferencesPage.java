/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Copy of org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage (Kepler)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.preferences;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.BasePlannersUpdater;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.utils.SWTFactory;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class BaseEPSLPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private BaseInstalledEPSLsBlock fPlannersBlock;
	
	protected abstract String getPreferencesID();
	
	protected abstract BaseInstalledEPSLsBlock createInstalledBlock();
	
	protected abstract BasePlannersUpdater createPlannersUpdater();
	
    protected BaseEpslRegistry getEpslRegistry()
    {
    	return getConfigurationData().getEpslRegistry();
    }
    
    protected abstract ConfigurationData getConfigurationData();

	@Override
	protected Control createContents(Composite ancestor) {
		ConfigurationData confData = getConfigurationData();
		
		initializeDialogUnits(ancestor);

		noDefaultButton();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);

		SWTFactory.createWrapLabel(ancestor,
				confData.get(ConfigurationData.ADD_REMOVE_EDIT),
				1, 300);
		SWTFactory.createVerticalSpacer(ancestor, 1);

		fPlannersBlock = createInstalledBlock();
		fPlannersBlock.createControl(ancestor);
		Control control = fPlannersBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		fPlannersBlock.restoreColumnSettings(Activator.getDefault().getDialogSettings(), getPreferencesID());

		initDefaultPlanner();
		fPlannersBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IEPSLInstall install = getCurrentDefaultPlanner();
				if (install == null) {
					setValid(false);
					if (fPlannersBlock.getPlanners().length < 1) {
						setErrorMessage(confData.get(ConfigurationData.AT_LEAST_ONE_PLANNER));
					}
					else {
						setErrorMessage(confData.get(ConfigurationData.DEFAULT_NEEDED));
					}
				} else {
					setValid(true);
					setErrorMessage(null);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}
	
	private void initDefaultPlanner() {
		IEPSLInstall epslDefault = getEpslRegistry().getDefault();
		if (epslDefault != null) {
			IEPSLInstall[] planners = fPlannersBlock.getPlanners();
			for (int i = 0; i < planners.length; i++) {
				IEPSLInstall entry = planners[i];
				if (entry.equals(epslDefault)) {
					fPlannersBlock.setCheckedItem(entry);
					break;
				}
			}
		}
	}

	private IEPSLInstall getCurrentDefaultPlanner() {
		return fPlannersBlock.getCheckedItem();
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				IEPSLInstall defaultPlanner = getCurrentDefaultPlanner();
				IEPSLInstall[] vms = fPlannersBlock.getPlanners();
				BasePlannersUpdater updater = createPlannersUpdater();
				if (!updater.updatePlannersSettings(vms, defaultPlanner)) {
					canceled[0] = true;
				}
			}
		});

		if (canceled[0]) {
			return false;
		}

		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		fPlannersBlock.saveColumnSettings(settings, getPreferencesID());

		return super.performOk();
	}
	
}
