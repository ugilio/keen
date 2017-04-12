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
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;

import it.cnr.istc.keen.ddl.ParType;

public class ParTypeAdapterFactory extends AdapterFactoryImpl {
	
	  @Override
	  public boolean isFactoryForType(Object object) {
		  return object instanceof ParType;
	  }
	  
	  @Override
	  public Adapter createAdapter(Notifier target) {
		  return new ReflectiveItemProvider(this);
	  }
}
