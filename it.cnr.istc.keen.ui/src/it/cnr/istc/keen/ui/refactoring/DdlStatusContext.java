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

