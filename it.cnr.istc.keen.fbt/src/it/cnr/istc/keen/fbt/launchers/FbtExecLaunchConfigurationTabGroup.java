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
package it.cnr.istc.keen.fbt.launchers;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.launchers.MainLaunchConfigurationTab;
import it.cnr.istc.keen.epsl.preferences.ForcedConsoleCommonTab;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;
import it.cnr.istc.keen.fbt.preferences.FbtExecTab;

public class FbtExecLaunchConfigurationTabGroup extends
        AbstractLaunchConfigurationTabGroup
{

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
    	ConfigurationData confData = FbtExecConfigurationData.getInstance();
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new MainLaunchConfigurationTab(confData),
                new FbtExecTab(),
                new JavaJRETab(),
                new ForcedConsoleCommonTab()
        };
        setTabs(tabs);
    }

}
