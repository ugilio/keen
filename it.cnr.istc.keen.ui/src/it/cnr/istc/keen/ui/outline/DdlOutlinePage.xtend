package it.cnr.istc.keen.ui.outline

import org.eclipse.xtext.ui.editor.outline.impl.OutlinePage

class DdlOutlinePage extends OutlinePage {
	override getDefaultExpansionLevel() {
		return 2;
	}
}