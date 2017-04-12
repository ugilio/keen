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
package it.cnr.istc.keen.modeling.design.ext.modelextender;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.ecore.extender.business.api.accessor.IExtenderProvider;
import org.eclipse.sirius.ecore.extender.business.api.accessor.IMetamodelExtender;
import org.eclipse.xtext.resource.XtextResourceSet;

public class ExtenderProvider implements IExtenderProvider {

	@Override
	public boolean provides(ResourceSet set) {
		return set instanceof XtextResourceSet;
	}

	@Override
	public IMetamodelExtender getExtender(ResourceSet set) {
		return new Extender();
	}

}
