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
package it.cnr.istc.keen.ui.syntaxcoloring

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper

class DdlTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper{
	
	override calculateId(String tokenName, int tokenType) {
		if("RULE_VARID".equals(tokenName)) {
			return DdlHighlightingConfiguration.VARIABLE_ID;
		}
		return super.calculateId(tokenName, tokenType);
	}	
}