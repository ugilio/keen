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
package it.cnr.istc.keen.verification;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;

public class VerifierHandler implements Runnable
{
    private String p2tFile;
    private IProgressMonitor monitor;
    
    public VerifierHandler(String p2tFile, IProgressMonitor monitor)
    {
        this.p2tFile = p2tFile;
        this.monitor = monitor;
    }

    @Override
    public void run()
    {
        try
        {
            Plan2TigaLauncher p2tl = new Plan2TigaLauncher(p2tFile);
            p2tl.execute();
            VerificationTigaLauncher tl = new VerificationTigaLauncher(p2tFile, monitor);
            StatusType r = tl.execute();
			Display disp = PlatformUI.getWorkbench().getDisplay();
			disp.asyncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					if (r==StatusType.OK)
					{
						VerifyDialogOK d = new VerifyDialogOK(shell);
						d.create();
						d.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell,
								SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
						mb.setText("Plan verification");
						mb.setMessage("Plan verification FAILED");
						mb.open();
					}
				}
			});
        }
        catch (CoreException e)
        {
            StatusManager.getManager().handle(e.getStatus(),StatusManager.SHOW);
        }
    }

}
