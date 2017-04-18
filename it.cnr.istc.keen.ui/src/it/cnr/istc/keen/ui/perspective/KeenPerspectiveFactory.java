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
 * Inspired by org.eclipse.jdt.internal.ui.JavaPerspectiveFactory
 */
package it.cnr.istc.keen.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;

public class KeenPerspectiveFactory implements IPerspectiveFactory {
	
	public static final String ID = "it.cnr.istc.keen.ui.perspective.keenPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();

		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea);
		folder.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		//outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder(IPageLayout.ID_PROGRESS_VIEW);

		IFolderLayout outlineFolder = layout.createFolder("right", IPageLayout.RIGHT, (float)0.75, editorArea);
		outlineFolder.addView(IPageLayout.ID_OUTLINE);

		outlineFolder.addPlaceholder(TemplatesView.ID);

		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		// views - ddl
		//layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);

		// views - search
		//layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - debugging
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		layout.addShowViewShortcut(TemplatesView.ID);

		// new actions - Ddl project creation wizard
		layout.addNewWizardShortcut("it.cnr.istc.keen.ui.wizard.DdlNewProjectWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");

		// 'Window' > 'Open Perspective' contributions
		//layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
	}

}
