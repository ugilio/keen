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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Domain;

public class GenericSelectionWizard extends SelectionWizard {

    private final AdapterFactory factory;
    
    private Class<?> targetClass;
	
	public GenericSelectionWizard(Class<?> targetClass, String title,
			String message, String imagePath, boolean multiple) {
		super(title,message,imagePath,multiple);
		this.targetClass=targetClass;
		this.factory = new GenericAdapterFactory(targetClass);
	}
	
    protected Collection<EObject> computeInput(EObject self) {
    	Domain dom = EcoreUtil2.getContainerOfType(self, Domain.class);
    	if (dom == null)
    		return Collections.emptyList();
    	return
    	dom.getElements().stream().
    		filter(o -> targetClass.isAssignableFrom(o.getClass())).
    		collect(Collectors.toList());
    }
    
    protected AdapterFactory getAdapterFactory() {
    	return factory;
    }
}
