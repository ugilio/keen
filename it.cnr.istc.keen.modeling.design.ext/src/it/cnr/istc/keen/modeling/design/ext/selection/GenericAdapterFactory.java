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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

public class GenericAdapterFactory extends AdapterFactoryImpl {

	private Class<?> targetClass;

	public GenericAdapterFactory(Class<?> targetClass) {
		super();
		this.targetClass = targetClass;
	}

	@Override
	public boolean isFactoryForType(Object object) {
		return targetClass.isAssignableFrom(object.getClass());
	}

	@Override
	public Adapter createAdapter(Notifier target) {
		return new GenericItemProvider(this);
	}
}
