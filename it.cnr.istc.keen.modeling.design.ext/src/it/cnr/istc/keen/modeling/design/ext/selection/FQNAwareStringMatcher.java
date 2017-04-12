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
package it.cnr.istc.keen.modeling.design.ext.selection;

import org.eclipse.sirius.common.tools.api.util.StringMatcher;

public class FQNAwareStringMatcher extends StringMatcher {

	public FQNAwareStringMatcher(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
		super(pattern, ignoreCase, ignoreWildCards);
	}
	
	@Override
	public boolean match(String text) {
		boolean ok = super.match(text);
		if (!ok && text != null && !text.startsWith("*")) {
			//match the segment after the last dot
			int pos = text.lastIndexOf(".");
			if (pos>=0)
				return super.match(text.substring(pos+1));
		}
		return ok;
	}

}
