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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class ModificationListener {
	
	protected ResourceTracker resourceTracker;
	
	public ModificationListener(ResourceTracker resourceTracker) {
		this.resourceTracker = resourceTracker;
	}
	
	private boolean isInterestingResource(Resource r) {
		return (r instanceof XtextResource) || (r instanceof VirtualResource);		
	}
	
	protected void handleObjectModified(Object obj) {
		Resource r = null;
		if (obj instanceof EObject)
			r = ((EObject)obj).eResource();
		else if (obj instanceof Resource)
			r = (Resource)obj;
		if (r!=null && isInterestingResource(r))
			resourceTracker.setModified(r.getURI());
	}
}
