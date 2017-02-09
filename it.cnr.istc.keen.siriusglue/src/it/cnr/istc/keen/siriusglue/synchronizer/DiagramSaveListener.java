package it.cnr.istc.keen.siriusglue.synchronizer;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.common.tools.api.resource.ResourceSetSync;
import org.eclipse.sirius.common.tools.api.resource.ResourceSyncClient;

import com.google.inject.Inject;

import org.eclipse.sirius.common.tools.api.resource.ResourceSetSync.ResourceStatus;

public class DiagramSaveListener {
	
	private ResourceTracker resourceTracker;
	private ResourceUpdater resourceUpdater;

	@Inject
	public DiagramSaveListener(ResourceTracker resourceTracker, ResourceUpdater resourceUpdater) {
		this.resourceTracker = resourceTracker;
		this.resourceUpdater = resourceUpdater;
	}
	
	private HashMap<ResourceSetSync, Integer> registeredResourceSetSync = new HashMap<>();
	
	public void registerSiriusNotification(TransactionalEditingDomain domain) {
		synchronized(registeredResourceSetSync)
		{
			ResourceSetSync sync = ResourceSetSync.getOrInstallResourceSetSync(domain);
			Integer count = registeredResourceSetSync.get(sync);
			if (count == null) {
				count = 1;
				sync.registerClient(syncClient);
			}
			else
				count++;
			registeredResourceSetSync.put(sync, count);
		}
	}
	
	public void unregisterSiriusNotification(TransactionalEditingDomain domain) {
		synchronized(registeredResourceSetSync)
		{
			ResourceSetSync sync = ResourceSetSync.getOrInstallResourceSetSync(domain);
			Integer count = registeredResourceSetSync.get(sync);
			if (count!=null)
				count--;
			if (count==null || count<=0) {
				sync.unregisterClient(syncClient);
				registeredResourceSetSync.remove(sync);
			}
			else
				registeredResourceSetSync.put(sync, count);
		}
	}
	
	public void disableSiriusNotifications() {
		synchronized(registeredResourceSetSync)
		{
			for (ResourceSetSync sync : registeredResourceSetSync.keySet())
				sync.unregisterClient(syncClient);
		}
	}

	public void enableSiriusNotifications() {
		synchronized(registeredResourceSetSync)
		{
			for (ResourceSetSync sync : registeredResourceSetSync.keySet())
				sync.registerClient(syncClient);
		}
	}
	
	private ResourceSyncClient syncClient = new ResourceSyncClient() {
		@Override
		public void statusesChanged(Collection<ResourceStatusChange> changes) {
			for (ResourceStatusChange c : changes) {
				Resource res = c.getResource();
				if (res != null && resourceTracker.isModified(res.getURI()) && c.getNewStatus()==ResourceStatus.SYNC)
					resourceUpdater.updateAndSaveAllPeerResources(res.getURI(), res);
			}
		}

		@Override
		public void statusChanged(Resource resource, ResourceStatus oldStatus, ResourceStatus newStatus) {}
	};
}
