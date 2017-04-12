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
package it.cnr.istc.keen.modeling.design.ext.actions;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;

import it.cnr.istc.keen.modeling.design.ext.actions.SelectionHelper.ResourceContext;
import it.cnr.istc.keen.siriusglue.virtualresource.Utils;

public class OpenRootDiagramHandler extends AbstractHandler {
	
	private static final String MODEL_URI = "platform:/plugin/it.cnr.istc.keen/model/generated/Ddl.ecore";
	
	private boolean enabled = false;
	
	private SelectionHelper selectionHelper = new SelectionHelper();
	
	@Override
	public boolean isEnabled() {
		return enabled && super.isEnabled();
	}
	
	private boolean canHandle(ResourceContext rc) {
		return rc != null && "ddl".equals(rc.theURI.fileExtension());
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		enabled=canHandle(selectionHelper.getResourceContextFromEvent(new ExecutionEvent(null,Collections.emptyMap(),null,evaluationContext)));
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ResourceContext rc = selectionHelper.getResourceContextFromEvent(event);
		
		URI uri = Utils.copyURI("virtual", rc.theURI);
		IProject project = rc.theProject;
		new RootDiagramOpener().
			configure(URI.createURI(MODEL_URI)).
			openRootDiagram(project, uri);
		return null;
	}
}
