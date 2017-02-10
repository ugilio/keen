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
