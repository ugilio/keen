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
package it.cnr.istc.keen.validation.singlelaunch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import it.cnr.istc.keen.ddl.Ddl;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.validation.Activator;
import it.cnr.istc.keen.validation.QueryComponentInfo;
import it.cnr.istc.keen.validation.ValidationResult;
import it.cnr.istc.keen.validation.ValidationResultElement;
import it.cnr.istc.keen.validation.ValidationView;
import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;
import it.cnr.istc.mc.translator.MCTranslator;

public class ValidateCommand extends AbstractHandler
{
	private boolean enabled = false;	
	
	@Override
	public boolean isEnabled() {
		return enabled && super.isEnabled();
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		enabled=getDomain(
				new ExecutionEvent(null,Collections.emptyMap(),null,evaluationContext))!=null;
	}
	
	@SuppressWarnings("unchecked")
	private Domain getDomain(ExecutionEvent event)
	{
    	IEditorPart part = HandlerUtil.getActiveEditor(event);
    	if (part instanceof XtextEditor)
    	{
    		XtextEditor editor = (XtextEditor)part;
    		IXtextDocument doc = editor.getDocument();
    		XtextResource res = doc != null ? doc.readOnly(r -> r) : null;
    		if (res != null && res.getContents().size()>0)
    		{
    			EObject root = res.getContents().get(0);
    			if (root instanceof Ddl)
    				return ((Ddl)root).getDomain();
    		}
    	}
    	IStructuredSelection ss = HandlerUtil.getCurrentStructuredSelection(event);
    	for (Object o : (Iterable<Object>)() -> ss.iterator())
    		if (o instanceof EObject)
    		{
    			Ddl ddl = EcoreUtil2.getContainerOfType((EObject)o,Ddl.class);
    			if (ddl != null)
    				return ddl.getDomain();
    		}
		return null;
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
    	Domain domain = getDomain(event);
    	if (domain == null)
    		return null;
    	
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    	try
    	{
    		it.cnr.istc.keen.validation.mcinterface.Domain wrappedDomain =
    				new it.cnr.istc.keen.validation.mcinterface.Domain(domain);
    		MCTranslator XtaTrans = new MCTranslator(wrappedDomain);
    		List<QueryComponentInfo> list =
    				translateCompInfo(XtaTrans.writeXTAFlexDomValfileParam(),wrappedDomain);
    		String baseName = new File(new File(System.getProperty("java.io.tmpdir")),domain.getName()).getAbsolutePath();
    		ValidationResult result = new TigaLauncher(baseName, list).execute();
    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    		ValidationView vview = null;//(ValidationView)page.findView(ValidationView.ID);
    		try
    		{
    			if (vview == null)
    				vview = (ValidationView)page.showView(ValidationView.ID);
    		}
    		catch (PartInitException e) {}
    		if (vview != null)
    			vview.setInput(result);

    		int failures = 0;
    		int tot = result.getList().size();
    		for (ValidationResultElement el : result.getList())
    			if (el.getStatus()!=StatusType.OK)
    				failures++;
    		String msg = String.format("%d/%d properties were successfully validated, %d failures.", tot-failures,tot,failures);
    		if (result.getStatus())
    			MessageDialog.openInformation(shell, "Domain Validation", msg);
    		else
    			MessageDialog.openWarning(shell, "Domain Validation", msg);

    	}
    	catch (CoreException e)
    	{
    		StatusManager.getManager().handle(e.getStatus(),StatusManager.SHOW);
    	}
    	catch (IOException e)
    	{
    		StatusManager.getManager().handle(new Status(IStatus.ERROR,Activator.PLUGIN_ID,
    				"There was a problem during validation: "+e),
    				StatusManager.SHOW);
    		MessageDialog.openError(shell, "Domain Validation", e.getMessage());
    	}    	

    	return null;
	}
    
    private List<QueryComponentInfo> translateCompInfo(List<MCTranslator.QueryComponentInfo> src, it.cnr.istc.keen.validation.mcinterface.Domain domain)
    {
        List<QueryComponentInfo> l = new ArrayList<QueryComponentInfo>(src.size());
        for (MCTranslator.QueryComponentInfo i : src)
        {
            QueryComponentInfo loc = new QueryComponentInfo(i.startLine, i.timeline, domain);
            l.add(loc);
        }
        return l;
    }
}
