package it.cnr.istc.keen.verification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;
import it.cnr.istc.keen.epsl.launchers.IEpslLaunchConfigurationConstants;
import it.cnr.istc.keen.validation.Activator;

public class VerifyRunModeExtension implements IRunModeExtension {

	public VerifyRunModeExtension() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object computeData(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
        String projName = configuration.getAttribute(IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME, "unnamed");
        
        try
        {
        	Path path = Files.createTempDirectory("keen");
        	File dir = path.toFile();
        	dir.deleteOnExit();
        	String baseName = new File(dir,projName).getAbsolutePath();
        	new File(baseName).deleteOnExit();
        	new File(baseName+".xta").deleteOnExit();
        	new File(baseName+".q").deleteOnExit();
        	return baseName;
        }
        catch (IOException e)
        {
        	throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
        			"There was a problem during plan verification: "+e.getMessage(),e));
        }
	}

	@Override
	public String getPlannerCommand(Object extraData) {
		return "export "+extraData;
	}

	@Override
	public Runnable getAfterPlanningRunnable(Object extraData, IProgressMonitor monitor) {
		return new VerifierHandler(""+extraData, monitor);
	}

}
