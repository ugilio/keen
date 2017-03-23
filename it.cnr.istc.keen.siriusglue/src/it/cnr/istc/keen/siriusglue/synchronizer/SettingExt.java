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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;

public class SettingExt {
	EObject object;
	EStructuralFeature feature;
	EObject value;
	
	public SettingExt(Setting setting, EObject value) {
		this.object = setting.getEObject();
		this.feature = setting.getEStructuralFeature();
		this.value = value;
	}
	
	private static final int PRIME = 31;
	public int hashCode() {
		return value.hashCode()+PRIME*
				(feature.hashCode()+PRIME*
				(object.hashCode()+PRIME));
	}
	
	private boolean safeEq(Object o1, Object o2) {
		return (o1 == o2) || (o1 != null && o1.equals(o2));
	}
	
	public boolean equals(Object o2) {
		if (! (o2 instanceof SettingExt))
			return false;
		SettingExt that = (SettingExt)o2;
		return safeEq(this.object,that.object) &&
				safeEq(this.feature,that.feature) &&
				safeEq(this.value,that.value);
	}
}