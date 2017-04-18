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