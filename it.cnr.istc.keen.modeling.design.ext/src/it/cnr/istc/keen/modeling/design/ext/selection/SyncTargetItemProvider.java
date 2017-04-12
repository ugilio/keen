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
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;

import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.utils.DomainUtils;

public class SyncTargetItemProvider extends ReflectiveItemProvider implements IItemLabelProvider {
	
	public SyncTargetItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		return getText((InstantiatedComponentDecision)object);
	}
	
	@Override
	public Object getImage(Object object) {
		return super.getImage(object);
	}
	
	private String getText(InstantiatedComponentDecision icd) {
		return DomainUtils.getFullDecisionName(icd.getDecision(), icd.getTimeline());
	}
}
