/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Copy of org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.preferences;

import it.cnr.istc.keen.epsl.EpslRegistry;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.launchers.EPSLComboBlock;
import it.cnr.istc.keen.epsl.launchers.EPSLDescriptor;
import it.cnr.istc.keen.epsl.launchers.IEpslLaunchConfigurationConstants;
import it.cnr.istc.keen.epsl.utils.SWTFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class EPSLTab extends AbstractLaunchConfigurationTab {

	protected EPSLComboBlock fEPSLBlock;
	protected ILaunchConfiguration fLaunchConfiguration;

	protected boolean fIsInitializing = false;

	private IPropertyChangeListener fCheckListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			handleSelectedPlannerChanged();
		}
	};

	@Override
	public void dispose() {
		super.dispose();
		if (fEPSLBlock != null) {
			fEPSLBlock.removePropertyChangeListener(fCheckListener);
		}
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite topComp = SWTFactory.createComposite(parent, font, 1, 1, GridData.FILL_HORIZONTAL, 0, 0);

		fEPSLBlock = new EPSLComboBlock(true);
		fEPSLBlock.setDefaultEPSLDescriptor(getDefaultPlannerDescriptor());
		fEPSLBlock.setSpecificPlannerDescriptor(getSpecificPlannerDescriptor());
		fEPSLBlock.createControl(topComp);
		Control control = fEPSLBlock.getControl();
		fEPSLBlock.addPropertyChangeListener(fCheckListener);
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(topComp);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		fIsInitializing = true;
		getControl().setRedraw(false);
		setLaunchConfiguration(configuration);
		updatePlannerFromConfig(configuration);
		fEPSLBlock.setDefaultEPSLDescriptor(getDefaultPlannerDescriptor());
		getControl().setRedraw(true);
		fIsInitializing = false;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fEPSLBlock.isDefaultPlanner()) {
			configuration.setAttribute(IEpslLaunchConfigurationConstants.ATTR_EPSL_INSTALL_PATH, (String) null);
		} else {
			IPath containerPath = fEPSLBlock.getPath();
			String portablePath = null;
			if (containerPath != null) {
				portablePath = containerPath.toPortableString();
			}
			configuration.setAttribute(IEpslLaunchConfigurationConstants.ATTR_EPSL_INSTALL_PATH, portablePath);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		IStatus status = fEPSLBlock.getStatus();
		if (!status.isOK()) {
			setErrorMessage(status.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public boolean canSave() {
		IStatus status = fEPSLBlock.getStatus();
		return status.isOK();
	}

	@Override
	public String getName() {
		return "EPSL";
	}

	@Override
	public Image getImage() {
		// FIXME
		return null;
	}

	@Override
	public String getId() {
		return "it.cnr.istc.keen.epsl.preferences.EPSLTab";
	}

	private void updatePlannerFromConfig(ILaunchConfiguration config) {
		try {
			String path = config.getAttribute(IEpslLaunchConfigurationConstants.ATTR_EPSL_INSTALL_PATH, (String) null);
			if (path != null) {
				fEPSLBlock.setPath(Path.fromPortableString(path));
				return;
			}
			IEPSLInstall epsl = EpslRegistry.getDefault();
			if (epsl != null)
				fEPSLBlock.setPath(Path.fromOSString(epsl.getInstallLocation().getAbsolutePath()));
		} catch (CoreException e) {
		}
	}

	private void handleSelectedPlannerChanged() {
		updateLaunchConfigurationDialog();
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
	}

	private IEPSLInstall getEpslForProject() {
		ILaunchConfiguration config = getLaunchConfiguration();
		if (config == null)
			return null;
		String path = null;
		try {
			path = config.getAttribute(IEpslLaunchConfigurationConstants.ATTR_EPSL_INSTALL_PATH, (String) null);
			if (path != null)
				return EpslRegistry.findByPath(path);
		} catch (CoreException e) {
		}
		return null;

	}

	private EPSLDescriptor getDefaultPlannerDescriptor() {
		return new EPSLDescriptor() {
			@Override
			public String getDescription() {
				IEPSLInstall epsl = getEpslForProject();
				if (epsl != null)
					return String.format("&Project planner (%s)", epsl.getName());

				String name = "undefined";
				epsl = EpslRegistry.getDefault();
				if (epsl != null)
					name = epsl.getName();
				return String.format("Workspace &default planner (%s)", name);
			}
		};
	}

	private EPSLDescriptor getSpecificPlannerDescriptor() {
		return null;
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		fEPSLBlock.refresh();
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}
}
