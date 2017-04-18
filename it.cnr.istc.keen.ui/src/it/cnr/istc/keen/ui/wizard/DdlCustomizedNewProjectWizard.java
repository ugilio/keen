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
package it.cnr.istc.keen.ui.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.xtext.ui.wizard.IProjectCreator;

import com.google.inject.Inject;

public class DdlCustomizedNewProjectWizard extends DdlNewProjectWizard implements IExecutableExtension {
	
	private IConfigurationElement config;

	@Inject
	public DdlCustomizedNewProjectWizard(IProjectCreator projectCreator) {
		super(projectCreator);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		this.config = config;
	}
	
	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result)
			BasicNewProjectResourceWizard.updatePerspective(config);
		
		return result;
	}

}
