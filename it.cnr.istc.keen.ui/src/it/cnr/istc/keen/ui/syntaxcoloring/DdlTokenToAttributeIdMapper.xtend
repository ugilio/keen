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