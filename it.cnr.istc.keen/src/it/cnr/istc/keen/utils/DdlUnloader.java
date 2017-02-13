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
package it.cnr.istc.keen.utils;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.parser.antlr.IReferableElementsUnloader;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;

/**
 * Modified from {@link IReferableElementsUnloader.GenericUnloader} to collect all references
 * first, and clear adapters later.
 * Rationale: consider this case:
 * * URI are computed hierarchically (based upon container's URI)
 * * Some objects require access to their parsing Node to compute their URI
 * * If adapters are cleared first, a children trying to obtain the container's URI will fail
 *   because the container doesn't have an ICompositeNode adapter anymore
 * @author giulio
 *
 */
public class DdlUnloader implements IReferableElementsUnloader {

	@Override
	public void unloadRoot(EObject root) {
		// Content adapters should be removed the same way as they are added: top-down. 
		// Fragments are recursive, so we need them to be calculated before proxifying the container.
		// OTOH, some model elements resolve their children when being proxified (e.g. EPackageImpl).
		// => first calculate all fragments, then proxify elements starting form root.
		List<Pair<EObject, URI>> elementsToUnload = newArrayList();
		elementsToUnload.add(Tuples.create(root, EcoreUtil.getURI(root)));
		for (Iterator<EObject> i = EcoreUtil.getAllProperContents(root, false); i.hasNext();) {
			EObject element = i.next();
			elementsToUnload.add(Tuples.create(element, EcoreUtil.getURI(element)));
		}
		for (Pair<EObject,URI> elementToUnload : elementsToUnload) {
			InternalEObject element = (InternalEObject) elementToUnload.getFirst();
			element.eAdapters().clear();
			element.eSetProxyURI(elementToUnload.getSecond());
		}
	}

}
