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

import java.io.File;

public class EpslInstallImpl implements IEPSLInstall
{
    private String id;
    private String name;
    private File installLocation;

    public EpslInstallImpl(String id)
    {
        this.id = id;
    }
    
    public EpslInstallImpl(IEPSLInstall that)
    {
        this(that,null);
    }
    
    public EpslInstallImpl(IEPSLInstall that, String id)
    {
        this.id = id;
        this.name = that.getName();
        this.installLocation = that.getInstallLocation();
    }
    
    @Override
    public String getId()
    {
        return id;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public File getInstallLocation()
    {
        return installLocation;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setInstallLocation(File file)
    {
        this.installLocation = file;
    }
    
    private boolean nulleq(Object o1, Object o2)
    {
        if (o1 == null)
            return (o2==null);
        return o1.equals(o2);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (! (o instanceof EpslInstallImpl))
            return false;
        EpslInstallImpl that = (EpslInstallImpl)o;
        return nulleq(that.id,this.id) &&
                nulleq(that.name,that.name) &&
                nulleq(that.installLocation,that.installLocation);
    }

}
