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
package it.cnr.istc.keen.siriusglue.synchronizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;

import com.google.inject.Inject;

import it.cnr.istc.keen.siriusglue.virtualresource.VirtualResource;

public class ResourceSaveListener {
	
	private ResourceTracker resourceTracker;
	private ResourceUpdater resourceUpdater;

	@Inject
	public ResourceSaveListener(ResourceTracker resourceTracker, ResourceUpdater resourceUpdater) {
		this.resourceTracker = resourceTracker;
		this.resourceUpdater = resourceUpdater;
	}

	void enableResourceChangeListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(reschangeListener);
	}
	
	void disableResourceChangeListener() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(reschangeListener);
	}
	
	private void handleMoved(URI uri,IPath movedTo) {
		URI movedURI = URI.createPlatformResourceURI(movedTo.toPortableString(), true);
		uri = VirtualResource.getVirtualResourceURI(uri);
		movedURI = VirtualResource.getVirtualResourceURI(movedURI);
		resourceUpdater.renameVirtualResourceInAllSessions(uri, movedURI);
	}
	
	private void handleRemoved(URI uri) {
		if (!uri.isPlatformResource())
			return;
		
		Job job = new Job("Deleting related resources and representations") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				URI virtualURI = VirtualResource.getVirtualResourceURI(uri);
				resourceUpdater.deleteVirtualResourceInAllSessions(virtualURI);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}	
	
	private IResourceChangeListener reschangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource res = delta.getResource();
					if (!(res instanceof IFile))
						return true;
					URI uri = URI.createPlatformResourceURI(res.getFullPath().toPortableString(), true);
					int flags = delta.getFlags();
					switch (delta.getKind()) {
						case IResourceDelta.CHANGED:
							if (!resourceTracker.isModified(uri))
								return true;
							if ((flags & (IResourceDelta.CONTENT | IResourceDelta.REPLACED)) != 0)
								resourceUpdater.updateAndSaveAllPeerResources(uri, null);
							break;
						case IResourceDelta.REMOVED:
							if ((flags & IResourceDelta.MOVED_TO) != 0)
								handleMoved(uri,delta.getMovedToPath());
							else
								handleRemoved(uri);
							break;
					}
					return true;
				}
			};
			IResourceDelta delta = event.getDelta();
				try
				{
					if (delta != null && event.getType()==IResourceChangeEvent.POST_CHANGE)
						delta.accept(visitor);
				} catch (CoreException e)
				{
					throw new TmpException(e);
				};
		}
	};

}
