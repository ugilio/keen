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
package it.cnr.istc.keen.modeling.design.ext.contentassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.Domain;

public class ContentAssistFactory {

	private HashMap<Class<? extends EObject>, Class<? extends AbstractContentProposalProvider>> map;	
	public static ContentAssistFactory INSTANCE = new ContentAssistFactory();
	
	private ContentAssistFactory() {
		map = init();
	}
	
	private static HashMap<Class<? extends EObject>, Class<? extends AbstractContentProposalProvider>> init() {
		HashMap<Class<? extends EObject>, Class<? extends AbstractContentProposalProvider>> m = new HashMap<>();
		m.put(Component.class, ComponentTypeContentProposalProvider.class);
		m.put(ComponentDecisionType.class, ComponentDecisionTypeContentProposalProvider.class);
		return m;
	}

	private Class<? extends AbstractContentProposalProvider> get(EObject object) {
		for ( Entry<Class<? extends EObject>, Class<? extends AbstractContentProposalProvider>> e : map.entrySet())
			if (e.getKey().isAssignableFrom(object.getClass()))
				return e.getValue();
		return null;
	}
	
	public boolean hasContentAssistFor(EObject object) {
		return get(object)!=null;
	}
	
	public AbstractContentProposalProvider getContentAssistFor(EObject object) {
		Class<? extends AbstractContentProposalProvider> c = get(object);
		if (c == null)
			return null;
		try {
			Constructor<? extends AbstractContentProposalProvider> cons = c.getConstructor(Domain.class);
			cons.setAccessible(true);
			Domain dom = EcoreUtil2.getContainerOfType(object, Domain.class);
			return cons.newInstance(dom);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
