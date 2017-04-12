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
package it.cnr.istc.keen.modeling.design.ext.embeddededitor;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.emf.ecore.EObject;

public class OpenEmbeddedEditorServices
{
	private OpenEmbeddedEditor extension = new OpenEmbeddedEditor();
	
	public EObject openEmbeddedEditor(EObject self, EObject view) {
		extension.execute(Arrays.asList(new EObject[]{self,view}), Collections.emptyMap());
		return self;
	}
}
