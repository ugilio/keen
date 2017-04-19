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
 * Inspired from org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab
 */
package it.cnr.istc.keen.epsl.launchers;

import java.io.File;
import java.util.ArrayList;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.utils.ProjectUtils;
import it.cnr.istc.keen.epsl.utils.SWTFactory;
import it.cnr.istc.keen.ui.nature.DdlNature;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

import com.google.inject.Inject;

public class MainLaunchConfigurationTab extends AbstractLaunchConfigurationTab
{
    protected Text fProjText;
    protected Text fDdlText;
    protected Text fPdlText;
    protected Text fArgsText;
    protected Button fClosePlannerButton;
    
    @Inject
    private ILabelProvider labelProvider;
    
    private ProjectUtils projectUtils = new ProjectUtils();
    
    private ConfigurationData confData;
    
    public MainLaunchConfigurationTab(ConfigurationData confData)
    {
    	super();
    	this.confData = confData;
    	Activator.getDefault().getInjector().injectMembers(this);
    }
    
    protected boolean needsPdl()
    {
    	return confData.needsPdl();
    }
    
    protected boolean needsClosePlanner()
    {
    	return confData.needsClosePlanner();
    }
    
    private boolean needsMiscOptions()
    {
    	return needsClosePlanner();
    }
    
