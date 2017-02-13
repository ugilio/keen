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
package it.cnr.istc.keen.scoping

import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.util.IAcceptor
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.EcoreUtil2
import it.cnr.istc.keen.ddl.TransitionConstraint
import it.cnr.istc.keen.ddl.TimelineSynchronization
import it.cnr.istc.keen.ddl.Problem

class DdlResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
	
	override createEObjectDescriptions(EObject obj, IAcceptor<IEObjectDescription> acceptor) {
		if (EcoreUtil2.getContainerOfType(obj, TransitionConstraint) != null ||
			EcoreUtil2.getContainerOfType(obj, TimelineSynchronization) != null ||
			EcoreUtil2.getContainerOfType(obj, Problem) != null
		)
			return false;
		return super.createEObjectDescriptions(obj, acceptor);
	}
	
}