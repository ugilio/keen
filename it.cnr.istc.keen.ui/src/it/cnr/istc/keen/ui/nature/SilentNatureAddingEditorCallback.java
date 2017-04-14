package it.cnr.istc.keen.ui.nature;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;
import org.eclipse.xtext.ui.editor.XtextEditor;

public class SilentNatureAddingEditorCallback extends IXtextEditorCallback.NullImpl {

	@Override
	public void afterCreatePartControl(XtextEditor editor) {
		IResource resource = editor.getResource();
		IProject project = resource != null ? resource.getProject() : null;
		if (resource != null && !XtextProjectHelper.hasNature(project) && project.isAccessible()
				&& !project.isHidden()) {
			try
			{
				NatureUtils.addNature(project, XtextProjectHelper.NATURE_ID);
			}
			catch (CoreException ex) {
				Logger.getLogger(SilentNatureAddingEditorCallback.class).
					error("Error toggling Xtext nature", ex);
			}
		}
	}
}