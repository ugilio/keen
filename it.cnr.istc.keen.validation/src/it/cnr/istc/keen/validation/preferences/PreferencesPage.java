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
package it.cnr.istc.keen.validation.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import it.cnr.istc.keen.validation.Activator;

public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencesPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("External tools");
	}

	public void createFieldEditors() {
		addField(new FileFieldEditor(Preferences.VERIFYTGAPATH, "Path to &verifytga:", true, getFieldEditorParent()));
		addField(new FileFieldEditor(Preferences.PLAN2TIGAPATH, "Path to &plan2tiga:", true, getFieldEditorParent()));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = (Composite)super.createContents(parent);
		
		int numColumns = ((GridLayout) composite.getLayout()).numColumns;
		
        Group validationComposite = new Group(composite, SWT.LEFT);
        GridLayout layout = new GridLayout();
        validationComposite.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, numColumns, 1);
        validationComposite.setLayoutData(data);
        validationComposite.setText("Validation options");
		
		IntegerFieldEditor timeoutField =
				new IntegerFieldEditor(Preferences.VALIDATIONTIMEOUT,
						"Validation timeout (seconds)", validationComposite);
        timeoutField.setPage(this);
        timeoutField.setPropertyChangeListener(this);
        timeoutField.setPreferenceStore(getPreferenceStore());
        timeoutField.load();
        addField(timeoutField);
        
		return composite;
	}

	public void init(IWorkbench workbench) {

	}

}