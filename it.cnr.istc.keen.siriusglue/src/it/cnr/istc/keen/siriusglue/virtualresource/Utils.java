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
