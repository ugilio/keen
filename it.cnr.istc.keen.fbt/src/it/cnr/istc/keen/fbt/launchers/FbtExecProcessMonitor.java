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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;

import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;
import it.cnr.istc.keen.epsl.launchers.BaseEpslProcessMonitor;
import it.cnr.istc.keen.fbt.FbtExecConfigurationData;

public class FbtExecProcessMonitor extends BaseEpslProcessMonitor
{
    protected String initCmd;
    protected String execArgs;
	
	public FbtExecProcessMonitor(String ddlFile, String pdlFile, String planArgs, boolean doExit,
			IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process) {
		super(ddlFile, pdlFile, planArgs, doExit, null, null, sp, iocon, monitor, process);
	}

	@Override
    protected String getPrompt()
    {
    	return "FbT$";
    }

	@Override
    protected void initArgs(String ddlFile, String pdlFile, String planArgs,
            boolean doExit, IRunModeExtension extHandler, Object extraData, IStreamsProxy sp, IProgressMonitor monitor,
            IProcess process)
    {
        this.sp = sp;
        this.monitor = monitor;
        this.execArgs = String.format("%s%n",planArgs);
        this.process = process;
        initCmd = String.format("set-preference %s%n", ddlFile);
    }
	
	@Override	
    protected void beforeThreadStart(Thread t)
    {
		ProcessMonitorRegistry.addMonitor(process, this);
    }
    
	@Override
    protected void sendCommands() throws IOException
    {
		if (true == false)
		{
        waitPrompt();
        con.write(initCmd);

        if (monitor.isCanceled())
            return;
		}

        waitPrompt();
        con.write(execArgs);

        if (monitor.isCanceled())
            return;

        monitor.worked(1);
    }

	@Override
	protected ConfigurationData getConfigurationData() {
		return FbtExecConfigurationData.getInstance();
	}
}
