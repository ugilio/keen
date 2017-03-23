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

import java.util.Collection;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;

public class Reconciler {
	
	private ResourceTracker resourceTracker;

	private CrossReferenceUpdater crossReferenceUpdater;

	@Inject
	public Reconciler(ResourceTracker resourceTracker) {
		this.resourceTracker = resourceTracker;
		this.crossReferenceUpdater = new CrossReferenceUpdater();
	}

	void updateIfPeerWasModified(Resource res, URI peerURI, IEditorPart editor) {
		if (resourceTracker.isModified(peerURI)) {
			IWorkbenchPage activePage = ((IEditorPart)editor).getEditorSite().getPage();
			Resource peerRes = ResourceUtils.getOpenResource(activePage, peerURI);
			if (peerRes == null)
				peerRes = ResourceUtils.getOpenResourceAnywhereElse(activePage, peerURI);
			IXtextDocument doc = (editor instanceof XtextEditor) ? ((XtextEditor)editor).getDocument() : null;
			if (peerRes != null)
				reconcile(doc,res,peerRes);
			else
				resourceTracker.setUnmodified(peerURI);
		}
	}

	void reconcileInUIThread(IXtextDocument doc, Resource outdatedResource, Resource modifiedResource)
	{
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				reconcile(doc, outdatedResource, modifiedResource);
			}
		});
	}
	
	private void reconcile(IXtextDocument doc, Resource outdatedResource, Resource modifiedResource)
	{
		boolean wasModified = resourceTracker.isModified(outdatedResource.getURI());
		
		try
		{
			if (doc != null)
				doReconcile(doc, modifiedResource);
			else
				doReconcile(outdatedResource, modifiedResource);
		}
		finally
		{
			outdatedResource.setModified(true);
			resourceTracker.setUnmodified(modifiedResource.getURI());
			if (!wasModified)
				resourceTracker.setUnmodified(outdatedResource.getURI());
		}
	}
	
	private void doReconcile(IXtextDocument outdatedDocument, Resource modifiedResource) {
		outdatedDocument.modify(new IUnitOfWork.Void<XtextResource>() {
			@Override
			public void process(XtextResource outdatedResource) throws Exception {
				IComparisonScope scope = new DefaultComparisonScope(outdatedResource, modifiedResource, null);
				final Comparison comparison = EMFCompare.builder().build().compare(scope);

				Collection<SettingExt> toRefresh =
						crossReferenceUpdater.getObjectsToRefresh(comparison.getDifferences());

				IMerger.Registry mergerRegistry = EMFCompareRCPPlugin.getDefault().getMergerRegistry();
				final IBatchMerger merger = new BatchMerger(mergerRegistry);

				merger.copyAllRightToLeft(comparison.getDifferences(), new BasicMonitor());

				crossReferenceUpdater.refreshReferences(toRefresh);
			}
		});
	}
	
	private void doReconcile(Resource outdatedResource, Resource modifiedResource) {
		IComparisonScope scope = new DefaultComparisonScope(outdatedResource, modifiedResource, null);
		final Comparison comparison = EMFCompare.builder().build().compare(scope);

		IMerger.Registry mergerRegistry = EMFCompareRCPPlugin.getDefault().getMergerRegistry();
		final IBatchMerger merger = new BatchMerger(mergerRegistry);

		final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(outdatedResource);
		if (editingDomain == null)
			throw new TmpException(String.format("Cannot update resource '%s' (no editing domain)",outdatedResource.getURI()));
		editingDomain.getCommandStack()
		.execute(new RecordingCommand(editingDomain, "Update peer models after change") {

			@Override
			protected void doExecute() {
				merger.copyAllRightToLeft(comparison.getDifferences(), new BasicMonitor());
			}
		});
	}	
}
