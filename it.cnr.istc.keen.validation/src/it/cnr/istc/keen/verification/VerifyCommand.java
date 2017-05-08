package it.cnr.istc.keen.verification;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import it.cnr.istc.keen.epsl.launchers.EpslRunShortcut;
import it.cnr.istc.keen.epsl.launchers.IEpslLaunchConfigurationConstants;

public class VerifyCommand extends AbstractHandler
{
    private EpslRunShortcut runShortCut = new EpslRunShortcut();
    
    private void launchLastConfiguration() throws ExecutionException
    {
        ILaunchConfiguration conf = DebugUITools.getLastLaunch(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
        try
        {
            if (conf == null || !IEpslLaunchConfigurationConstants.ID.equals(conf.getType().getIdentifier()))
            {
                ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
                ILaunchConfigurationType lt = lm.getLaunchConfigurationType(IEpslLaunchConfigurationConstants.ID);
                ILaunchConfiguration[] arr = lm.getLaunchConfigurations(lt);
                if (arr.length>0)
                    conf = arr[arr.length-1];
            }
        }
        catch (CoreException e)
        {
            throw new ExecutionException("Cannot verify plan: "+e.getMessage(),e);
        }
        if (conf == null)
            throw new ExecutionException("No launch configuration defined!");
        DebugUITools.launch(conf, "verify");
    }
    
    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
        IEditorPart part = HandlerUtil.getActiveEditor(event);
        if (part != null)
            runShortCut.launch(part, "verify");
        else
            launchLastConfiguration();
        
		return null;
	}
}
