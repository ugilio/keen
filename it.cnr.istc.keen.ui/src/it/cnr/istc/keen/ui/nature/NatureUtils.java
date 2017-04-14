package it.cnr.istc.keen.ui.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

public class NatureUtils {
	
	public static void addNature(IProject project, String natureID) throws CoreException
	{
		if (!project.hasNature(natureID))
		{
			IProjectDescription description = project.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length]= natureID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
	}
}
