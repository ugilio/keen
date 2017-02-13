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

import java.util.HashMap;

import org.eclipse.emf.transaction.TransactionalEditingDomain;

public class CountingListenerList<T> {
	
	private HashMap<T, Integer> registeredListeners = new HashMap<>();
	
	public boolean offer(TransactionalEditingDomain domain, T listener) {
		synchronized(registeredListeners)
		{
			Integer count = registeredListeners.get(listener);
			boolean isNew = count == null;
			if (isNew)
				count = 1;
			else
				count++;
			registeredListeners.put(listener, count);
			return isNew;
		}
	}
	
	public boolean take(TransactionalEditingDomain domain, T listener) {
		synchronized(registeredListeners)
		{
			Integer count = registeredListeners.get(listener);
			boolean shouldUnregister = false;
			if (count!=null)
				count--;
			if (count==null || count<=0) {
				shouldUnregister=true;
				registeredListeners.remove(listener);
			}
			else
				registeredListeners.put(listener, count);
			return shouldUnregister;
		}
	}
}
