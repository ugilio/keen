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
package it.cnr.istc.keen.naming

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.IFragmentProvider.Fallback
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.InternalEObject
import com.google.inject.Inject
import org.eclipse.xtext.util.IResourceScopeCache
import com.google.inject.Provider

class DdlCachingQualifiedNameFragmentProvider extends DdlQualifiedNameFragmentProvider {
	
	@Inject
	private IResourceScopeCache cache = IResourceScopeCache.NullImpl.INSTANCE;

	protected def getFragment(EObject obj, Resource res) {
		return cache.get(obj,res,new Provider<String>(){
			override get() {
				return internalGetFragment(obj,res);
			}
		});
	}
	
	def internalGetFragment(EObject obj, Resource res) {
		var intObj = obj as InternalEObject;
		val cont = intObj.eInternalContainer();
		val name = getObjName(intObj);
		if (cont != null)
		{
			val feature = intObj.eContainingFeature();
			val partString = if (name == null)
				cont.eURIFragmentSegment(feature, intObj)
			else
			{
				val suffix = getUniqueIndex(cont, feature, intObj);
				"@" + feature.name + "[" + name + suffix + "]"
			}

			return getFragment(cont,res) + "/" + partString;
		}
		else
		{
			val suffix = getUniqueIndex(intObj);
			return "/" + suffix;
		}
	}
	
	override getFragment(EObject obj, Fallback fallback) {
		val res = obj.eResource;
		val frag = getFragment(obj,res);
		return if (frag!=null) frag else fallback.getFragment(obj);
	}
}