    protected void createProjectEditor(Composite parent)
    {
        Group group = SWTFactory.createGroup(parent, "&Project:", 2, 1, GridData.FILL_HORIZONTAL);
        fProjText = SWTFactory.createSingleText(group, 1);
        fProjText.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                updateLaunchConfigurationDialog();
            }
        });
        Button projButton = createPushButton(group, "&Browse...", null); 
        projButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                projButtonClicked();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }
    
    private Group createDlFileEditor(Composite parent, String text)
    {
        Group group = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL);
        final Text t = SWTFactory.createSingleText(group, 1);
        t.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                updateLaunchConfigurationDialog();
            }
        });
        Button b = createPushButton(group, "&Search...", null);
        b.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                searchButtonClicked(t);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        return group;
    }
    
    //FIXME: implement me
    private class TempLabelProvider extends LabelProvider
    {
        public String getText(Object element)
        {
            if (element instanceof IResource)
                return ((IResource)element).getName();
            return element.toString();
        }
    }
    
    private IProject[] getDdlProjects()
    {
        IProject allProjects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        ArrayList<IProject> projects = new ArrayList<IProject>(allProjects.length);
        for (IProject p : allProjects)
        {
            try
            {
                if (p.hasNature(DdlNature.ID))
                    projects.add(p);
            }
            catch (CoreException e) {}
        }
        IProject arr[] = new IProject[projects.size()];
        return projects.toArray(arr);
    }
    
    private IProject getCurrentProject()
    {
        String name = fProjText.getText().trim();
        if (name.length() < 1)
            return null;
        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        try
        {
            if (p != null && p.hasNature(DdlNature.ID))
                return p;
        }
        catch (CoreException e) {}
        return null;
    }
    
    private IProject chooseDdlProject()
    {
        ILabelProvider labelProvider= new TempLabelProvider();
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setTitle("Project Selection"); 
        dialog.setMessage("Select a project to constrain your search."); 
        dialog.setElements(getDdlProjects());
        IProject currProject= getCurrentProject();
        if (currProject != null)
            dialog.setInitialSelections(new Object[] { currProject });
        if (dialog.open() == Window.OK)
            return (IProject) dialog.getFirstResult();
        return null;        
    }

    private IFile chooseFile(String extension, String title)
    {
        IContainer root = getCurrentProject();
        if (root == null)
            root = ResourcesPlugin.getWorkspace().getRoot();
        FilteredResourcesSelectionDialog dialog =
                new FileExtensionSelectionDialog(getShell(), false,
                        root, IResource.FILE, extension);
        dialog.setTitle(title);
        dialog.setMessage("Select &type (? = any character, * = any String):");
        dialog.setInitialPattern("**");
        dialog.setListLabelProvider(labelProvider);
        
        if (dialog.open() == Window.CANCEL)
            return null;
        Object[] results = dialog.getResult();
        if (results != null && results[0] instanceof IFile)
             return (IFile)results[0];
        return null;
    }
    
    private void searchButtonClicked(Text associatedText)
    {
        if (associatedText == fDdlText)
        {
            IFile f = chooseFile("ddl", "Select DDL file");
            fDdlText.setText(f.getProjectRelativePath().toString());
            fProjText.setText(f.getProject().getName());
        }
        else if (associatedText == fPdlText)
        {
            IFile f = chooseFile("pdl", "Select PDL file");
            fPdlText.setText(f.getProjectRelativePath().toString());
            fProjText.setText(f.getProject().getName());
        }
    }
    
    private void projButtonClicked()
    {
        IProject p = chooseDdlProject();
        if (p != null)
            fProjText.setText(p.getName());
    }
    
    private void createDdlFileEditor(Composite parent)
    {
        Group g = createDlFileEditor(parent, "&Domain Description File:");
        fDdlText = (Text)g.getChildren()[0];
    }
    
    private void createPdlFileEditor(Composite parent)
    {
        Group g = createDlFileEditor(parent, "&Problem Description File:");
        fPdlText = (Text)g.getChildren()[0];
        if (!needsPdl())
        	g.setVisible(false);
    }
    
    protected void createArgsEditor(Composite parent)
    {
        Group group = SWTFactory.createGroup(parent, confData.get(
        		ConfigurationData.PLANNER_ARGUMENTS), 2, 1, GridData.FILL_HORIZONTAL);
        fArgsText = SWTFactory.createSingleText(group, 1);
        fArgsText.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                updateLaunchConfigurationDialog();
            }
        });
    }
    
    protected void createMiscOptions(Composite parent)
    {
        Group group = SWTFactory.createGroup(parent, "Miscellaneous &options:", 2, 1, GridData.FILL_HORIZONTAL);
        fClosePlannerButton = SWTFactory.createCheckButton(group, "Quit the planner on completion", null, true, 5);
        fClosePlannerButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                updateLaunchConfigurationDialog();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        if (!needsMiscOptions())
        	group.setVisible(false);
    }
    
    @Override
    public void createControl(Composite parent)
    {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
        ((GridLayout)comp.getLayout()).verticalSpacing = 0;
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        createDdlFileEditor(comp);
        if (needsPdl())
        	createVerticalSpacer(comp, 1);
        createPdlFileEditor(comp);
        createVerticalSpacer(comp, 1);
        createArgsEditor(comp);
        if (needsMiscOptions())
        	createVerticalSpacer(comp, 1);
        createMiscOptions(comp);
        setControl(comp);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy conf)
    {
        conf.setAttribute(IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
        conf.setAttribute(IEpslLaunchConfigurationConstants.ATTR_DDL_FILE, "");
        conf.setAttribute(IEpslLaunchConfigurationConstants.ATTR_PDL_FILE, "");
        conf.setAttribute(IEpslLaunchConfigurationConstants.ATTR_ARGUMENTS, "");
        conf.setAttribute(IEpslLaunchConfigurationConstants.ATTR_QUITPLANNER, true);
    }

    private void initField(ILaunchConfiguration conf, Text control, String attribute, String dflt)
    {
        try
        {
            control.setText(conf.getAttribute(attribute,dflt));
        }
        catch(CoreException ce)
        {
            control.setText(dflt);
        }
    }
    
    private void initField(ILaunchConfiguration conf, Button control, String attribute, boolean dflt)
    {
        try
        {
            control.setSelection(conf.getAttribute(attribute, dflt));
        }
        catch(CoreException ce)
        {
            control.setSelection(dflt);
        }
    }
    
    private String guessProjectName()
    {
        try
        {
            IProject p = projectUtils.getCurrentProject();
            if (p != null && p.hasNature(DdlNature.ID))
                return p.getName();
        }
        catch (CoreException e) {}
        return "";
    }
    
    private String guessDdlName()
    {
        try
        {
            IFile curFile = projectUtils.getCurrentFile();
            if (curFile!=null)
                return curFile.getProjectRelativePath().toOSString();
                
            IProject p = getCurrentProject();
            if ( p == null)
                return "";
            for (IResource r : p.members())
                if (r instanceof IFile)
                    if (r.getName().toLowerCase().endsWith(".ddl"))
                        return r.getProjectRelativePath().toOSString();
        }
        catch (CoreException e) {}
        return "";
    }
    
    private String guessPdlName()
    {
        String baseName = fDdlText.getText().trim();
        String origName = baseName;
        if (baseName.toLowerCase().endsWith(".ddl"))
            baseName = baseName.substring(0,baseName.length()-4);
        String fname = baseName;
        if (!fname.isEmpty())
            fname+=".pdl";
        IProject p = getCurrentProject();
        if (p == null)
        	return "";
        if (p.getFile(fname).exists())
            return fname;

        IFile orig = p.getFile(origName);
        IContainer parent = orig.getParent();
        if (parent==null)
            parent = p;
        baseName=baseName.toLowerCase();
        IResource lastResort = null;
        try
        {
            for (IResource r : parent.members())
                if (r instanceof IFile)
                {
                    fname = r.getProjectRelativePath().toOSString().toLowerCase();
                    //fname = r.getName().toLowerCase();
                    if (fname.endsWith(".pdl"))
                    {
                        if (fname.contains(baseName))
                            return r.getProjectRelativePath().toOSString();
                        if (lastResort == null)
                            lastResort = r;
                    }
                }
        }
        catch (CoreException e) {}
        
        if (lastResort != null)
            return lastResort.getProjectRelativePath().toOSString();
        
        return "";
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration conf)
    {
        initField(conf, fProjText, IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
        initField(conf, fDdlText, IEpslLaunchConfigurationConstants.ATTR_DDL_FILE, "");
        initField(conf, fPdlText, IEpslLaunchConfigurationConstants.ATTR_PDL_FILE, "");
        initField(conf, fArgsText, IEpslLaunchConfigurationConstants.ATTR_ARGUMENTS, "");
        initField(conf, fClosePlannerButton, IEpslLaunchConfigurationConstants.ATTR_QUITPLANNER, true);
        
        if (fProjText.getText().trim().isEmpty())
            fProjText.setText(guessProjectName());
        if (fDdlText.getText().trim().isEmpty())
            fDdlText.setText(guessDdlName());
        if (fPdlText.getText().trim().isEmpty())
            fPdlText.setText(guessPdlName());
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration)
    {
        configuration.setAttribute(
                IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                fProjText.getText().trim());
        configuration.setAttribute(
                IEpslLaunchConfigurationConstants.ATTR_DDL_FILE,
                fDdlText.getText().trim());
        configuration.setAttribute(
                IEpslLaunchConfigurationConstants.ATTR_PDL_FILE,
                fPdlText.getText().trim());
        configuration.setAttribute(
                IEpslLaunchConfigurationConstants.ATTR_ARGUMENTS,
                fArgsText.getText().trim());
        configuration.setAttribute(
                IEpslLaunchConfigurationConstants.ATTR_QUITPLANNER,
                fClosePlannerButton.getSelection());
    }

    private boolean checkValidProject()
    {
        String name = fProjText.getText().trim();
        if (name.length() <= 0)
        {
            fProjText.setFocus();
            setErrorMessage("A project is required");
            return false;
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IStatus status = workspace.validateName(name, IResource.PROJECT);
        if (status.isOK())
        {
            IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
            if (!project.exists())
            {
                setErrorMessage(String.format("Project %s does not exist", name)); 
                return false;
            }
            if (!project.isOpen())
            {
                setErrorMessage(String.format("Project %s is closed",name)); 
                return false;
            }
        }
        else
        {
            setErrorMessage(String.format("Illegal project name: %s",status.getMessage())); 
            return false;
        }
        return true;
    }
    
    private boolean checkValidFile(Text field)
    {
        String projName = fProjText.getText().trim();
        String name = field.getText().trim();
        if (name.length() <= 0)
        {
            field.setFocus();
            setErrorMessage("File name is required");
            return false;
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        String nameOnly = new File(name).getName();
        //String path = project.getFile(name).getProjectRelativePath().toString();
        //IStatus status = workspace.validatePath(path, IResource.FILE);
        IStatus status = workspace.validateName(nameOnly, IResource.FILE);
        if (status.isOK())
        {
            IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
            IFile file = project.getFile(name);
            if (!file.exists())
            {
                setErrorMessage(String.format("File %s does not exist", name)); 
                return false;
            }
        }
        else
        {
            setErrorMessage(String.format("Illegal file name: %s",status.getMessage())); 
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isValid(ILaunchConfiguration launchConfig)
    {
        setErrorMessage(null);
        setMessage(null);
        if (!checkValidProject())
            return false;
        if (!checkValidFile(fDdlText))
            return false;
        if (needsPdl() && !checkValidFile(fPdlText))
            return false;
        return true;        
    }
    
    @Override
    public String getName()
    {
        return "Main";
    }
    
    @Override
    public Image getImage()
    {
        return null;
    }

    @Override
    public String getId()
    {
        return "it.cnr.istc.keen.epsl.launchers.mainLaunchConfigurationTab";
    }

    
    
}
