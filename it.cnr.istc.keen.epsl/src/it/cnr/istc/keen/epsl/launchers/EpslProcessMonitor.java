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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.EpslConfigurationData;
import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;

public class EpslProcessMonitor extends BaseEpslProcessMonitor
{
    protected String initCmd;
    protected String planArgs;
    protected boolean doExit;
    protected Object extraData;
    protected IRunModeExtension extHandler;
	
	public EpslProcessMonitor(String ddlFile, String pdlFile, String planArgs, boolean doExit,
			IRunModeExtension extHandler, Object extraData, IStreamsProxy sp,
			IOConsole iocon, IProgressMonitor monitor, IProcess process) {
		super(ddlFile, pdlFile, planArgs, doExit, extHandler, extraData, sp, iocon, monitor, process);
	}

	@Override
    protected String getPrompt()
    {
    	return "epsl-agent$";
    }
	
	@Override
    protected void initArgs(String ddlFile, String pdlFile, String planArgs, boolean doExit,
    		IRunModeExtension extHandler, Object extraData, IStreamsProxy sp, IProgressMonitor monitor, IProcess process)
    {
        this.sp = sp;
        this.monitor = monitor;
        this.planArgs = planArgs;
        this.doExit = doExit;
        this.process = process;
        this.extHandler = extHandler;
        this.extraData = extraData == null ? "" : extraData;
        if (this.extraData.equals(""))
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
        if (extHandler != null)
        	con.write(String.format("%s%n",extHandler.getPlannerCommand(extraData)));
        else
            con.write(String.format("display%n"));

        if (doExit)
        {
            waitPrompt();
            con.write(String.format("exit%n"));
        }

        monitor.worked(1);
        
        if (extHandler!=null)
        	executeExtension();
    }
	
	private void executeExtension()
	{
		ISafeRunnable runnable = new ISafeRunnable() {
            @Override
            public void handleException(Throwable e) {
            	Activator.log(e);
            }

            @Override
            public void run() throws Exception {
            	Job job = new Job("Plan Verification") {
					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
                		extHandler.getAfterPlanningRunnable(extraData, monitor).run();            	
						return Status.OK_STATUS;
					}
				};
				job.schedule();
            }
		};
		SafeRunner.run(runnable);		
	}

	@Override
	protected ConfigurationData getConfigurationData() {
		return EpslConfigurationData.getInstance();
	}
}
