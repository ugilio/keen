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
package it.cnr.istc.keen.modeling.design.ext.selection;

import java.util.Collection;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sirius.common.ui.tools.api.selection.EObjectSelectionWizard;
import org.eclipse.sirius.diagram.ui.provider.DiagramUIPlugin;
import org.eclipse.sirius.diagram.ui.provider.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public abstract class SelectionWizard {
	
	protected String title;
	protected String message;
	protected String imagePath;
	protected boolean multiple;
	protected ImageDescriptor image;
	
	protected SelectionWizard(String title, String message, String imagePath, boolean multiple)
	{
    	this.title = title;
    	this.message = message;
    	this.imagePath = imagePath;
    	this.multiple = multiple;
	}
	
    public Collection<EObject> openSelectionWizard(EObject self) {
        Shell shell = null;
        boolean createdShell = false;
        if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        }
        if (shell == null) {
            shell = new Shell();
            createdShell = true;
        }
        
        try
        {
        	Collection<EObject> input = computeInput(self);
        	if (image == null && imagePath!=null && imagePath.length()>0)
        		image = DiagramUIPlugin.Implementation.findImageDescriptor(imagePath);

        	final EObjectSelectionWizard wizard = new EObjectSelectionWizard(title, message, image,
        			input, getAdapterFactory());
        	wizard.setMany(multiple);
        	final WizardDialog dlg = new WizardDialog(shell, wizard);
        	final int result = dlg.open();
        	if (result == Window.OK) {
        		return wizard.getSelectedEObjects();
        	} else {
        		throw new OperationCanceledException(Messages.SelectionWizardCommand_cancelExceptionMsg);
        	}
        }
        finally
        {
        	if (createdShell)
        		shell.dispose();
        }
    }
    
    protected abstract Collection<EObject> computeInput(EObject self);
    
    protected abstract AdapterFactory getAdapterFactory();
    
}
