package it.cnr.istc.keen.siriusglue.virtualresource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IResourceFactory;

public class VirtualFactory implements IResourceFactory {

	@Override
	public Resource createResource(URI uri) {
		return new VirtualResource(uri);
	}

}
