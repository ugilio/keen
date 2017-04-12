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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import it.cnr.istc.keen.ddl.Domain;

public abstract class AbstractContentProposalProvider implements IContentProposalProvider {
	
	protected Domain domain;
	
	private static Pattern regex = Pattern.compile(".*?(\\w+)$");

	public AbstractContentProposalProvider(Domain domain) {
		this.domain = domain;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String prefix = contents == null ? "" : contents;
		if (position <= prefix.length())
			prefix = prefix.substring(0,position);
		
		if (!canBeApplied(prefix))
			return new IContentProposal[0];

		Matcher m = regex.matcher(prefix);
		if (m.matches())
			prefix=m.group(1);
		else
			prefix="";
		int prefSize = prefix.length();

		List<EObject> candidates = getCandidates();
		List<IContentProposal> result = new ArrayList<IContentProposal>(candidates.size());
		for (EObject candidate : candidates) {
			String name = getName(candidate); 
			if (name.substring(0, Math.min(name.length(),prefSize)).equalsIgnoreCase(prefix))
				result.add(new ContentProposal(name));
		}
		return result.toArray(new IContentProposal[result.size()]);
	}
	
	protected abstract boolean canBeApplied(String prefix);
	
	protected abstract String getName(EObject obj);
	
	protected abstract List<EObject> getCandidates();
}
