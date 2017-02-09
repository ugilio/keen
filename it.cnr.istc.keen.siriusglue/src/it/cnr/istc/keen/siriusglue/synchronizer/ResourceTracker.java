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
