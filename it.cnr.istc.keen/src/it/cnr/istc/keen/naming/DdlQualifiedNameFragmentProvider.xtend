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

import org.eclipse.xtext.resource.IFragmentProvider
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.IFragmentProvider.Fallback
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.common.util.SegmentSequence
import org.eclipse.emf.ecore.EStructuralFeature
import java.util.List
import java.util.Collections
import java.util.regex.Pattern

class DdlQualifiedNameFragmentProvider implements IFragmentProvider {
	
	private char[] escapeChar = #['{', '}', '[', ']', '@', '?'];
	
	//Example: @feature[name{2}]
	private Pattern pattern = Pattern.compile("^@([^\\[]+)(?:\\[([^\\]\\{]+)(?:\\{(\\d+)\\})?\\])?$");
	
	def getObjName(EObject obj)
	{
		return escape(DdlNameProvider.getObjName(obj));
	}
	
	def escape(String str)
	{
		if (str==null)
			return null;
		var s = str;
		for (c : escapeChar)
			s = s.replace(""+c,"_");
		return s;
	}
	
	private def getSiblings(InternalEObject cont, EStructuralFeature feat, InternalEObject obj)
	{
		if (feat!=null && feat.isMany())
			return cont.eGet(feat) as List<EObject>;
		return Collections.<EObject>emptyList();
	}
	
	protected def getUniqueIndex(InternalEObject cont, EStructuralFeature feat, InternalEObject intObj)
	{
		val name = getObjName(intObj);
		if (name==null)
			return "";
		val siblings = getSiblings(cont,feat,intObj);
		val sameName = siblings.filter[o|name.equals(getObjName(o))].toList();
		if (sameName.size()<=1)
			return "";
		return "{"+sameName.indexOf(intObj)+"}";
	}
  	
	protected def getUniqueIndex(InternalEObject intObj)
	{
		val res = intObj.eDirectResource;
		if (res == null)
			return getUniqueIndex(intObj.eInternalContainer(),intObj.eContainingFeature,intObj);
		val siblings = res.contents;
		if (siblings.size()<=1)
			return "";
		return "{"+siblings.indexOf(intObj)+"}";
	}
  	
	private def getRoot(Resource res, String part)
	{
		val contents = res.contents;
		val index = if (part.startsWith("{") && part.endsWith("}"))
			{
				try
				{
					Integer.parseInt(part.substring(1,part.length-1));
				}
				catch (NumberFormatException e) { 0 }
			}
			else 0;
		if (contents.size()>index)
			return contents.get(index) as InternalEObject;
		return null;
	}

	private def getChild(EObject container, String part)
	{
		val matcher = pattern.matcher(part);
		if (!matcher.matches())
			return null;
			
		val featName = matcher.group(1);
		val objName = matcher.group(2);
		val indexStr = matcher.group(3);
		val index = if (indexStr==null) 0 else Integer.parseInt(indexStr);
		
		val feat = container.eClass.getEStructuralFeature(featName);
		if (feat == null)
			return null;

		val children = 
			if (feat.many)
				container.eGet(feat,false) as List<EObject>
			else
				Collections.singletonList(container.eGet(feat,false) as EObject);
		val sameName =
			if (objName==null)
				children.filter[o|getObjName(o)==null]
			else
				children.filter[o|objName.equals(getObjName(o))];
		if (sameName.size()>index)
			return sameName.get(index) as InternalEObject;
		
		return null;
	}
	
	override getFragment(EObject obj, Fallback fallback) {
		val builder = SegmentSequence.newBuilder("/");			
		var intObj = obj as InternalEObject;
		while (intObj != null)
		{
			val cont = intObj.eInternalContainer();
			val name = getObjName(intObj);
			if (cont != null)
			{
				val feature = intObj.eContainingFeature();
				val partString = 
				if (name == null)
					cont.eURIFragmentSegment(feature,intObj)
				else
				{
					val suffix = getUniqueIndex(cont,feature,intObj);
					"@"+feature.name+"["+name+suffix+"]"
				}
				
				builder.append(partString);
			}
			else
			{
				val suffix = getUniqueIndex(intObj);
				builder.append(suffix);
			}
			intObj = cont;
		}
		builder.append("");
		val fragment = builder.reverse().toSegmentSequence.toString;
		return fragment;
	}
	
	override getEObject(Resource resource, String fragment, Fallback fallback) {
		val segments = SegmentSequence.create("/", fragment).subSegmentsList(1);
		
		if (segments == null || segments.size<1)
			return fallback.getEObject(fragment);
		
		var obj = getRoot(resource,segments.get(0));
		
		for (var i = 1; obj!=null && i < segments.size(); i++)
		{
			var child = getChild(obj,segments.get(i));
			if (child == null)
				try {
					child = obj.eObjectForURIFragmentSegment(segments.get(i)) as InternalEObject;
				}
				catch (StringIndexOutOfBoundsException ex) {
					//ignore, it is expected if we are using our format but the object does
					//not exist (e.g. it has been deleted offline?)
				}
			obj = child;
		}
		
		//return if (obj!=null) obj else fallback.getEObject(fragment);
		return obj;
	}
}