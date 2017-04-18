/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - Adapted for EPSL
 *     
 * Copied from org.eclipse.jdt.internal.debug.ui.jres.JREsComboBlock (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.launchers;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.EpslRegistry;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.preferences.EPSLPreferencesPage;
import it.cnr.istc.keen.epsl.utils.SWTFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class EPSLComboBlock {

	public static final String PROPERTY_EPSL = "PROPERTY_EPSL";

	private Composite fControl;

	private List<IEPSLInstall> fPlanners = new ArrayList<IEPSLInstall>();

	private Combo fCombo;
	private Button fManageButton;

	private ListenerList<IPropertyChangeListener> fListeners = new ListenerList<>();

	private boolean fDefaultFirst;

	private EPSLDescriptor fDefaultDescriptor = null;
	private EPSLDescriptor fSpecificDescriptor = null;

	private Button fDefaultButton = null;
	private Button fSpecificButton = null;

	private String fTitle = null;

	private IPath fErrorPath;

	private IStatus fStatus = OK_STATUS;

	private static IStatus OK_STATUS = new Status(IStatus.OK, Activator.PLUGIN_ID, "");

	public EPSLComboBlock(boolean defaultFirst) {
		fDefaultFirst = defaultFirst;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	private void firePropertyChange() {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROPERTY_EPSL, null, getPath());
		for (IPropertyChangeListener listener : fListeners) {
			listener.propertyChange(event);
		}
	}

	public void createControl(Composite ancestor) {
		fControl = SWTFactory.createComposite(ancestor, 1, 1, GridData.FILL_BOTH);
		if (fTitle == null) {
			fTitle = "Planner to run:";
		}
		Group group = SWTFactory.createGroup(fControl, fTitle, 1, 1, GridData.FILL_HORIZONTAL);
		Composite comp = SWTFactory.createComposite(group, group.getFont(), 3, 1, GridData.FILL_BOTH, 0, 0);

		if (fDefaultFirst) {
			createDefaultPlannerControls(comp);
		}
		createSpecificPlannerControls(comp);
		if (!fDefaultFirst) {
			createDefaultPlannerControls(comp);
		}
	}

	private void createSpecificPlannerControls(Composite comp) {
		String text = "Specific Planner:";
		if (fSpecificDescriptor != null) {
			text = fSpecificDescriptor.getDescription();
		}
		fSpecificButton = SWTFactory.createRadioButton(comp, text, 1);
		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSpecificButton.getSelection()) {
					fCombo.setEnabled(true);
					if (fCombo.getText().length() == 0 && !fPlanners.isEmpty()) {
						fCombo.select(0);
					}
					if (fPlanners.isEmpty()) {
						setError("No planners defined in workspace");
					} else {
						setStatus(OK_STATUS);
					}
					firePropertyChange();
				}
			}
		});
		fCombo = SWTFactory.createCombo(comp, SWT.DROP_DOWN | SWT.READ_ONLY, 1, null);
		fCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setStatus(OK_STATUS);
				firePropertyChange();
			}
		});

		fManageButton = SWTFactory.createPushButton(comp, "&Installed planners...", null);
		fManageButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				showPrefPage(EPSLPreferencesPage.ID);
			}
		});
		fillWithWorkspacePlanners();
	}

	private void createDefaultPlannerControls(Composite comp) {
		if (fDefaultDescriptor != null) {
			fDefaultButton = SWTFactory.createRadioButton(comp, fDefaultDescriptor.getDescription(), 3);
			fDefaultButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fDefaultButton.getSelection()) {
						setUseDefaultPlanner();
						setStatus(OK_STATUS);
						firePropertyChange();
					}
				}
			});
		}
	}

	private void showPrefPage(String id/* , IPreferencePage page */) {
		IEPSLInstall prevPlanner = getPlanner();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null).open();
		fillWithWorkspacePlanners();
		restoreCombo(fPlanners, prevPlanner, fCombo);
		// update text
		setDefaultEPSLDescriptor(fDefaultDescriptor);
		if (isDefaultPlanner()) {
			// reset in case default has changed
			setUseDefaultPlanner();
		}
		setPath(getPath());
		firePropertyChange();
	}

	private void restoreCombo(List<?> elements, Object element, Combo combo) {
		int index = -1;
		if (element != null) {
			index = elements.indexOf(element);
		}
		if (index >= 0) {
			combo.select(index);
		} else {
			combo.select(0);
		}
	}

	public Control getControl() {
		return fControl;
	}

	protected void setPlanners(List<EpslInstallImpl> planners) {
		fPlanners.clear();
		fPlanners.addAll(planners);
		// sort by name
		Collections.sort(fPlanners, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				IEPSLInstall left = (IEPSLInstall) o1;
				IEPSLInstall right = (IEPSLInstall) o2;
				return left.getName().compareToIgnoreCase(right.getName());
			}

			@Override
			public boolean equals(Object obj) {
				return obj == this;
			}
		});
		// now make an array of names
		String[] names = new String[fPlanners.size()];
		Iterator<IEPSLInstall> iter = fPlanners.iterator();
		int i = 0;
		while (iter.hasNext()) {
			IEPSLInstall epsl = iter.next();
			names[i] = epsl.getName();
			i++;
		}
		fCombo.setItems(names);
		fCombo.setVisibleItemCount(Math.min(names.length, 20));
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	private void selectPlanner(IEPSLInstall epsl) {
		fSpecificButton.setSelection(true);
		fDefaultButton.setSelection(false);
		fCombo.setEnabled(true);
		if (epsl != null) {
			int index = fPlanners.indexOf(epsl);
			if (index >= 0) {
				fCombo.select(index);
			}
		}
		firePropertyChange();
	}

	public IEPSLInstall getPlanner() {
		int index = fCombo.getSelectionIndex();
		if (index >= 0) {
			return fPlanners.get(index);
		}
		return null;
	}

	protected void fillWithWorkspacePlanners() {
		List<EpslInstallImpl> list = new ArrayList<EpslInstallImpl>();
		IEPSLInstall[] installs = EpslRegistry.getInstalledPlanners();
		for (int j = 0; j < installs.length; j++) {
			IEPSLInstall install = installs[j];
			list.add(new EpslInstallImpl(install));
		}
		setPlanners(list);
	}

	public void setDefaultEPSLDescriptor(EPSLDescriptor descriptor) {
		fDefaultDescriptor = descriptor;
		setButtonTextFromDescriptor(fDefaultButton, descriptor);
	}

	private void setButtonTextFromDescriptor(Button button, EPSLDescriptor descriptor) {
		if (button != null) {
			String currentText = button.getText();
			String newText = descriptor.getDescription();
			if (!newText.equals(currentText)) {
				button.setText(newText);
				fControl.layout();
			}
		}
	}

	public void setSpecificPlannerDescriptor(EPSLDescriptor descriptor) {
		fSpecificDescriptor = descriptor;
		setButtonTextFromDescriptor(fSpecificButton, descriptor);
	}

	public boolean isDefaultPlanner() {
		if (fDefaultButton != null) {
			return fDefaultButton.getSelection();
		}
		return false;
	}

	private void setUseDefaultPlanner() {
		if (fDefaultDescriptor != null) {
			fDefaultButton.setSelection(true);
			fSpecificButton.setSelection(false);
			fCombo.setEnabled(false);
			firePropertyChange();
		}
	}

	public void setTitle(String title) {
		fTitle = title;
	}

	public void refresh() {
		setDefaultEPSLDescriptor(fDefaultDescriptor);
	}

	public IPath getPath() {
		if (!getStatus().isOK() && fErrorPath != null) {
			return fErrorPath;
		}
		IEPSLInstall epsl;
		if (fSpecificButton.getSelection()) {
			int index = fCombo.getSelectionIndex();
			if (index >= 0)
				epsl = fPlanners.get(index);
			else
				return null;
		} else
			epsl = EpslRegistry.getDefault();
		if (epsl != null)
			return Path.fromOSString(epsl.getInstallLocation().getAbsolutePath());
		return null;
	}

	public void setPath(IPath containerPath) {
		fErrorPath = null;
		setStatus(OK_STATUS);
		IEPSLInstall install = containerPath != null ? EpslRegistry.findByPath(containerPath.toOSString()) : null;
		if (install != null && EpslRegistry.getDefault().equals(install)) {
			setUseDefaultPlanner();
		} else {
			if (install == null) {
				selectPlanner(install);
				fErrorPath = containerPath;
				setError("Unable to resolve EPSL install");
			} else {
				selectPlanner(install);
				File location = install.getInstallLocation();
				if (location == null) {
					setError("Planner home directory not specified");
				} else if (!location.exists()) {
					setError("Planner home directory does not exist");
				}
			}
		}
	}

	private void setError(String message) {
		setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
	}

	public IStatus getStatus() {
		return fStatus;
	}

	private void setStatus(IStatus status) {
		fStatus = status;
	}
}
