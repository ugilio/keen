package it.cnr.istc.keen.siriusglue.virtualresource;

import org.eclipse.emf.common.util.URI;

public class Utils {
	public static URI copyURI(String scheme, URI orig) {
		if (orig==null)
			return null;
		if (orig.isHierarchical())
			return URI.createHierarchicalURI(scheme,
					orig.authority(), orig.device(), orig.segments(),
					orig.query(), orig.fragment());
		else
			return URI.createGenericURI(scheme, orig.opaquePart(), orig.fragment());
	}

}
