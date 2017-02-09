package it.cnr.istc.keen.siriusglue.synchronizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DRepresentationDescriptor;
import org.eclipse.sirius.viewpoint.DRepresentationElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import com.google.inject.Inject;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class ResourceUpdater {

	private Controller controller;
	private ResourceTracker resourceTracker;
	private Reconciler reconciler;
	
	@Inject
	public ResourceUpdater(Controller controller, ResourceTracker resourceTracker, Reconciler reconciler) {
		super();
		this.controller = controller;
		this.resourceTracker = resourceTracker;
		this.reconciler = reconciler;
	}
	
	//FIXME: This is to break dependency cycle
	protected void setController(Controller controller) {
		this.controller = controller;
	}

	private void saveFromXtextEditor(IXtextDocument doc, URI uri) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart editor = ResourceUtils.findEditorFor(doc, uri);
				if (editor != null)
					editor.doSave(new NullProgressMonitor());
			}
		});
	}
	
	private Object findBestResource(Set<Object> resources) {
		if (resources==null)
			return null;
		for (Object o : resources)
			if (o instanceof IXtextDocument)
				return o;
		for (Object o : resources)
			return o;
		return null;
	}
	
	private void doUpdateAndSaveAllPeerResources(URI uri, Resource res) {
		URI peerURI = uri.isPlatformResource() ?
				VirtualResource.getVirtualResourceURI(uri) :
				VirtualResource.getRealResourceURI(uri);
		Set<Object> resources = 
				res == null ? ResourceUtils.getOpenResourcesAnywhere(uri) : Collections.singleton(res);
		Object tmp = findBestResource(resources);
		assert (tmp instanceof IXtextDocument) || (tmp instanceof Resource) : "Unexpected resource type";
		if (tmp instanceof IXtextDocument)
			res = ResourceUtils.getResourceFromXtextDocument((IXtextDocument)tmp);
		else
			res = (Resource)tmp;
		Map<Object, Object> xtextSaveOptions = 
				SaveOptions.newBuilder().noValidation().getOptions().toOptionsMap();
		Object peer = findBestResource(ResourceUtils.getOpenResourcesAnywhere(peerURI));
		if (peer==null && peerURI.isPlatformResource())
			peer=new ResourceSetImpl().getResource(peerURI, true);
		
		Resource peerRes = null;
		IXtextDocument doc = null;
		if (peer instanceof IXtextDocument) {
			doc =(IXtextDocument)peer; 
			peerRes = ResourceUtils.getResourceFromXtextDocument(doc);
		}
		else
			peerRes = (Resource)peer;
		reconciler.reconcileInUIThread(doc,peerRes,res);
		try
		{
			controller.disableAllSaveNotifications();
			if (doc!=null)
				saveFromXtextEditor(doc, peerURI);
			else
				peerRes.save(xtextSaveOptions);
		}
		catch (IOException e)
		{
			throw new TmpException(e);
		}
		finally
		{
			controller.enableAllSaveNotifications();
		}
		
		resourceTracker.setUnmodified(uri);
		resourceTracker.setUnmodified(peerURI);
	}
	
	private Object saveRelatedResourcesLock = new Object();
	
	public void updateAndSaveAllPeerResources(URI uri, Resource res) {
		Job job = new Job("Saving related resources") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized(saveRelatedResourcesLock)
				{
					doUpdateAndSaveAllPeerResources(uri, res);
					return Status.OK_STATUS;
				}
			}
		};
		job.schedule();
	}
	
	public void renameVirtualResourceInAllSessions(URI oldURI, URI newURI) {
		for (Session s : SessionManager.INSTANCE.getSessions())
			for (Resource r : s.getSemanticResources())
				if (r.getURI().equals(oldURI))
					r.setURI(newURI);
	}
	
	private void deleteRepresentations(List<DRepresentationDescriptor> descs, Session s) {
		if (descs.isEmpty())
			return;
		DialectManager dm = DialectManager.INSTANCE;
		Resource aird = s.getSessionResource();
		final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(aird);
		if (editingDomain != null) {
			editingDomain.getCommandStack()
			.execute(new RecordingCommand(editingDomain, "Delete representations") {
				@Override
				protected void doExecute() {
					for (DRepresentationDescriptor d : descs)
						dm.deleteRepresentation(d, s);
				}
			});
		}
	}
	
	public void deleteVirtualResourceInAllSessions(URI uri) {
		//Delete representations
		DialectManager dm = DialectManager.INSTANCE;
		for (Session s : SessionManager.INSTANCE.getSessions()) {
			ArrayList<DRepresentationDescriptor> toDelete = new ArrayList<>();
			for (DRepresentationDescriptor d: dm.getAllRepresentationDescriptors(s)) {
				DRepresentation repr = d.getRepresentation();
				if (repr != null)
					reprLoop: for (DRepresentationElement e : repr.getRepresentationElements())
						for (EObject o : e.getSemanticElements()) {
							Resource res = o.eResource();
							if (res != null && uri.equals(res.getURI())) {
								toDelete.add(d);
								break reprLoop;
							}
						}
			}
			deleteRepresentations(toDelete, s);
		}
		
		//Delete resource
		for (Session s : SessionManager.INSTANCE.getSessions()) {
			boolean modified = false;
			for (Resource r : s.getSemanticResources())
				if (r.getURI().equals(uri)) {
					final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(r);
					if (editingDomain == null)
						throw new TmpException(String.format("Cannot delete resource '%s' (no editing domain)",uri));
					modified = true;
					editingDomain.getCommandStack()
					.execute(new RecordingCommand(editingDomain, "Delete peer resource") {
						@Override
						protected void doExecute() {
							s.removeSemanticResource(r, new NullProgressMonitor(), true);
						}
					});
					break;
				}
			if (modified)
				s.save(new NullProgressMonitor());
		}
	}	
}
