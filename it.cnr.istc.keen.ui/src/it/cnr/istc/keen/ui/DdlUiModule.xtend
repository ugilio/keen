/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *
 * generated by Xtext 2.10.0
 */
package it.cnr.istc.keen.ui

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import it.cnr.istc.keen.ui.outline.DdlOutlinePage
import it.cnr.istc.keen.ui.syntaxcoloring.DdlHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import it.cnr.istc.keen.ui.syntaxcoloring.DdlTokenToAttributeIdMapper
import it.cnr.istc.keen.ui.syntaxcoloring.DdlSemanticHighlightingCalculator
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import it.cnr.istc.keen.ui.refactoring.DdlRenameStrategy
import it.cnr.istc.keen.ui.refactoring.DdlReferenceUpdater
import it.cnr.istc.keen.ui.refactoring.DdlDependentItemsCalculator
import it.cnr.istc.keen.ui.wizard.DdlCustomizedProjectCreator
import it.cnr.istc.keen.ui.nature.SilentNatureAddingEditorCallback

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class DdlUiModule extends AbstractDdlUiModule {
	override bindIContentOutlinePage() {
		return DdlOutlinePage;
	}
	
	def Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return DdlHighlightingConfiguration;
	}
	
	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		return DdlTokenToAttributeIdMapper;
	}
	
	def Class<? extends ISemanticHighlightingCalculator> bindIdeSemanticHighlightingCalculator() {
		return DdlSemanticHighlightingCalculator;
	}
	
	override bindIRenameStrategy() {
		return DdlRenameStrategy;
	}
	
	//copied from Xtext 2.10 to workaround a bug in RefactoringCrossReferenceSerializer
	override bindIReferenceUpdater() {
		return DdlReferenceUpdater;
	}
	
	override bindIDependentElementsCalculator() {
		return DdlDependentItemsCalculator;
	}
	
	override bindIProjectCreator() {
		return DdlCustomizedProjectCreator;
	}
	
	override bindIXtextEditorCallback() {
		return SilentNatureAddingEditorCallback;
	}
}