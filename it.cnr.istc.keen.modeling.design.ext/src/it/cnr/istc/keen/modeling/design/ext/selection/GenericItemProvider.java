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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;

import it.cnr.istc.keen.naming.DdlNameProvider;

public class GenericItemProvider extends ReflectiveItemProvider implements IItemLabelProvider {
	
	public GenericItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		if (object instanceof EObject)
			return DdlNameProvider.getObjName((EObject)object);
		return super.getText(object);
	}
	
	@Override
	public Object getImage(Object object) {
		return super.getImage(object);
	}
}
