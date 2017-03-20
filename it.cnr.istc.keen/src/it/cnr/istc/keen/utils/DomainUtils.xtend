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

import com.google.inject.Inject
import it.cnr.istc.keen.ddl.CRConsumptionComponentDecision
import it.cnr.istc.keen.ddl.CRProductionComponentDecision
import it.cnr.istc.keen.ddl.Component
import it.cnr.istc.keen.ddl.ComponentDecision
import it.cnr.istc.keen.ddl.Domain
import it.cnr.istc.keen.ddl.RenewableResourceComponentDecision
import it.cnr.istc.keen.ddl.SVComponentDecision
import it.cnr.istc.keen.ddl.Timeline
import java.security.InvalidParameterException
import java.util.Collections
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.IResourceDescriptionsProvider

class DomainUtils {
	private static final String DOMAINCLASSNAME = Domain.getSimpleName();

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