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
