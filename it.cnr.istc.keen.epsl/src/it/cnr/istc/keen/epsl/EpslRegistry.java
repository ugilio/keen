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
package it.cnr.istc.keen.epsl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import it.cnr.istc.keen.epsl.preferences.Preferences;

public class EpslRegistry
{
    public static final String EPSL_JAR_NAME = "epsl-cli.jar";
    private static EpslContainer container = null;
    private static Object lock = new Object();
    
    protected static void setNewPlanners(EpslContainer cont)
    {
        container = cont;
    }
    
    private static void initialize()
    {
        if (container != null)
            return;
        synchronized (lock)
        {
            EpslContainer newCont = new EpslContainer();
            String xml = Preferences.getString(Preferences.EPSL_INSTALL_XML);
            if (!xml.isEmpty())
            {
                try
                {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF8"));
                    EpslContainer.parseXMLIntoContainer(inputStream, newCont);
                }
                catch (IOException e) {}
            }
            container = newCont;
        }
    }
    
    public static IEPSLInstall[] getInstalledPlanners()
    {
        initialize();
        return container.getPlanners();
    }
    
    public static IEPSLInstall findById(String id)
    {
        if (id==null)
            return null;
        for (IEPSLInstall epsl : getInstalledPlanners())
            if (epsl.getId().equals(id))
                return epsl;
        return null;
    }
    
    public static IEPSLInstall findByName(String name)
    {
        if (name==null)
            return null;
        for (IEPSLInstall epsl : getInstalledPlanners())
            if (epsl.getName().equals(name))
                return epsl;
        return null;
    }
    
    public static IEPSLInstall findByPath(String path)
    {
        if (path==null)
            return null;
        File f = new File(path);
        for (IEPSLInstall epsl : getInstalledPlanners())
            if (epsl.getInstallLocation().equals(f))
                return epsl;
        return null;
    }
    
    public static IEPSLInstall getDefault()
    {
        initialize();
        return container.getDefault();
    }
}
