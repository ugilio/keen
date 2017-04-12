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
package it.cnr.istc.keen.siriusglue.synchronizer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.InternalEList;

public class CrossReferenceUpdater {

	public CrossReferenceUpdater() {
		// TODO Auto-generated constructor stub
	}
	
	private ECrossReferenceAdapter getXref(Resource res) {
		ECrossReferenceAdapter xref = ECrossReferenceAdapter.getCrossReferenceAdapter(res);
		if (xref == null)
			xref = ECrossReferenceAdapter.getCrossReferenceAdapter(res.getResourceSet());
		if (xref == null) {
			xref = new ECrossReferenceAdapter();
			xref.setTarget(res);
		}
			
		return xref;
	}
	
	private void addAllXref(EObject obj, Collection<SettingExt> toRefresh) {
		Resource res = obj.eResource();
		ECrossReferenceAdapter xref = getXref(res);
		if (xref != null)
			toRefresh.addAll(
					xref.getInverseReferences(obj).stream().
					filter(s -> s.getEStructuralFeature() != obj.eContainingFeature()).
					filter(s -> s.getEObject().eResource()==res).
					map(s -> new SettingExt(s, obj)).
					collect(Collectors.toList()));
		//refresh also all references to the children of this object
		for (EObject child : obj.eContents())
			addAllXref(child, toRefresh);
	}
	
	protected Collection<SettingExt> getObjectsToRefresh(List<Diff> diffs) {
		HashSet<SettingExt> toRefresh = new HashSet<>();
		for (Diff diff : diffs) {
			if (diff.getKind()!=DifferenceKind.CHANGE)
				continue;
			EObject before = diff.getMatch().getLeft();
			//EObject after = diff.getMatch().getRight();

			//Left can be null if there is a changeset involving links to a newly created object,
			//that still doesn't exist
			if (before!=null)
				addAllXref(before, toRefresh);
		}
		int oldCount = 0;
		int count = toRefresh.size();
		while (count != oldCount) {
			HashSet<SettingExt> copy = new HashSet<>(toRefresh);
			for (SettingExt o : copy) {
				addAllXref(o.object, toRefresh);
			}
			oldCount = count;
			count = toRefresh.size();
		}
		return toRefresh;
	}
	
	@SuppressWarnings("unchecked")
	private void refresh(EObject o, EStructuralFeature f, EObject value) {
		int idx = -1;
		InternalEList<EObject> l = null;
		boolean deliver = o.eDeliver();
		o.eSetDeliver(false);
		try
		{
			if (f.isMany()) {
				l = (InternalEList<EObject>)o.eGet(f);
				idx = l.indexOf(value);
				assert idx != -1;
				l.remove(idx);
				o.eSetDeliver(true);
				l.addUnique(idx, value);
			}
			else {
				o.eUnset(f);
				o.eSetDeliver(true);
				o.eSet(f, value);
			}
		}
		finally
		{
			o.eSetDeliver(deliver);
		}
	}
	
	protected void refreshReferences(Collection<SettingExt> toRefresh) {
		for (SettingExt s : toRefresh) {
			refresh(s.object,s.feature,s.value);
		}
	}	

}
