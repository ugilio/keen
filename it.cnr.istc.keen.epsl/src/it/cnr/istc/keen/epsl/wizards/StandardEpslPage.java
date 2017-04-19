/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Copy of org.eclipse.jdt.internal.debug.ui.jres.StandardVMPage (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.wizards;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.utils.SWTFactory;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

public class StandardEpslPage extends AbstractEpslInstallPage {

	private EpslInstallImpl fInstallation;
	private Text fPlannerName;
	private Text fPlannerRoot;
	private IStatus[] fFieldStatus = new IStatus[1];
	
	private BaseEpslRegistry fEpslRegistry;
	
	public StandardEpslPage (ConfigurationData strings) {
		super(strings);
		this.fEpslRegistry = strings.getEpslRegistry();
		for (int i = 0; i < fFieldStatus.length; i++) {
			fFieldStatus[i] = Status.OK_STATUS;
		}
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public void createControl(Composite p) {
		Composite composite = new Composite(p, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		SWTFactory.createLabel(composite, strings.get(ConfigurationData.PLANNER_HOME), 1);
		fPlannerRoot = SWTFactory.createSingleText(composite, 1);
		Button folders = SWTFactory.createPushButton(composite, "Direct&ory...", null);
		GridData data = (GridData) folders.getLayoutData();
		data.horizontalAlignment = GridData.END;

		SWTFactory.createLabel(composite, strings.get(ConfigurationData.PLANNER_NAME), 1);
		fPlannerName = SWTFactory.createSingleText(composite, 2);

		fPlannerName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePlannerName();
			}
		});
		fPlannerRoot.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePlannerLocation();
			}
		});
		folders.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				File file = new File(fPlannerRoot.getText());
				String text = fPlannerRoot.getText();
				if (file.isFile()) {
					text = file.getParentFile().getAbsolutePath();
				}
				dialog.setFilterPath(text);
				dialog.setMessage(strings.get(ConfigurationData.ROOT_DIR));
				String newPath = dialog.open();
				if (newPath != null)
					fPlannerRoot.setText(newPath);
			}
		});
		Dialog.applyDialogFont(composite);
		setControl(composite);
		initializeFields();
	}

	private boolean checkEPSLJar(File directory) {
		File f = new File(directory.getPath(), fEpslRegistry.getJarName());
		return (f.exists() && f.isFile());
	}

	private void validatePlannerLocation() {
		String locationName = fPlannerRoot.getText();
		IStatus s = Status.OK_STATUS;
		File file = null;
		if (locationName.length() == 0)
			s = new Status(IStatus.WARNING, Activator.PLUGIN_ID, strings.get(ConfigurationData.HOME_DIR));
		else {
			file = new File(locationName);
			if (!file.exists())
				s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The home directory does not exist.");
			else if (!checkEPSLJar(file))
				s = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						String.format("The home directory does not contain %s.", fEpslRegistry.getJarName()));
		}
		if (file != null)
			fInstallation.setInstallLocation(file);
		if (s.isOK() && file != null) {
			String name = fPlannerName.getText();
			if (name == null || name.trim().length() == 0)
				fPlannerName.setText(strings.get(ConfigurationData.DEFAULT));
		}
		setPlannerLocationStatus(s);
		updatePageStatus();
	}

	protected File getInstallLocation() {
		return new File(fPlannerRoot.getText());
	}

	private void validatePlannerName() {
		nameChanged(fPlannerName.getText());
	}

	@Override
	public boolean finish() {
		setFieldValuesToInstall(fInstallation);
		return true;
	}

	@Override
	public EpslInstallImpl getSelection() {
		return fInstallation;
	}

	@Override
	public void setSelection(EpslInstallImpl installation) {
		super.setSelection(installation);
		fInstallation = installation;
		setTitle(strings.get(ConfigurationData.DEFINITION));
		setDescription(strings.get(ConfigurationData.ATTRIBUTES));
	}

	protected void setFieldValuesToInstall(EpslInstallImpl install) {
		File dir = new File(fPlannerRoot.getText());
		File file = dir.getAbsoluteFile();
		install.setInstallLocation(file);
		install.setName(fPlannerName.getText());
	}

	private void initializeFields() {
		fPlannerName.setText(fInstallation.getName());
		File installLocation = fInstallation.getInstallLocation();
		if (installLocation != null)
			fPlannerRoot.setText(installLocation.getAbsolutePath());
		validatePlannerName();
		validatePlannerLocation();
	}

	private void setPlannerLocationStatus(IStatus status) {
		fFieldStatus[0] = status;
	}

	@Override
	protected IStatus[] getInstallStatus() {
		return fFieldStatus;
	}

}
