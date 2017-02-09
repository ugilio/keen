package it.cnr.istc.keen.siriusglue.synchronizer;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.diagram.ui.tools.api.editor.DDiagramEditor;

import com.google.inject.Inject;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class DiagramModificationListener extends ModificationListener {
	
	private Reconciler reconciler;
	private DiagramSaveListener saveListener;

	@Inject
	public DiagramModificationListener(ResourceTracker resourceTracker, Reconciler reconciler,
			DiagramSaveListener saveListener) {
		super(resourceTracker);
		this.reconciler = reconciler;
		this.saveListener = saveListener;
	}
	
	public void editorOpened(DDiagramEditor editor) {
		URI uri = null;
		VirtualResource vres = ResourceUtils.getVirtualResourceForEditor(editor);
		if (vres != null)
			uri = null;
		resourceTracker.opening(uri);
		try
		{
			TransactionalEditingDomain ted = 
					editor.getSession().getTransactionalEditingDomain();
			if (ted!=null) {
				saveListener.registerSiriusNotification(ted);
				ted.addResourceSetListener(diagramChangeListener);
			}
			editorActivated(editor);
		}
		finally
		{
			resourceTracker.opened(uri);
		}
	}
	
	public void editorClosed(DDiagramEditor editor) {
		TransactionalEditingDomain ted = 
				editor.getSession().getTransactionalEditingDomain();
		if (ted!=null) {
			ted.removeResourceSetListener(diagramChangeListener);
			saveListener.unregisterSiriusNotification(ted);
		}
	}	
	
	private ResourceSetListener diagramChangeListener = new ResourceSetListenerImpl(){
		@Override
		public void resourceSetChanged(ResourceSetChangeEvent event) {
			for (Notification n : event.getNotifications())
				handleObjectModified(n.getNotifier());
		}
		
		@Override
		public boolean isPostcommitOnly() {
			return true;
		}
	};
	
	public void editorActivated(DDiagramEditor editor) {
		VirtualResource vres = ResourceUtils.getVirtualResourceForEditor(editor);
		if (vres!=null)
			reconciler.updateIfPeerWasModified(vres, vres.getRealResourceURI(), editor);
	}

}
