/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *   
 * Inspired by org.eclipse.jdt.internal.debug.ui.jres.JREsUpdater
 */
package it.cnr.istc.keen.epsl;

import it.cnr.istc.keen.epsl.preferences.Preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class PlannersUpdater {
    private EpslContainer fOriginalPlanners;

    public PlannersUpdater() {
        fOriginalPlanners = new EpslContainer();
        IEPSLInstall def = EpslRegistry.getDefault();
        if (def != null) {
            fOriginalPlanners.setDefault(def);
        }
        
        for (IEPSLInstall epsl : EpslRegistry.getInstalledPlanners())
            fOriginalPlanners.addInstallation(epsl);
    }

    public boolean updatePlannersSettings(IEPSLInstall[] jres, IEPSLInstall defaultJRE) {
        EpslContainer newEntries = new EpslContainer();

        for (int i = 0; i < jres.length; i++)
            newEntries.addInstallation(jres[i]);

        newEntries.setDefault(defaultJRE);

        savePlannerDefinitions(newEntries);
        EpslRegistry.setNewPlanners(newEntries);

        return true;
    }

    private void savePlannerDefinitions(final EpslContainer entries)
    {
        IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
            {
                try
                {
                    monitor.beginTask("Save planner definitions", 100);
                    String xml = entries.getAsXML();
                    monitor.worked(40);
                    Preferences.setString(Preferences.EPSL_INSTALL_XML, xml);
                    monitor.worked(30);
                    //flush preferences?
                    monitor.worked(30);
                }
                catch (CoreException e) {}
                finally
                {
                    monitor.done();
                }
            }
        };
        try
        {
            Activator.getDefault().getWorkbench().getProgressService()
                    .busyCursorWhile(runnable);
        }
        catch (InvocationTargetException e) {
        	Activator.log(e);
        }
        catch (InterruptedException e) {
        	Activator.log(e);
        }
    }
}
