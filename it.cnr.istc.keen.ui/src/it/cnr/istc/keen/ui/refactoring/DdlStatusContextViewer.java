/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *   IBM Corporation - org.eclipse.ltk.internal.ui.refactoring.FileStatusContextViewer
 *   
 * Inspired from FileStatusContextViewer
 */
package it.cnr.istc.keen.ui.refactoring;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.ui.refactoring.TextStatusContextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
import org.eclipse.xtext.ui.editor.info.ResourceWorkingCopyFileEditorInput;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;

import com.google.inject.Inject;

public class DdlStatusContextViewer extends TextStatusContextViewer {

	@Inject
	private XtextSourceViewerConfiguration sourceViewerConfiguration;
	
	@Inject
	private XtextDocumentProvider docProvider;
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		SourceViewer sv = getSourceViewer();
		sv.setEditable(false);
		sv.configure(sourceViewerConfiguration);
		sv.getControl().setFont(getDefaultFont());
	}
	
	private Font getDefaultFont()
	{
		//Font font = JFaceResources.getFont(symbolicFontName);
		Font font= JFaceResources.getTextFont();
//		if (font==null)
//			JFaceResources.getFontRegistry().put(
//					JFaceResources.TEXT_FONT, new FontData[]{new FontData("Monospace", 10, SWT.NORMAL)});
		//ugly fallback
		if (font == null)
			font=JFaceResources.getFont("org.eclipse.jdt.ui.editors.textfont");
		return font;
	}

	@Override
	public void setInput(RefactoringStatusContext context) {
		DdlStatusContext sc= (DdlStatusContext)context;
		Resource res = sc.getResource();
		IFileEditorInput input = getEditorInput(res);
		IDocument document = getDocument(input);
		updateTitle(input);
		IRegion region= sc.getTextRegion();
		if (region != null && document.getLength() >= region.getOffset() + region.getLength())
			setInput(document, region);
		else {
			setInput(document, new Region(0, 0));
		}
	}
	
	private IFileEditorInput getEditorInput(Resource res) {
		try
		{
			return new ResourceWorkingCopyFileEditorInput(res);
		}
		catch (IOException ex)
		{
			throw new WrappedException(ex);
		}
	}
	
	private IDocument getDocument(IEditorInput input) {
		try {
			try {
				docProvider.connect(input);
				return docProvider.getDocument(input);
			}
			finally {
				docProvider.disconnect(input);
			}
		} catch (CoreException e) {
			throw new WrappedException(e);
		}
	}
	
	@Override
	protected SourceViewer createSourceViewer(Composite parent) {
	    return new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
	}
}

