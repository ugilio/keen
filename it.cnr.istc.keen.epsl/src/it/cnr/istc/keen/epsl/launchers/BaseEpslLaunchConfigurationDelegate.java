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

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.BaseEpslRegistry;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;

public abstract class BaseEpslLaunchConfigurationDelegate extends
        AbstractJavaLaunchConfigurationDelegate
{
	private class JarInfo
	{
		String[] classPath;
		String mainClass;
	}
	
    protected BaseEpslRegistry getEpslRegistry()
    {
    	return getConfigurationData().getEpslRegistry();
    }
    
    protected abstract ConfigurationData getConfigurationData();
    
    protected abstract void launchProcessMonitor(String ddlFile, String pdlFile,
    		String planArgs, boolean doExit, IRunModeExtension extHandler, Object extraData,
            IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process);
    
    private String[] resolveJarClasspath(File jarFile, String cp)
    {
        if (cp!=null)
            cp=cp.trim();
        if (cp == null || cp.isEmpty())
            return new String[0];
        String baseDir = jarFile.getParentFile().getAbsolutePath();
        String[] inArr = cp.split("\\s");
        ArrayList<String> outArr = new ArrayList<String>(inArr.length+1);
        outArr.add(jarFile.getAbsolutePath());
        for (String s : inArr)
            if (!s.equals("."))
                outArr.add(new File(baseDir,s).getAbsolutePath());
        
        return outArr.toArray(new String[outArr.size()]);
    }
    
    private JarInfo readJar(File file) throws CoreException
    {
        try
        {
            JarFile jar = new JarFile(file);
            try
            {
                Manifest mf = jar.getManifest();
                Attributes attrs = mf.getMainAttributes();
                String mainClass = attrs.getValue("Main-Class");
                String classPath = attrs.getValue("Class-Path");
                JarInfo info = new JarInfo();
                info.mainClass = mainClass;
                info.classPath = resolveJarClasspath(file, classPath);
                return info;
            }
            finally
            {
                jar.close();
            }
        }
        catch (IOException e)
        {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    String.format("Cannot read file %s: %s",
                            file.toString(),e.getMessage())));
        }
    }
    
    private JarInfo getEpslInfo(ILaunchConfiguration configuration) throws CoreException
    {
    	ConfigurationData confData = getConfigurationData();
        String plannerPath = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_EPSL_INSTALL_PATH,
                (String)null);
        IEPSLInstall epsl = null;
        if (plannerPath == null)
            epsl = getEpslRegistry().getDefault();
        else
            epsl = getEpslRegistry().findByPath(plannerPath);
        if (epsl == null)
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
            		confData.get(ConfigurationData.NO_PLANNER_FOUND)));
        File baseDir = epsl.getInstallLocation();
        File jarFile = new File(baseDir,getEpslRegistry().getJarName());
        return readJar(jarFile);
    }
    
    public String[] getClasspath(ILaunchConfiguration configuration,
            String[] jarClassPath) throws CoreException
    {
        String[] std = getClasspath(configuration);
        String[] dst = new String[std.length+jarClassPath.length];
        System.arraycopy(std, 0, dst, 0, std.length);
        System.arraycopy(jarClassPath, 0, dst, std.length, jarClassPath.length);
        return dst;
    }

    protected void doLaunch(ILaunchConfiguration configuration, String mode, IRunModeExtension ext,
            Object extraData, ILaunch launch, IProgressMonitor monitor)
                    throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask(configuration.getName(), 3);
        if (monitor.isCanceled())
            return;

        try
        {
            monitor.subTask("Verifying launch attributes...");
            
            JarInfo info = getEpslInfo(configuration);
            
            String mainTypeName = info.mainClass;
            IVMRunner runner = getVMRunner(configuration, mode);

            File workingDir = verifyWorkingDirectory(configuration);
            String workingDirName = null;
            if (workingDir != null)
                workingDirName = workingDir.getAbsolutePath();

            // Environment variables
            String[] envp = getEnvironment(configuration);

            // Program & VM arguments
            String pgmArgs = getProgramArguments(configuration);
            String vmArgs = getVMArguments(configuration);
            ExecutionArguments execArgs = new ExecutionArguments(vmArgs,
                    pgmArgs);

            // VM-specific attributes
            Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

            // Classpath
            String[] classpath = getClasspath(configuration, info.classPath);

            // Create VM config
            VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
                    mainTypeName, classpath);
            runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
            runConfig.setEnvironment(envp);
            runConfig.setVMArguments(execArgs.getVMArgumentsArray());
            runConfig.setWorkingDirectory(workingDirName);
            runConfig.setVMSpecificAttributesMap(vmAttributesMap);

            // Bootpath
            runConfig.setBootClassPath(getBootpath(configuration));

            // check for cancellation
            if (monitor.isCanceled())
                return;

            // done the verification phase
            monitor.worked(1);

            // Launch the configuration - 1 unit of work
            runner.run(runConfig, launch, monitor);

            // check for cancellation
            if (monitor.isCanceled())
                return;
            
            sendCommands(configuration, ext, extraData, launch, monitor);
        }
        finally
        {
            monitor.done();
        }
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException
    {
    	IRunModeExtension ext = Activator.getDefault().getExtensions().get(mode);
    	Object extraData = null;
    	if (ext != null) {
    		extraData = ext.computeData(configuration, mode, launch, monitor);
    		mode="run";
    	}
        doLaunch(configuration, mode, ext, extraData, launch, monitor);
    }
    
    private IOConsole findConsole(IProcess target)
    {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager manager = plugin.getConsoleManager();
        for (IConsole con : manager.getConsoles())
        {
            if (! (con instanceof IOConsole))
                continue;
            IOConsole iocon = ((IOConsole)con);
            IProcess proc = (IProcess)iocon.getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
            if (proc == target)
                return iocon;
        }
        return null;
    }
    
    private void sendCommands(ILaunchConfiguration configuration,
    		IRunModeExtension extHandler, Object extraData,
            ILaunch launch, final IProgressMonitor monitor) throws CoreException
    {
        String projName = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
        String ddlFile = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_DDL_FILE, "");
        String pdlFile = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_PDL_FILE, "");
        final String planArgs = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_ARGUMENTS, "");
        boolean doExit = configuration.getAttribute(
                IEpslLaunchConfigurationConstants.ATTR_QUITPLANNER,true);
        
        IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
        if (proj == null)
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    String.format("Project %s not found",projName)));
        
        ddlFile = proj.getFile(ddlFile).getRawLocation().makeAbsolute().toOSString();
        if (getConfigurationData().needsPdl())
        	pdlFile = proj.getFile(pdlFile).getRawLocation().makeAbsolute().toOSString();
        
        IProcess[] processes = launch.getProcesses();
        if (processes == null || processes.length != 1)
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
            		"Process does not exist!"));
        IProcess p = processes[0];
        final IStreamsProxy sp = p.getStreamsProxy();
        if (sp == null)
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Streams proxy is not supported for process!"+
                    "Please ensure 'Allocate console' option is checked"+
                    "under Common tab of launch configuration properties."));

        if (monitor.isCanceled())
            return;
        
        IOConsole con = findConsole(p);
        
        launchProcessMonitor(ddlFile, pdlFile, planArgs, doExit, extHandler, extraData,
                sp, con, monitor, p);
    }
}
