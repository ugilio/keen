package it.cnr.istc.keen.naming

import it.cnr.istc.keen.ddl.DdlPackage
import it.cnr.istc.keen.ddl.ParameterConstraint
import it.cnr.istc.keen.ddl.ProblemTemporalConstraint
import it.cnr.istc.keen.ddl.SGSVComponentDecision
import it.cnr.istc.keen.ddl.SGSVTransitionConstraint
import it.cnr.istc.keen.ddl.SSVComponentDecision
import it.cnr.istc.keen.ddl.SSVTransitionConstraint
import it.cnr.istc.keen.ddl.Synchronization
import it.cnr.istc.keen.ddl.TemporalConstraint
import it.cnr.istc.keen.ddl.TimelineSynchronization
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

class DdlNameProvider {
	private def static getTextContent(EObject obj, EStructuralFeature feature)
	{
		val nodes = NodeModelUtils.findNodesForFeature(obj,feature);
		if (nodes.size()!=1)
			return null;
		val node = nodes.get(0);
		return node?.text?.trim();
	}
	
	private def static getTextContentSync(EObject obj, EStructuralFeature feature)
	{
		var text = getTextContent(obj,feature);
		if (text==null)
			return text;
		val bracket = text.indexOf('>');
		if (bracket!=-1)
			text=text.substring(bracket+1);
		val paren = text.indexOf('(');
		if (paren!=-1)
			text=text.substring(0,paren);
		return text.trim();
	}
	
	private def static getFirstWord(EObject obj)
	{
		val node = NodeModelUtils.getNode(obj);
		val text = node?.text?.trim();
		return text?.split("\\s")?.get(0);
	}
	
	
	//Example: VALUE Idle() [1, +INF]
	protected def static dispatch doGetObjName(SGSVTransitionConstraint obj)
	{
		return getTextContent(obj,DdlPackage.Literals.SGSV_TRANSITION_CONSTRAINT__VALUE);
	}
	
	//Example: VALUE Idle() [1, +INF]
	protected def static dispatch doGetObjName(SSVTransitionConstraint obj)
	{
		return getTextContent(obj,DdlPackage.Literals.SSV_TRANSITION_CONSTRAINT__VALUE);
	}

	protected def static dispatch doGetObjName(SGSVComponentDecision obj)
	{
		return getTextContent(obj,DdlPackage.Literals.SGSV_COMPONENT_DECISION__VALUE);
	}
	
	protected def static dispatch doGetObjName(SSVComponentDecision obj)
	{
		return getTextContent(obj,DdlPackage.Literals.SSV_COMPONENT_DECISION__VALUE);
	}
	
	//Example: SYNCHRONIZE RobotController.functions
	protected def static dispatch doGetObjName(TimelineSynchronization obj)
	{
		return getTextContent(obj,DdlPackage.Literals.TIMELINE_SYNCHRONIZATION__TIMELINE);
	}
	
	//Example: VALUE Assembly(?piece)
	protected def static dispatch doGetObjName(Synchronization obj)
	{
		return getTextContentSync(obj,DdlPackage.Literals.SYNCHRONIZATION__VALUE);
	}

	//Example: STARTS-DURING [0, +INF] [0, +INF] task0;	
	protected def static dispatch doGetObjName(TemporalConstraint obj)
	{
		return getFirstWord(obj);
	}
	
	//Example: STARTS-DURING [0, +INF] [0, +INF] task0;	
	protected def static dispatch doGetObjName(ProblemTemporalConstraint obj)
	{
		return getFirstWord(obj);
	}
	
	//Example: abc = def	
	protected def static dispatch doGetObjName(ParameterConstraint obj)
	{
		return getTextContent(obj,DdlPackage.Literals.PARAMETER_CONSTRAINT__OP);
	}
	
	protected def static dispatch doGetObjName(EObject obj)
	{
		val feature = obj.eClass().getEStructuralFeature("name");
		if (feature != null && String.equals(feature.getEType().getInstanceClass()))
			return obj.eGet(feature) as String;
		return null;
	}
	
	def static getObjName(EObject obj)
	{
		return doGetObjName(obj);
	}
}