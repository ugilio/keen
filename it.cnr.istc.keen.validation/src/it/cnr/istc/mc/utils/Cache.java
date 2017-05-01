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
package it.cnr.istc.mc.utils;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.function.Function;

public class Cache {
	public static final Cache instance = new Cache();
	
	private WeakHashMap<Object, WeakReference<Object>> cache = new WeakHashMap<>();
	
	private Cache() {}
	
	@SuppressWarnings("unchecked")
	public <T,A> T getFromCache(A actual, Function<A, T> func)
	{
		WeakReference<Object> cached = cache.get(actual);
		Object strong = cached == null ? null : cached.get();
		if (strong == null)
		{
			strong = func.apply(actual);
			cache.put(actual, new WeakReference<Object>(strong));
		}
		return (T) strong;
	}	

	public void clear()
	{
		cache.clear();
	}
}
