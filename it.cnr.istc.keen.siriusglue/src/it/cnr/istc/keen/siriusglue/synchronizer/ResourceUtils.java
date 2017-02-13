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
package it.cnr.istc.keen.siriusglue.synchronizer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class ResourceUtils {
	
	static ResourceSet getResourceSetForEditor(IAdaptable editor) {
		IEditingDomainProvider dp = (IEditingDomainProvider)editor.getAdapter(IEditingDomainProvider.class);
		if (dp!=null) {
			EditingDomain e = dp.getEditingDomain();
			ResourceSet rs = e.getResourceSet();
			return rs;
		}
		return null;
	}
	
	static VirtualResource getVirtualResourceForEditor(IAdaptable editor) {
		ResourceSet rs = getResourceSetForEditor(editor);
		for (Resource r : rs.getResources())
			if (r instanceof VirtualResource)
				return (VirtualResource)r;
		return null;
		
	}
	
	static Resource getResourceForEditor(IAdaptable editor, URI uri) {
		ResourceSet rs = getResourceSetForEditor(editor);
		if (rs!=null)
			for (Resource r : rs.getResources())
				if (r.getURI().equals(uri))
					return r;
		return null;
	}
	
	static Resource findOpenResourceInDiagramEditors(IWorkbenchPage page, URI uri) {
		for (IEditorReference ref : page.getEditorReferences()) {
			IEditorPart part = ref.getEditor(false);
			if (part != null && (part.getEditorInput() instanceof URIEditorInput))
				return getResourceForEditor(part, uri);
		}
		return null;
	}
	
	static Resource getOpenResource(IWorkbenchPage page, URI uri) {
		IEditorPart editor = null;
		if (uri.isPlatformResource()) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().
					getFile(new Path(uri.toPlatformString(true)));
			FileEditorInput fi = new FileEditorInput(file);
			editor = page.findEditor(fi);
		}
		if (editor == null)
			return findOpenResourceInDiagramEditors(page, uri);
		if (editor instanceof XtextEditor)
			return getResourceFromXtextEditor((XtextEditor)editor);
		else if (editor instanceof IAdaptable)
			return getResourceForEditor(editor,uri);
		return null;
	}
	
	static Resource getOpenResourceAnywhereElse(IWorkbenchPage page, URI uri) {
		IWorkbench wb = PlatformUI.getWorkbench();
		for (IWorkbenchWindow w : wb.getWorkbenchWindows())
			for (IWorkbenchPage p : w.getPages())
				if (p != page) {
					Resource result = getOpenResource(p, uri);
					if (result != null)
						return result;
				}
		return null;
	}
	
	static Object getResourceOrDocument(IEditorReference r, URI uri) {
		IEditorPart editor = r.getEditor(false);
		if (editor == null)
			return null;
		IEditorInput ei = editor.getEditorInput();
		if (ei instanceof FileEditorInput && uri.isPlatformResource()) {
			FileEditorInput fei = (FileEditorInput)ei;
			IFile file = ResourcesPlugin.getWorkspace().getRoot().
					getFile(new Path(uri.toPlatformString(true)));
			if (!fei.getFile().equals(file))
				return null;
		}
		else
			return getResourceForEditor(editor, uri);
		if (editor instanceof XtextEditor)
			return ((XtextEditor)editor).getDocument();
		return null;
	}
	
	static Set<Object> getOpenResourcesAnywhere(URI uri) {
		HashSet<Object> s = new HashSet<>();
		for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows())
			for (IWorkbenchPage p : w.getPages())
				for (IEditorReference r : p.getEditorReferences()) {
					Object obj = getResourceOrDocument(r, uri);
					if (obj!=null)
						s.add(obj);
				}
		return s;
	}
	
	static XtextResource getResourceFromXtextEditor(XtextEditor editor) {
		return getResourceFromXtextDocument(editor.getDocument());
	}
	static XtextResource getResourceFromXtextDocument(IXtextDocument doc) {
		if (doc == null)
			return null;
		return
		doc.readOnly(new IUnitOfWork<XtextResource, XtextResource>(){
			@Override
			public XtextResource exec(XtextResource state) throws Exception {
				return state;
			}
		});
	}
	
	static IEditorPart isEditorFor(IEditorReference r, URI uri) {
		IEditorInput input = null;
		try
		{
			input = r.getEditorInput();
		} catch (PartInitException e)
		{
			input = r.getEditor(true).getEditorInput();
		}
		if (input==null)
			return null;
		if (input instanceof FileEditorInput && uri.isPlatformResource()) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().
					getFile(new Path(uri.toPlatformString(true)));
			if (file.equals(((FileEditorInput)input).getFile()))
				return r.getEditor(true);
			
		}
		if (input instanceof URIEditorInput) {
			if (uri.equals(((URIEditorInput)input).getURI()))
				return r.getEditor(true);
		}
		return null;
	}
	
	static IEditorPart findEditorFor(IXtextDocument doc, URI uri) {
		for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows())
			for (IWorkbenchPage p : w.getPages())
				for (IEditorReference r : p.getEditorReferences()) {
					IEditorPart part = isEditorFor(r, uri);
					if (part!=null && part instanceof XtextEditor) {
						XtextEditor editor = ((XtextEditor)part);
						if (editor.getDocument()==doc)
							return editor;
					}
				}
		return null;
	}	

}
