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
package it.cnr.istc.keen.modeling.design.ext.modelextender;

import java.util.HashSet;
import java.util.stream.Collectors;

public class CyclicResolutionException extends RuntimeException {
	private static final long serialVersionUID = 4434071478750282530L;

	private static String formatMessage(HashSet<Class<?>> resolving, Class<?> cls) {
		String already = "[" +
				resolving.stream().map(c -> c.getSimpleName()).
				collect(Collectors.joining(","))+
				"]";
		return String.format("Cycle in rule dependency. Trying to add %s but already resolving %s",
				cls.getSimpleName(),already);
	}
	
	public CyclicResolutionException(HashSet<Class<?>> resolving, Class<?> cls) {
		super(formatMessage(resolving, cls));
	}
}
