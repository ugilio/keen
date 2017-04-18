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
package it.cnr.istc.keen.epsl.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ProjectUtils {

	public ProjectUtils() {
		// TODO Auto-generated constructor stub
	}
	
	private IResource getResource(IStructuredSelection ss) {
        Object element = ss.getFirstElement();
        if (element instanceof IResource)
           return (IResource) element;
        if (!(element instanceof IAdaptable))
           return null;
        IAdaptable adaptable = (IAdaptable)element;
        Object adapter = adaptable.getAdapter(IResource.class);
        return (IResource) adapter;        
	}
	
	private IFile getFile(IStructuredSelection ss) {
        Object element = ss.getFirstElement();
        if (element instanceof IFile)
           return (IFile) element;
        if (!(element instanceof IAdaptable))
           return null;
        IAdaptable adaptable = (IAdaptable)element;
        Object adapter = adaptable.getAdapter(IFile.class);
        return (IFile) adapter;        
	}
	
	private IFile getFile(IEditorInput ei) {
		if (ei != null) {
			if (ei instanceof IFileEditorInput)
				return ((IFileEditorInput)ei).getFile();
		}
		return null;
	}
	
	public IFile getCurrentFile() {
		Object o = getCurrentObject();
		if (o instanceof IFile)
			return (IFile) o;
		return null;
	}
	
	public IProject getCurrentProject() {
		Object o = getCurrentObject();
		if (o instanceof IResource)
			return ((IResource) o).getProject();
		return null;
	}
	
	private Object getCurrentObject() {
		Object rc[] = new Object[1];
		IWorkbench wb = PlatformUI.getWorkbench();
		wb.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart part = wb.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				Object theFile=getFile(part.getEditorInput());
				ISelection sel = wb.getActiveWorkbenchWindow().getActivePage().getSelection();
				boolean validSelection = sel instanceof IStructuredSelection && !sel.isEmpty(); 
				if (validSelection)
					rc[0]=getFile((IStructuredSelection)sel);
				if (rc[0] == null && theFile != null)
					rc[0] = theFile;
				if (validSelection && rc[0]==null)
					rc[0]=getResource((IStructuredSelection)sel);
			}
		});
		return rc[0];
	}
	

}
