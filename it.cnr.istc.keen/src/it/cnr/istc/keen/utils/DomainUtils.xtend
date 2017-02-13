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
package it.cnr.istc.keen.utils

import org.eclipse.emf.ecore.EObject
import com.google.inject.Inject
import org.eclipse.xtext.resource.IResourceDescriptionsProvider
import java.util.Collections
import org.eclipse.xtext.resource.IEObjectDescription

class DomainUtils {
	private static final String DOMAINCLASSNAME = it.cnr.istc.keen.ddl.Domain.getSimpleName();

	@Inject
	private IResourceDescriptionsProvider resDescProvider;

	def getAllDomainDescriptors(EObject object) {
		val res = object.eResource;
		var rs = res?.resourceSet;
		if (rs==null)
			return Collections.<IEObjectDescription>emptyList();
		var all = resDescProvider.getResourceDescriptions(rs);
		return all.exportedObjects.filter[o|o.EClass.name.equals(DOMAINCLASSNAME)];
	}	
}