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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;

public class EpslLaunchConfigurationDelegate extends
        BaseEpslLaunchConfigurationDelegate
{
	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
	
	@Override
    protected void launchProcessMonitor(String ddlFile, String pdlFile,
    		String planArgs, boolean doExit, String exportFile,
            IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process)
    {
        new EpslProcessMonitor(ddlFile, pdlFile, planArgs, doExit, exportFile,
                sp, iocon, monitor, process);
    }
}
