/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *   Jan Koehnlein (itemis AG) - Initial contribution and API
 *   
 * Inspired by org.eclipse.xtext.ui.refactoring.impl.DefaultDependentElementsCalculator from Xtext 2.10
 */
package it.cnr.istc.keen.ui.refactoring;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.refactoring.IDependentElementsCalculator;

import com.google.common.collect.Lists;

/**
 * Delivers all contained elements of an element to be renamed in order to updated references to them.
 * 
 * @author Jan Koehnlein - Initial contribution and API
 */
@SuppressWarnings("restriction")
public class DdlDependentItemsCalculator implements IDependentElementsCalculator {
	
	private static final int MONITOR_CHUNK_SIZE = 1000;
	
	//Always include all children elements, because URIs include the name of the parent!
	@Override
	public List<URI> getDependentElementURIs(EObject baseElement, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 2);
		List<URI> elementURIs = Lists.newArrayList();
		int counter = 0;
		for (Iterator<EObject> iterator = EcoreUtil.getAllProperContents(baseElement, false); iterator.hasNext();) {
			if(progress.isCanceled()) {
				throw new OperationCanceledException();
			}
			EObject childElement = iterator.next();
			URI childURI = EcoreUtil.getURI(childElement);
			if (childURI != null) {
				elementURIs.add(childURI);
			}
			counter++;
			if (counter % MONITOR_CHUNK_SIZE == 0) {
				progress.worked(1);
				progress.setWorkRemaining(2);
			}
		}
		return elementURIs;
	}

}

