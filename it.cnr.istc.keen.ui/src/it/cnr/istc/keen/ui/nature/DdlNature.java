package it.cnr.istc.keen.ui.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class DdlNature implements IProjectNature {

	public static String ID = "it.cnr.istc.keen.ui.nature.DdlNature"; 
	
	private IProject project;

	@Override
	public void configure() throws CoreException
	{

	}

	@Override
	public void deconfigure() throws CoreException
	{

	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}

}
