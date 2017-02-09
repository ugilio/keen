package it.cnr.istc.keen.ui.refactoring

import com.google.inject.Inject
import it.cnr.istc.keen.ddl.Domain
import it.cnr.istc.keen.ddl.EnumLiteral
import it.cnr.istc.keen.ddl.EnumerationParameterType
import it.cnr.istc.keen.naming.DdlNameProvider
import it.cnr.istc.keen.utils.DomainUtils
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.jface.text.Region
import org.eclipse.ltk.core.refactoring.RefactoringStatus
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.ui.refactoring.impl.DefaultRenameStrategy
import org.eclipse.xtext.ui.refactoring.ui.IRenameElementContext

class DdlRenameStrategy extends DefaultRenameStrategy {
	
	private EObject targetElement;
	
	@Inject
	private DomainUtils domainUtils;
	
	
	override initialize(EObject targetElement, IRenameElementContext context) {
		val result = super.initialize(targetElement,context);
		this.targetElement=targetElement;
		return result;
	}
	
	
	private def getByName(String name, List<EObject> list) {
		for (o : list)
			if (name.equals(DdlNameProvider.getObjName(o)))
				return o;
		return null;
	}
	
	//TODO: put all unique-name related stuff somewhere else
	
	protected def dispatch checkUnique(String newName, EObject obj) {
		val feat = obj.eContainingFeature;
		if (feat==null || !feat.isMany)
			return null;
		val cont = obj.eContainer;
		val list = cont.eGet(feat) as List<EObject>;
		return getByName(newName,list); 
	}
	
	protected def dispatch checkUnique(String newName, EnumLiteral obj) {
		val domain = EcoreUtil2.getContainerOfType(obj,Domain);
		val allLiterals = domain.elements.filter(EnumerationParameterType).
			map[e|e.values].flatten.map[e|e as EObject].toList;
		return getByName(newName,allLiterals);
	}
	
	protected def dispatch checkUnique(String newName, Domain domain) {
		val domains = domainUtils.getAllDomainDescriptors(domain);
		val resUri = domain.eResource?.getURI();
		for (d: domains) {
			if (!resUri.equals(d?.getEObjectURI()?.trimFragment()))
				if (d!=null && d.name.toString().equals(newName)) {
					val object = d.getEObjectOrProxy();
					return EcoreUtil2.resolve(object,domain);
				}
		}
		return null;
	}
	
	override RefactoringStatus validateNewName(String newName) {
		val status = super.validateNewName(newName);
		if (newName==null)
			return status;
		val existing = checkUnique(newName,targetElement);
		if (existing!=null && existing!= targetElement)
			status.addEntry(new RefactoringStatusEntry(RefactoringStatus.ERROR,
			"Duplicate identifier: an element named '" + newName + "' already exists.",
			createContext(existing)));
		return status;
	}
	
	private def createContext(EObject obj) {
		//try to highlight the exact name only
		val feature = obj.eClass().getEStructuralFeature("name");
		val nodes = 
			if (feature != null)
				NodeModelUtils.findNodesForFeature(obj,feature);
		val node =
			if (nodes!=null && nodes.size()==1)
				nodes.get(0)
			else //else, get the whole node
				NodeModelUtils.getNode(obj);
				
		val region = 
			if (node != null)
				new Region(node.offset,node.length)
			else
				null;
		return new DdlStatusContext(obj.eResource, region);
	}
}