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
package it.cnr.istc.keen.modeling.design.ext.contentassist;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.diagram.ui.tools.TextDirectEditManager;
import org.eclipse.gmf.runtime.gef.ui.internal.parts.WrapTextCellEditor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.sirius.viewpoint.DRepresentationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Domain;

@SuppressWarnings("restriction")
public class ContentAssistAwareDirectEditManager extends TextDirectEditManager {

	private ContentProposalAdapter adapter = null;
	/** To signal if completion proposal just closed. Either the keyboard or the focus handler
	 * will take care of it*/
	private int popupJustClosed = 0;
	
	public ContentAssistAwareDirectEditManager(ITextAwareEditPart source) {
		super(source);
	}

	public ContentAssistAwareDirectEditManager(GraphicalEditPart source, Class<?> editorType, CellEditorLocator locator) {
		super(source, editorType, locator);
	}
	
	private EObject getSemantic() {
		GraphicalEditPart part = getEditPart();
		if (part == null)
			return null;
		Object tmp = part.getModel();
		if (tmp instanceof View)
			tmp = ((View)tmp).getElement();
		if (tmp instanceof DRepresentationElement) {
			List<EObject> semantic = ((DRepresentationElement)tmp).getSemanticElements();
			for (EObject o : semantic)
				if (EcoreUtil2.getContainerOfType(o, Domain.class) != null)
					return o;
		}
		return null;
	}
	
	private void insertProposal(Text text, String proposal) {
		String orig = text.getText();
		int pos = text.getCaretPosition();
		if (orig == null)
			orig = "";
		int negPos = orig.length()-pos;
		String suffix=orig.substring(pos);
		orig = orig.substring(0,pos);
		int len1 = orig.length();
		int len2 = proposal.length();
		int len = Math.min(len1,len2);

		while (len>0) {
			if (orig.substring(len1-len).equalsIgnoreCase(proposal.substring(0, len))) {
				break;
			}
			len--;
		}
		
		String newText = orig.substring(0,len1-len)+proposal+suffix;
		text.setText(newText);
		text.setSelection(newText.length()-negPos);
	}
	
	@Override
	protected CellEditor createCellEditorOn(Composite composite) {
		//CellEditor ce = super.createCellEditorOn(composite);
		CellEditor ce = new ContentAssistAwareCellEditor(composite);
		
		Text textWidget = null;
		for (Control c : composite.getChildren())
			if (c instanceof Text) {
				textWidget = (Text)c;
				break;
			}
		if (textWidget==null)
			return ce;
		
		final Text finalTextWidget = textWidget;
		
		adapter = 
		new ContentProposalAdapter(textWidget,
				new TextContentAdapter(),
				ContentAssistFactory.INSTANCE.getContentAssistFor(getSemantic()),
				KeyStroke.getInstance(SWT.CTRL, SWT.SPACE),
				null);
		adapter.setAutoActivationDelay(500);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
		adapter.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				String content = proposal.getContent();
				insertProposal(finalTextWidget, content);
			}
		});
		adapter.addContentProposalListener(new IContentProposalListener2() {
			
			@Override
			public void proposalPopupOpened(ContentProposalAdapter adapter) {
				popupJustClosed=0;
			}
			
			@Override
			public void proposalPopupClosed(ContentProposalAdapter adapter) {
				popupJustClosed++;
			}
		});
		return ce;
	}
	
	private class ContentAssistAwareCellEditor extends WrapTextCellEditor {
		public ContentAssistAwareCellEditor(Composite parent) {
			super(parent);
		}
		
		@Override
		protected void keyReleaseOccured(KeyEvent keyEvent) {
			//if return or esc, and we are showing the proposal popup...
			if ((keyEvent.character == SWT.CR || keyEvent.character == SWT.ESC) &&
					adapter.isProposalPopupOpen()) {
				popupJustClosed--;
				return; //ignore
			}
			super.keyReleaseOccured(keyEvent);
		}
		
		protected void focusLost() {
			//ignore if proposal popup is open
			if (adapter.isProposalPopupOpen())
				return;
			if (popupJustClosed--==1)
				return;
			super.focusLost();
		}
	}
}
