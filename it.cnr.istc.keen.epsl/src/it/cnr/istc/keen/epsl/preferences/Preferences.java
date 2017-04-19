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
package it.cnr.istc.keen.epsl.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import it.cnr.istc.keen.epsl.Activator;

public class Preferences
{
	public static String getString(String key)
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(key);
	}
	
    public static void setString(String key, String value)
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setValue(key, value);
    }
    
}
