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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;
import it.cnr.istc.keen.epsl.launchers.BaseEpslLaunchConfigurationDelegate;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;

public class FbtExecLaunchConfigurationDelegate extends
        BaseEpslLaunchConfigurationDelegate
{
	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
	
	@Override
    protected void launchProcessMonitor(String ddlFile, String pdlFile,
    		String planArgs, boolean doExit, IRunModeExtension extHandler, Object extraData,
            IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process)
    {
        new FbtExecProcessMonitor(ddlFile, pdlFile, planArgs, doExit, sp, iocon, monitor, process);
    }
}
