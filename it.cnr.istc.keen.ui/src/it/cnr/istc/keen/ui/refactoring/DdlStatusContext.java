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
package it.cnr.istc.keen.ui.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class DdlStatusContext extends RefactoringStatusContext {
	private Resource fResource;
	private IRegion fSourceRegion;

	public DdlStatusContext(Resource res, IRegion region) {
		Assert.isNotNull(res);
		fResource = res;
		fSourceRegion= region;
	}

	public Resource getResource() {
		return fResource;
	}

	public IRegion getTextRegion() {
		return fSourceRegion;
	}

	@Override
	public Object getCorrespondingElement() {
		return getResource();
	}
}

