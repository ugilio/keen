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

import com.google.inject.Inject;

public class Controller {
	
	private DiagramSaveListener diagramSaveListener;
	private ResourceSaveListener resourceSaveListener;
	
	@Inject
	public Controller(DiagramSaveListener diagramSaveListener, ResourceSaveListener resourceSaveListener) {
		this.diagramSaveListener = diagramSaveListener;
		this.resourceSaveListener = resourceSaveListener;
	}

	public void disableAllSaveNotifications() {
		resourceSaveListener.disableResourceChangeListener();
		diagramSaveListener.disableSiriusNotifications();
	}
	
	public void enableAllSaveNotifications() {
		resourceSaveListener.enableResourceChangeListener();
		diagramSaveListener.enableSiriusNotifications();
	}
}
