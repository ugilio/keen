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

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import it.cnr.istc.keen.validation.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    @Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Preferences.VERIFYTGAPATH, findExecutable("verifytga"));
		store.setDefault(Preferences.PLAN2TIGAPATH, findExecutable("plan2tiga"));
		store.setDefault(Preferences.VALIDATIONTIMEOUT, "3");
	}
	
	private String findExecutable(String exec)
	{
		String pathVar = System.getenv("PATH");  
		String[] dirs = pathVar.split(File.pathSeparator);  
	   
		for (String dir : dirs)  
		{
			File file = new File(dir, exec);  
			if (file.isFile() && file.canExecute())
				return file.getAbsolutePath();
		}
		return "";
	}

}
