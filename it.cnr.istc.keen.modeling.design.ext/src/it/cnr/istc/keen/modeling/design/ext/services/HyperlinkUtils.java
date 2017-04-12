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
package it.cnr.istc.keen.modeling.design.ext.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.helper.task.ICommandTask;
import org.eclipse.sirius.business.api.helper.task.TaskHelper;
import org.eclipse.sirius.business.api.query.EObjectQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.description.DiagramDescription;
import org.eclipse.sirius.diagram.description.tool.Navigation;
import org.eclipse.sirius.diagram.description.tool.ToolFactory;
import org.eclipse.sirius.tools.api.command.SiriusCommand;
import org.eclipse.sirius.tools.api.command.ui.UICallBack;
import org.eclipse.sirius.viewpoint.ViewpointPackage;
import org.eclipse.sirius.viewpoint.provider.SiriusEditPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.EcoreUtil2;

import it.cnr.istc.keen.ddl.ComponentDecisionType;
import it.cnr.istc.keen.ddl.ComponentType;
import it.cnr.istc.keen.ddl.Ddl;
import it.cnr.istc.keen.ddl.Domain;
import it.cnr.istc.keen.ddl.InstantiatedComponentDecision;
import it.cnr.istc.keen.ddl.StateVariableComponentType;
import it.cnr.istc.keen.ddl.Timeline;
import it.cnr.istc.keen.ddl.TimelineSynchronization;
import it.cnr.istc.keen.modeling.design.ext.layout.LayoutHelper;

public class HyperlinkUtils {
	
	private static DiagramDescription getPreferredDiagramDescriptionFor(EObject object, EObject root, Session session) {
		List<DiagramDescription> descs = 
    	DialectManager.INSTANCE.getAvailableRepresentationDescriptions(
    			session.getSelectedViewpoints(false), root).stream().
			filter(repr -> repr instanceof DiagramDescription).map(repr -> (DiagramDescription)repr).
			collect(Collectors.toList());
		if (descs.size()>1) {
			String targetName = getPreferredDiagramNameFor(object);
			for (DiagramDescription d : descs)
				if (targetName.equals(d.getName()))
					return d;
		}
		if (descs.size()<=0)
			throw new RuntimeException("No diagram description found for "+object);
		return descs.get(0);
	}
	
	private static EObject getPreferredRootObject(EObject object) {
		EObject result = null;
		if (object instanceof Ddl)
			result = object; //EcoreUtil2.getContainerOfType(object, Ddl.class);
		if (object instanceof ComponentType)
			result = EcoreUtil2.getContainerOfType(object, Domain.class);
		if (object instanceof ComponentDecisionType)
			result = EcoreUtil2.getContainerOfType(object, StateVariableComponentType.class);
		if (object instanceof InstantiatedComponentDecision)
			result = EcoreUtil2.getContainerOfType(object, TimelineSynchronization.class);
		if (object instanceof Timeline)
			result = EcoreUtil2.getContainerOfType(object, Domain.class);
		if (result==null)
			throw new RuntimeException("No preferred root object for "+object);
		return result;
	}
	
	private static String getPreferredDiagramNameFor(EObject object) {
		if (object instanceof ComponentType)
			return LayoutHelper.DIAGNAME_DOMAINMODEL;
		if (object instanceof ComponentDecisionType)
			return LayoutHelper.DIAGNAME_TYPECONSTRAINTS;
		if (object instanceof InstantiatedComponentDecision)
			return LayoutHelper.DIAGNAME_SYNCDIAGRAM;
		if (object instanceof Timeline)
			return LayoutHelper.DIAGNAME_COMPDIAGRAM;
		if (object instanceof Ddl)
			return LayoutHelper.DIAGNAME_ROOT;
		throw new RuntimeException("No preferred diagram name for "+object);
	}
	
	private static DiagramEditor getCurrentDiagramEditor() {
		IWorkbench wb = PlatformUI.getWorkbench();
		DiagramEditor[] result = new DiagramEditor[1];
		wb.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
		    	IEditorPart part = wb.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    	if (part instanceof DiagramEditor)
		    		result[0]=(DiagramEditor)part;
			}
		});
    	return result[0];
	}
	
	/*
	private static DiagramDescription getCurrentDiagramDescription() {
    	DiagramEditor editor = getCurrentDiagramEditor();
    	Diagram diag = editor.getDiagram();
    	EObject element = diag.getElement();
    	if (element instanceof DDiagram)
    		return ((DDiagram)element).getDescription();
		return null;
	}
	*/
	
	public static void openTargetDiagram(EObject root, DiagramDescription desc, Session session,
			boolean createIfNotExistent) {
    	UICallBack cback = SiriusEditPlugin.getPlugin().getUiCallback();
    	TaskHelper tHelper = new TaskHelper(ModelUtils.getAccessor(root), cback);
    	Navigation nav = ToolFactory.eINSTANCE.createNavigation();
    	nav.setCreateIfNotExistent(createIfNotExistent);
    	nav.setDiagramDescription(desc);
    	ICommandTask ctask = tHelper.buildTaskFromModelOperation(root, nav);
		TransactionalEditingDomain ted = session.getTransactionalEditingDomain();
		SiriusCommand cmd = new SiriusCommand(ted,"Open diagram");
		cmd.getTasks().add(ctask);
    	PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				ted.getCommandStack().execute(cmd);
			}
		});
    	cmd.dispose();
	}
	
	private static DDiagramElement findTargetElement(EObject object, DiagramDescription desc) {
    	Collection<EObject> result = new EObjectQuery(object).
    			getInverseReferences(ViewpointPackage.Literals.DSEMANTIC_DECORATOR__TARGET);
    	List<DDiagramElement> candidates = 
    			result.stream().filter(o -> o instanceof DDiagramElement).
    			map(o -> (DDiagramElement)o).
    			filter(e -> e.getParentDiagram().getDescription()==desc).
    			collect(Collectors.toList());
    	if (candidates.size()>1) {
    		//prefer top-level nodes
    		List<DDiagramElement> newCandidates =
    		candidates.stream().filter(e -> e.eContainer() == e.getParentDiagram()).
    		collect(Collectors.toList());
    		if (newCandidates.size()>0)
    			candidates = newCandidates;
    	}
    	if (candidates.size()<=0)
    		return null;
    	return candidates.get(0);
	}
	
	private static void selectTargetElement(DDiagramElement targetDiagramElement) {
    	DiagramEditor editor = getCurrentDiagramEditor();
    	DiagramEditPart rootPart = editor.getDiagramEditPart();
    	IDiagramGraphicalViewer viewer = editor.getDiagramGraphicalViewer();
    	EditPart targetPart = rootPart.findEditPart(rootPart, targetDiagramElement);
    	
    	viewer.select(targetPart);
	}
    
	public static EObject openHyperLink(EObject self, EObject session, EObject target) {
		return openHyperLink(self, session, target, true);
	}
	
    public static EObject openHyperLink(EObject self, EObject session, EObject target,
    		boolean createIfNotExistent) {
    	EObject root = getPreferredRootObject(target);
    	DiagramDescription desc = getPreferredDiagramDescriptionFor(target, root, (Session)session);
    	
    	openTargetDiagram(root,desc, (Session)session, createIfNotExistent);
    	
		//Now the target should have a representation attached, if user did not cancel
    	DDiagramElement targetDiagramElement = findTargetElement(target,desc);
    	if (targetDiagramElement==null)
    		return self;
    	
    	selectTargetElement(targetDiagramElement);
    	return self;
    }
}
