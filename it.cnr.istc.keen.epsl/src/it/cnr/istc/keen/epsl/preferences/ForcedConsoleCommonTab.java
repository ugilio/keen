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
package it.cnr.istc.keen.epsl.preferences;

import java.lang.reflect.Field;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Button;

public class ForcedConsoleCommonTab extends CommonTab
{
    public boolean isValid(ILaunchConfiguration config)
    {
        try
        {
            Field field = CommonTab.class.getDeclaredField("fConsoleOutput");
            if (field != null)
            {
                field.setAccessible(true);
                Button btn = (Button)field.get(this);
                if (!btn.getSelection())
                {
                    setErrorMessage("'Allocate console' option must be checked.");
                    return false;
                }
            }
        }
        catch (SecurityException e) {}
        catch (NoSuchFieldException e) {}
        catch (IllegalAccessException e) {}
        return super.isValid(config);
    }
    
    public void setDefaults(ILaunchConfigurationWorkingCopy config)
    {
        config.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
        super.setDefaults(config);
    }

}
