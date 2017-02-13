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

import com.google.inject.Provider;

public class SynchronizerFactory implements Provider<Synchronizer>{
	public Synchronizer get() {
		ResourceTracker resourceTracker = new ResourceTracker();
		Reconciler reconciler = new Reconciler(resourceTracker);
		
		ResourceUpdater resourceUpdater = new ResourceUpdater(null, resourceTracker, reconciler);
		DiagramSaveListener diagramSaveListener = new DiagramSaveListener(resourceTracker, resourceUpdater);
		DiagramModificationListener diagramModificationListener = new DiagramModificationListener(resourceTracker, reconciler, diagramSaveListener);
		XtextModificationListener xtextModificationListener = new XtextModificationListener(resourceTracker, reconciler);
		ResourceSaveListener resourceSaveListener = new ResourceSaveListener(resourceTracker, resourceUpdater);
		Controller controller = new Controller(diagramSaveListener, resourceSaveListener);
		EditorListener editor = new EditorListener(diagramModificationListener, xtextModificationListener, controller);
		
		resourceUpdater.setController(controller);
		
		return new Synchronizer(editor);
	}

}
