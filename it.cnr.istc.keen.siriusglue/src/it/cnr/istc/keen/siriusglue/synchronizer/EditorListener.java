package it.cnr.istc.keen.siriusglue.synchronizer;

import org.eclipse.sirius.diagram.ui.tools.api.editor.DDiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.editor.XtextEditor;

import com.google.inject.Inject;

public class EditorListener {

	private DiagramModificationListener diagramModificationListener;
	private XtextModificationListener xtextModificationListener;
	private Controller controller;
	
	@Inject
	public EditorListener(DiagramModificationListener diagramModificationListener,
			XtextModificationListener xtextModificationListener,
			Controller controller) {
		this.diagramModificationListener = diagramModificationListener;
		this.xtextModificationListener = xtextModificationListener;
		this.controller = controller;
	}
	
	public void install() {
		IWorkbench wb = PlatformUI.getWorkbench();
		for (IWorkbenchWindow w : wb.getWorkbenchWindows()) {
			for (IWorkbenchPage p : w.getPages()) {
				for (IEditorReference ref : p.getEditorReferences()) {
					IEditorPart editor = ref.getEditor(false);
					if (editor != null)
						partListener.partOpened(editor);
				}
				p.addPartListener(partListener);
			}
			w.addPageListener(pageListener);
		}
		wb.addWindowListener(windowListener);
		controller.enableAllSaveNotifications();
	}
	
	private IWindowListener windowListener = new IWindowListener() {
		@Override
		public void windowOpened(IWorkbenchWindow window) {
			window.addPageListener(pageListener);
		}
		@Override
		public void windowClosed(IWorkbenchWindow window) {
			window.removePageListener(pageListener);
		}
		
		@Override
		public void windowDeactivated(IWorkbenchWindow window) {}
		@Override
		public void windowActivated(IWorkbenchWindow window) {}
	};
	
	private IPageListener pageListener = new IPageListener() {
		
		@Override
		public void pageOpened(IWorkbenchPage page) {
			page.addPartListener(partListener);
		}
		
		@Override
		public void pageClosed(IWorkbenchPage page) {
			page.removePartListener(partListener);
		}
		
		@Override
		public void pageActivated(IWorkbenchPage page) {}
	};
	
	private IPartListener partListener = new IPartListener() {
		
		@Override
		public void partOpened(IWorkbenchPart part) {
			if (part instanceof XtextEditor)
				xtextModificationListener.editorOpened((XtextEditor)part);
			else if (part instanceof DDiagramEditor)
				diagramModificationListener.editorOpened((DDiagramEditor)part);
		}
		
		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof XtextEditor)
				xtextModificationListener.editorClosed((XtextEditor)part);
			else if (part instanceof DDiagramEditor)
				diagramModificationListener.editorClosed((DDiagramEditor)part);
		}
		
		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof XtextEditor)
				xtextModificationListener.editorActivated((XtextEditor)part);
			else if (part instanceof DDiagramEditor)
				diagramModificationListener.editorActivated((DDiagramEditor)part);
		}
		
		@Override
		public void partDeactivated(IWorkbenchPart part) {}
		@Override
		public void partBroughtToTop(IWorkbenchPart part) {}
	};


}
