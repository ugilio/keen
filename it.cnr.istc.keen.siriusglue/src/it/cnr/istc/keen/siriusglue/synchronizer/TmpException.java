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

//FIXME: Implement a proper exception
public class TmpException extends RuntimeException {
	private static final long serialVersionUID = 1705424031620209744L;

	public TmpException(String msg) {
		super(msg);
	}

	public TmpException(Throwable t) {
		super(t);
	}

}