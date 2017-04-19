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
package it.cnr.istc.keen.epsl.launchers;

public interface IEpslLaunchConfigurationConstants
{
    public static final String ID = "it.cnr.istc.keen.epsl.launchers.epsl"; 
    public static final String ATTR_PROJECT_NAME = ID + ".projectName";
    public static final String ATTR_DDL_FILE = ID + ".ddlFile";
    public static final String ATTR_PDL_FILE = ID + ".pdlFile";
    public static final String ATTR_ARGUMENTS = ID + ".arguments";
    public static final String ATTR_QUITPLANNER = ID + ".quitPlanner";
    
    public static final String ATTR_EPSL_INSTALL_PATH = ID +".epslInstallPath";
    
	public static final String EPSL_INSTALL_XML = "epslInstallXml";
}
