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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;

import com.google.inject.Inject;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class XtextModificationListener extends ModificationListener {
	
	private Reconciler reconciler;

	@Inject
	public XtextModificationListener(ResourceTracker resourceTracker, Reconciler reconciler) {
		super(resourceTracker);
		this.reconciler = reconciler;
	}

	public void editorOpened(XtextEditor editor) {
		URI uri = null;
		Resource res = ResourceUtils.getResourceFromXtextEditor(editor);
		if (res != null)
			uri = null;
		resourceTracker.opening(uri);
		try
		{
			IXtextDocument doc = editor.getDocument();
			if (doc!=null)
				doc.addModelListener(xtextChangeListener);
			editorActivated(editor);
		}
		finally
		{
			resourceTracker.opened(uri);
		}
	}
	
	public void editorClosed(XtextEditor editor) {
		IXtextDocument doc = editor.getDocument();
		if (doc==null)
			return;
		doc.removeModelListener(xtextChangeListener);
	}
	
	private IXtextModelListener xtextChangeListener = new IXtextModelListener() {
		@Override
		public void modelChanged(XtextResource resource) {
			handleObjectModified(resource);
		}
	};

	public void editorActivated(XtextEditor editor) {
		XtextResource res = ResourceUtils.getResourceFromXtextEditor(editor);
		if (res!=null)
			reconciler.updateIfPeerWasModified(res,
					VirtualResource.getVirtualResourceURI(res.getURI()), editor);
	}

}
