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

import org.eclipse.jface.preference.IPreferenceStore;

import it.cnr.istc.keen.validation.Activator;

public class Preferences
{
	public static final String VERIFYTGAPATH = "verifytgaPath";
	public static final String PLAN2TIGAPATH = "plan2tigaPath";
	
	public static final String VALIDATIONTIMEOUT = "validationTimeout";
	
	public static String getString(String key)
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(key);
	}
	
	public static long getLong(String key)
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getLong(key);
	}
}
