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
package it.cnr.istc.keen.epsl.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface IRunModeExtension {
	public static String ID = "it.cnr.istc.keen.epsl.runmodeext";
	
    public Object computeData(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException;

	public String getPlannerCommand(Object extraData);
	
	public Runnable getAfterPlanningRunnable(Object extraData, IProgressMonitor monitor);
}
