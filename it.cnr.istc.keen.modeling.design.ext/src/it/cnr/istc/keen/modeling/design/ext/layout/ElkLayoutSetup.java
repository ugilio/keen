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
package it.cnr.istc.keen.modeling.design.ext.layout;

import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.ILayoutSetup;
import org.eclipse.sirius.diagram.DDiagram;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class ElkLayoutSetup implements ILayoutSetup {

	private LayoutHelper helper = new LayoutHelper();
	
	@Override
	public boolean supports(Object object) {
		DDiagram diag = helper.getDDiagram(object);
		if (diag != null)
			return helper.isSupportedDiagram(diag.getDescription().getName());
		return false;
	}

	@Override
	public Injector createInjector(Module defaultModule) {
		return Guice.createInjector(
				Modules.override(defaultModule).with(new LayoutModule()));
	}
	
	public static class LayoutModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(IDiagramLayoutConnector.class).to(ElkLayoutConnector.class);
		}
	}

}
