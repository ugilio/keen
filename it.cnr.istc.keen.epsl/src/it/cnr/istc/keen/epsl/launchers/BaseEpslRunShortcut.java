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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.google.inject.Inject;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.ConfigurationData;

public abstract class BaseEpslRunShortcut implements ILaunchShortcut
{
	protected String launchConfigurationType;
	
	@Inject
	private ILabelProvider ddlLabelProvider;
	
	public BaseEpslRunShortcut() {
		super();
		Activator.getDefault().getInjector().injectMembers(this);
	}
	
	protected abstract ConfigurationData getcConfigurationData();
	
    private Shell getShell()
    {
        return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
    }
    
    private IResource chooseFile(Object[] elements, String title)
    {
        Shell shell = getShell();
        
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                shell,ddlLabelProvider);
        
        dialog.setTitle(title);
        dialog.setMessage("Select &file (? = any character, * = any String):");
        dialog.setElements(elements);
        
        if (dialog.open() == Window.CANCEL)
            return null;
        Object[] results = dialog.getResult();
        if (results != null && results[0] instanceof IResource)
             return (IResource)results[0];
        return null;
    }
    
    private IResource chooseDdlFile(Object[] elements)
    {
        return chooseFile(elements, "Select Domain File");
    }
    
    private IResource choosePdlFile(Object[] elements)
    {
        return chooseFile(elements, "Select Problem File");
    }
    
    //FIXME
    private class TempLabelProvider extends LabelProvider
    {
        public String getText(Object element)
        {
            if (element instanceof ILaunchConfiguration)
                return ((ILaunchConfiguration)element).getName();
            return super.getText(element);
        }
    }
    
    private ILaunchConfiguration chooseConfiguration(Object[] elements) throws CoreException
    {
        Shell shell = getShell();
        
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                shell,new TempLabelProvider());
        
        dialog.setTitle("Select Configuration");
        dialog.setMessage("Select a configuration to launch:");
        dialog.setElements(elements);
        
        if (dialog.open() == Window.CANCEL)
            return null;
        Object[] results = dialog.getResult();
        if (results != null && results[0] instanceof ILaunchConfiguration)
             return (ILaunchConfiguration)results[0];
        return null;
    }
    
    private ILaunchConfiguration[] getCompatibleConfigurations(IResource ddl, IResource pdl) throws CoreException
    {
        IProject proj = ddl == null ? pdl.getProject() : ddl.getProject();
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType lt = lm.getLaunchConfigurationType(launchConfigurationType);
        ILaunchConfiguration[] configs = lm.getLaunchConfigurations(lt);
        ArrayList<ILaunchConfiguration> outConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
        for (ILaunchConfiguration conf : configs)
        {
            String projName = conf.getAttribute(IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME,"");
            String cDdl = conf.getAttribute(IEpslLaunchConfigurationConstants.ATTR_DDL_FILE,"");
            String cPdl = conf.getAttribute(IEpslLaunchConfigurationConstants.ATTR_PDL_FILE,"");
            
            if (!projName.equals(proj.getName()))
                continue;
            
            if (ddl != null && !proj.getFile(cDdl).equals(ddl))
                continue;
            if (pdl != null && !proj.getFile(cPdl).equals(pdl))
                continue;

            outConfigs.add(conf);
        }
        return outConfigs.toArray(new ILaunchConfiguration[outConfigs.size()]);
    }
    
    private IResource guessOtherFileName(IResource other)
    {
        String baseName = other.getProjectRelativePath().toOSString();
        String myExt = null;
        if (baseName.toLowerCase().endsWith(".ddl"))
            myExt = ".pdl";
        else if (baseName.toLowerCase().endsWith(".pdl"))
            myExt = ".ddl";
        else
            return null;
        
        baseName = baseName.substring(0,baseName.length()-4);
        String fname = baseName;
        if (!fname.isEmpty())
            fname+=myExt;
        IProject p = other.getProject();
        IFile f = p.getFile(fname);
        if (f.exists())
            return f;
        
        return null;
    }
    
    private ILaunchConfiguration generateConfiguration(IResource ddl, IResource pdl,
            String mode) throws CoreException
    {
        String baseName = ddl != null ? ddl.getName() : pdl.getName();
        int idx = baseName.lastIndexOf('.');
        if (idx >= 0)
            baseName = baseName.substring(0,idx);
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType lt = lm.getLaunchConfigurationType(launchConfigurationType);
        ILaunchConfigurationWorkingCopy wc = lt.newInstance(null,
                DebugPlugin.getDefault().getLaunchManager().
                generateLaunchConfigurationName(baseName));
        
        IProject proj = ddl != null ? ddl.getProject() : pdl.getProject();
        
        if (!(mode.equals("run") || mode.equals("debug") || mode.equals("verify")))
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    String.format("Unknown mode: "+mode)));
        
        wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
        
        wc.setAttribute(IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                proj.getName());
        if (ddl != null)
            wc.setAttribute(IEpslLaunchConfigurationConstants.ATTR_DDL_FILE,
                    ddl.getProjectRelativePath().toString());
        if (pdl != null)
            wc.setAttribute(IEpslLaunchConfigurationConstants.ATTR_PDL_FILE,
                    pdl.getProjectRelativePath().toString());
        ILaunchConfiguration config = wc.doSave();
        String launchGroup =
                mode.equals("debug") ? IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP :
                    IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
        
        boolean isVerify = mode.equals("verify");
        int retVal = isVerify ?
                DebugUITools.openLaunchConfigurationPropertiesDialog(getShell(), config,
                        launchGroup) :
                DebugUITools.openLaunchConfigurationDialog(getShell(), config,
                        launchGroup, null);
                            
        if (retVal==Window.CANCEL)
        {
            //delete the just-created configuration
            config.delete();
            return null;
        }
        if (isVerify)
            return config;
        //return null because if user clicks "run", it will be run! We don't
        //want to run it twice
        return null;
    }
    
    private void launch(Object[] elements, String mode)
    {
        IResource ddlFile = null;
        IResource pdlFile = null;
        
        try
        {
            for (Object o : elements)
            {
                if (! (o instanceof IResource))
                    continue;
                String name = ((IResource)o).getName().toLowerCase();
                if (name.endsWith(".ddl"))
                {
                    if (ddlFile != null)
                    {
                        ddlFile=chooseDdlFile(elements);
                        break;
                    }
                    ddlFile = (IResource)o;
                }
                if (name.endsWith(".pdl"))
                {
                    if (pdlFile != null)
                    {
                        pdlFile=choosePdlFile(elements);
                        break;
                    }
                    pdlFile = (IResource)o;
                }
            }

            if (ddlFile == null && pdlFile == null)
                throw new Exception(getcConfigurationData().get(ConfigurationData.NO_RESOURCES));
            
            ILaunchConfiguration[] configs1 = getCompatibleConfigurations(ddlFile, pdlFile);
            if (ddlFile == null)
                ddlFile = guessOtherFileName(pdlFile);
            if (pdlFile == null)
                pdlFile = guessOtherFileName(ddlFile);
            ILaunchConfiguration[] configs2 = getCompatibleConfigurations(ddlFile, pdlFile);
            ILaunchConfiguration chosenConfig = null;
            if (configs2.length==1)
                chosenConfig = configs2[0];
            else if (configs1.length==1)
                chosenConfig = configs1[0];
            else if (configs1.length==0)
                chosenConfig = generateConfiguration(ddlFile,pdlFile,mode);
            else
                chosenConfig = chooseConfiguration(configs1);
            if (chosenConfig != null)
                DebugUITools.launch(chosenConfig, mode);
        }
        catch (Exception e)
        {
            MessageDialog.openError(getShell(), "Launch error", e.getMessage());
        }
    }
    
    
    @Override
    public void launch(ISelection selection, String mode)
    {
        if (selection instanceof IStructuredSelection)
            launch(((IStructuredSelection)selection).toArray(), mode);
    }

    @Override
    public void launch(IEditorPart editor, String mode)
    {
        IEditorInput input = editor.getEditorInput();
        if (input==null)
            return;
        Object ada = input.getAdapter(IFile.class);
        if (ada != null)
            launch(new Object[]{ada}, mode);
    }

}
