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
package it.cnr.istc.keen.validation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
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
import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;
import it.cnr.istc.mc.translator.MCTranslator;
import it.cnr.istc.mc.translator.MCTranslator.QueryComponentInfoEx;

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
	
	@SuppressWarnings("unchecked")
	private <R> R syncGet(Supplier<R> f)
	{
		Display disp = PlatformUI.getWorkbench().getDisplay();
		Object[] res = new Object[1];
		disp.syncExec(new Runnable() {
			@Override
			public void run() {
				res[0]=f.get();
			}
		});
		return (R)res[0];
	}
	
	private void internalExecute(IProgressMonitor monitor, Domain domain)
	{
		Shell shell = syncGet(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		
    	try
    	{
    		Path path = Files.createTempDirectory("keen");
    		File dir = path.toFile();
    		dir.deleteOnExit();
    		String baseName = new File(dir,domain.getName()).getAbsolutePath();
    		File xtaFile = new File(baseName+".xta");
    		try(OutputStream os = new FileOutputStream(xtaFile))
    		{
    			xtaFile.deleteOnExit();
    			it.cnr.istc.keen.validation.mcinterface.Domain wrappedDomain =
    					new it.cnr.istc.keen.validation.mcinterface.Domain(domain);
    			MCTranslator XtaTrans = new MCTranslator(wrappedDomain);
    			List<QueryComponentInfoEx> list = XtaTrans.validateDomain(os);
    			ValidationResult result = new TigaLauncher(baseName, wrappedDomain, list, monitor).execute();
    			Display disp = PlatformUI.getWorkbench().getDisplay();
    			disp.asyncExec(new Runnable() {

    				@Override
    				public void run() {
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
    					int success = 0;
    					int tot = result.getList().size();
    					for (ValidationResultElement el : result.getList())
    						if (el.getStatus()==StatusType.OK)
    							success++;
    						else if (el.getStatus().compareTo(StatusType.FAILED)>=0)
    							failures++;
    					String msg = String.format("%d/%d properties were successfully validated, %d failures.", success,tot,failures);
    					if (result.getStatus())
    						MessageDialog.openInformation(shell, "Domain Validation", msg);
    					else
    						MessageDialog.openWarning(shell, "Domain Validation", msg);
    				}
    			});
    		}
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
	}
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
    	Domain domain = getDomain(event);
    	if (domain == null)
    		return null;
    	
    	Job job = new Job("Domain Validation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				internalExecute(monitor, domain);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
    	
    	return null;
	}
}
