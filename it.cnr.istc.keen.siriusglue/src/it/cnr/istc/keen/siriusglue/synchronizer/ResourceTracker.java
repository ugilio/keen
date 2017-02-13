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
package it.cnr.istc.keen.siriusglue.synchronizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.URI;

public class ResourceTracker {
	private Set<URI> modifiedResources = Collections.synchronizedSet(new HashSet<>());
	private Set<URI> openingResources = Collections.synchronizedSet(new HashSet<>());
	
	void setModified(URI uri) {
		if (!isOpening(uri))
			modifiedResources.add(uri);
	}
	
	void setUnmodified(URI uri) {
		modifiedResources.remove(uri);
	}
	
	boolean isModified(URI peerURI) {
		//TODO: normalize: (ResourceSet.getURIConverter().normalize())
		return modifiedResources.contains(peerURI);
	}
	
	void opening(URI uri) {
		if (uri != null)
			openingResources.add(uri);
	}
	
	boolean isOpening(URI uri) {
		return openingResources.contains(uri);
	}
	
	void opened(URI uri) {
		if (uri != null)
			openingResources.remove(uri);
	}
}
