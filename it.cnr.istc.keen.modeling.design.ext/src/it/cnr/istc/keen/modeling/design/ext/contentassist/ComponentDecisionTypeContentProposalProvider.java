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
package it.cnr.istc.keen.modeling.design.ext.contentassist;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.ParType;

public class ComponentDecisionTypeContentProposalProvider extends AbstractContentProposalProvider {

	private static Pattern regex = Pattern.compile(".*?\\(\\s*(\\w+\\s*(,\\s*\\w*\\s*)*)?$");
	
	public ComponentDecisionTypeContentProposalProvider(Domain domain) {
		super(domain);
	}
	
	protected boolean canBeApplied(String prefix) {
		Matcher m = regex.matcher(prefix);
		return m.matches();
	}
	
	protected String getName(EObject obj) {
		if (obj instanceof ParType)
			return ((ParType)obj).getName();
		return "";
	}
	
	protected List<EObject> getCandidates() {
		if (domain==null)
			return Collections.emptyList();
		return domain.getElements().stream().filter(e -> e instanceof ParType).
		collect(Collectors.toList());
	}
}
