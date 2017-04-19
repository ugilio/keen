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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;

public class EpslProcessMonitor extends BaseEpslProcessMonitor
{
    protected String initCmd;
    protected String planArgs;
    protected boolean doExit;
    protected String exportFile;
	
	public EpslProcessMonitor(String ddlFile, String pdlFile, String planArgs, boolean doExit,
			String exportFile, IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor,
			IProcess process) {
		super(ddlFile, pdlFile, planArgs, doExit, exportFile, sp, iocon, monitor, process);
	}

	@Override
    protected String getPrompt()
    {
    	return "epsl-agent$";
    }
	
	@Override
    protected void initArgs(String ddlFile, String pdlFile, String planArgs, boolean doExit,
    		String exportFile, IStreamsProxy sp, IProgressMonitor monitor, IProcess process)
    {
        this.sp = sp;
        this.monitor = monitor;
        this.planArgs = planArgs;
        this.doExit = doExit;
        this.process = process;
        this.exportFile = "".equals(exportFile) ? null : exportFile;
        if (this.exportFile!=null)
            this.doExit=true;
        initCmd = String.format("init %s %s%n", ddlFile,pdlFile);
    }
    
	@Override
    protected void sendCommands() throws IOException
    {
        waitPrompt();
        con.write(initCmd);

        if (monitor.isCanceled())
            return;

        String planCmd = String.format("plan %s%n", planArgs);
        waitPrompt();
        con.write(planCmd);

        if (monitor.isCanceled())
            return;

        waitPrompt();
        if (exportFile!=null)
            con.write(String.format("export %s%n", exportFile));
        else
            con.write(String.format("display%n"));

        if (doExit)
        {
            waitPrompt();
            con.write(String.format("exit%n"));
        }

        monitor.worked(1);
        
        //FIXME: handle VerifierHandler
//        if (exportFile!=null)
//            PlatformUI.getWorkbench().getDisplay().asyncExec(
//                    new VerifierHandler(exportFile));
    }
}
