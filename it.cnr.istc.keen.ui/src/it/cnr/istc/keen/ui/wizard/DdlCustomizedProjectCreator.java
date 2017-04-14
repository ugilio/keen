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
package it.cnr.istc.keen.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.ImmutableList;

import it.cnr.istc.keen.ui.nature.DdlNature;
import it.cnr.istc.keen.ui.nature.NatureUtils;

public class DdlCustomizedProjectCreator extends DdlProjectCreator {
	
	@Override
	protected List<String> getAllFolders() {
		return ImmutableList.of(getModelFolderName());
	}

	protected void enhanceProject(final IProject project, final IProgressMonitor monitor) throws CoreException {
		NatureUtils.addNature(project,DdlNature.ID);
		super.enhanceProject(project, monitor);
	}
}
