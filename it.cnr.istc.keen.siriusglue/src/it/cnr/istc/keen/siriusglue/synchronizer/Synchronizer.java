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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

import com.google.inject.Inject;

public class Synchronizer implements IStartup {

	public EditorListener editorListener;

	@Inject
	public Synchronizer(EditorListener editorListener) {
		this.editorListener = editorListener;
	}
	
	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				editorListener.install();
			}
		});
	}
}
