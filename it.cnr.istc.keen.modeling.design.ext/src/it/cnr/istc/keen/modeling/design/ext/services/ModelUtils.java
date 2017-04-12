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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;

public class ModelUtils {
	protected static ModelAccessor getAccessor(EObject self) {
    	Session session = SessionManager.INSTANCE.getSession(self);
    	if (session == null)
        	throw new RuntimeException("getAccessor(): No Sirius session for object "+self);
    	return session.getModelAccessor();
    }
	
    protected static void setMulti(ModelAccessor accessor, EObject obj, Object value, int featureID) {
    	EStructuralFeature feat = obj.eClass().getEStructuralFeature(featureID);
		try
		{
			accessor.eAdd(obj, feat.getName(), value);
		}
		catch (Exception e)
		{
			assert false : "Exception in setMulti(), invalid feature ID: "+e.getMessage();
		}
    }
}
