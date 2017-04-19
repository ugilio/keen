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

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.preferences.EPSLTab;
import it.cnr.istc.keen.epsl.preferences.ForcedConsoleCommonTab;

public class EpslLaunchConfigurationTabGroup extends
        AbstractLaunchConfigurationTabGroup
{

    public EpslLaunchConfigurationTabGroup()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
    	ConfigurationData confData = EpslConfigurationData.getInstance();
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new MainLaunchConfigurationTab(confData),
                new EPSLTab(),
                new JavaJRETab(),
                new ForcedConsoleCommonTab()
        };
        setTabs(tabs);
    }

}
