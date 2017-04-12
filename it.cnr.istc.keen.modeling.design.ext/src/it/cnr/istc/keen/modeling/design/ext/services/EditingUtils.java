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
package it.cnr.istc.keen.modeling.design.ext.services;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sirius.business.api.query.EObjectQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.Component;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.DdlPackage;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.EnumLiteral;
import it.cnr.istc.keen.ddl.EnumerationParameterType;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.LValue;
import it.cnr.istc.keen.ddl.ParameterConstraint;
import it.cnr.istc.keen.ddl.RValue;
import it.cnr.istc.keen.ddl.Synchronization;
import it.cnr.istc.keen.ddl.TransitionConstraint;
import it.cnr.istc.keen.ddl.VarRef;
import it.cnr.istc.keen.naming.DdlNameProvider;

public class EditingUtils {
	
    public static EObject delete(EObject self) {
    	deleteExcluding(self, new HashSet<>());
    	return self;
    }
    
    protected static void deleteExcluding(EObject self, Set<EObject> exclude) {
    	Session session = new EObjectQuery(self).getSession();
    	ECrossReferenceAdapter xref = session.getSemanticCrossReferencer();
    	delete(self,exclude,xref);
    }
    
    private static void delete(EObject self,Set<EObject> deleted, ECrossReferenceAdapter xref) {
    	if (deleted.contains(self))
    		return;
    	deleted.add(self);
    	
    	EObject cont = self.eContainer();
    	boolean wasEmpty = isEmpty(cont);

    	//recursively delete all elements that make a reference
    	xref.getInverseReferences(self).stream().filter(s -> s.getEStructuralFeature() instanceof EReference).
    		filter(s -> !((EReference)s.getEStructuralFeature()).isContainment()).map(s -> s.getEObject()).
    		filter(o -> isSemanticObject(o)).
    		forEach(obj -> delete(obj,deleted,xref));
    	
    	//Delete all children
    	//Use a copy because the original list will be modified while iterating
    	List<EObject> children = new ArrayList<>(self.eContents());
    	for (EObject o : children)
    		delete(o,deleted,xref);
    	
    	ModelAccessor accessor = ModelUtils.getAccessor(self);
    	accessor.eDelete(self, xref);
    	boolean contEmpty = isEmpty(cont);
    	if (contEmpty && !wasEmpty)
    		delete(cont,deleted,xref);
    }
    
    private static boolean isSemanticObject(EObject obj) {
    	return obj != null &&
    			obj.getClass().getPackage().getName().startsWith("it.cnr.istc.keen.ddl");
    }
    
    private static boolean isEmpty(EObject obj) {
    	if (obj instanceof TransitionConstraint)
    		return ((TransitionConstraint)obj).getMeets().isEmpty() || ((TransitionConstraint)obj).getValue()==null;
//    	if (obj instanceof TimelineSynchronization)
//    		return ((TimelineSynchronization)obj).getSynchronizations().isEmpty();
    	if (obj instanceof Synchronization)
    		return ((Synchronization)obj).getValue()==null;
//    		|| ((Synchronization)obj).getElements().isEmpty();
    	if (obj instanceof InstantiatedComponentDecision)
    		return ((InstantiatedComponentDecision)obj).getDecision()==null;
    	if (obj instanceof ParameterConstraint)
    		return ((ParameterConstraint)obj).getLeft()==null || ((ParameterConstraint)obj).getRight()==null; 
    	if (obj instanceof LValue)
    		return ((LValue)obj).getVar()==null;
    	if (obj instanceof RValue) {
    		RValue rv = (RValue)obj;
    		return rv.getFirst()==null && rv.getNumber()==null && rv.getEnumeration()==null;
    	}
    	if (obj instanceof VarRef)
    		return ((VarRef)obj).getRef()==null;
    	return false;
    }
    
    public static EObject editComponentLabel(Component comp, String arg) {
    	Matcher m = Pattern.compile("^\\s*([^:\\s]+)\\s*(?::\\s*([^\\s]+)\\s*)?$").matcher(arg);
    	if (!m.matches())
    		return comp;
    	String name = m.group(1);
    	String type = m.group(2);
    	//if invalid, throws an exception, so it stops here
    	uniqueName(comp, name);
    	//comp.setName(name);
    	if (type != null) {
    		Domain dom = EcoreUtil2.getContainerOfType(comp, Domain.class);
    		Optional<ComponentType> ctype = 
    				dom.getElements().stream().
    				filter(e -> e instanceof ComponentType).map(e -> (ComponentType)e).
    				filter(ct -> ct.getName().equals(type)).findFirst();
    		if (ctype.isPresent())
    			comp.setType(ctype.get());
    	}
    	return comp;
    }
    
    private static boolean mustCheckDuplicates(EReference feature) {
    	return 
    			feature == DdlPackage.Literals.DOMAIN__ELEMENTS
    			|| feature == DdlPackage.Literals.ENUMERATION_PARAMETER_TYPE__VALUES
    			|| feature == DdlPackage.Literals.STATE_VARIABLE_COMPONENT_TYPE__DEC_TYPES
    			|| feature == DdlPackage.Literals.STATE_VARIABLE_COMPONENT_TYPE__TRANS_CONSTRAINT
    			|| feature == DdlPackage.Literals.COMPONENT__TIMELINES
    			;
    }
    
    private static boolean isDuplicate(EnumLiteral self) {
    	Domain domain = EcoreUtil2.getContainerOfType(self,Domain.class);
    	if (domain != null) {
    		String name = self.getName();
    		return
    		domain.getElements().stream().filter(o -> o instanceof EnumerationParameterType).
    		map(o -> (EnumerationParameterType)o).flatMap(o -> o.getValues().stream()).
    		filter(l -> l != self).map(l -> l.getName()).filter(s -> name.equals(s)).
    		findAny().isPresent();
    	}
    	return false;
    }
    
    private static boolean hasDuplicates(EObject self, String name) {
    	EObject cont = self.eContainer();
    	EReference ref = self.eContainmentFeature();
    	if (!mustCheckDuplicates(ref))
    		return false;
    	boolean duplicate = false;
    	if (ref.isMany()) {
    		@SuppressWarnings("unchecked")
			EList<EObject> list = (EList<EObject>)cont.eGet(ref, true);
    		duplicate = 
    				list.stream().
    				filter(o -> o != self && name.equals(DdlNameProvider.getObjName(o))).
    				findAny().isPresent();
    	}
    	if (self instanceof EnumLiteral)
    		duplicate |= isDuplicate((EnumLiteral)self);
    	return duplicate;
    }
    
    public static EObject uniqueName(EObject self, String name) {
    	if (name == null)
    		return self;
    	if (hasDuplicates(self, name)) {
    		throw new InvalidParameterException("Duplicate identifier "+name);
    	}
    	else
    		self.eSet(
    				self.eClass().getEStructuralFeature("name"),
    				name
    				);
    		
    	return self;
    }
}
