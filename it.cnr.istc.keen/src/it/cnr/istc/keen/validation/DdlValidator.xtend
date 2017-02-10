/*
 * generated by Xtext 2.10.0
 */
package it.cnr.istc.keen.validation

import static it.cnr.istc.keen.ddl.DdlPackage.Literals.*;

import it.cnr.istc.keen.ddl.Domain
import org.eclipse.xtext.validation.Check
import it.cnr.istc.keen.ddl.EnumerationParameterType
import it.cnr.istc.keen.ddl.StateVariableComponentType
import it.cnr.istc.keen.ddl.TransitionConstraint
import it.cnr.istc.keen.ddl.Component
import it.cnr.istc.keen.ddl.TimelineSynchronization
import it.cnr.istc.keen.ddl.Synchronization
import it.cnr.istc.keen.ddl.Problem
import it.cnr.istc.keen.ddl.SVComponentDecision
import org.eclipse.emf.ecore.EStructuralFeature
import it.cnr.istc.keen.ddl.ComponentDecisionType
import it.cnr.istc.keen.ddl.ParType
import it.cnr.istc.keen.ddl.NumericParameterType
import it.cnr.istc.keen.ddl.ParameterConstraint
import it.cnr.istc.keen.ddl.EnumLiteral
import it.cnr.istc.keen.ddl.RenewableResourceComponentDecision
import it.cnr.istc.keen.ddl.ConsumableResourceComponentDecision
import java.util.Collections
import org.eclipse.emf.ecore.EObject
import com.google.inject.Inject
import it.cnr.istc.keen.conversion.NumberValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import java.util.List
import it.cnr.istc.keen.naming.DdlNameProvider
import org.eclipse.xtext.EcoreUtil2
import it.cnr.istc.keen.utils.DomainUtils
import it.cnr.istc.keen.ddl.StateVariableType

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class DdlValidator extends AbstractDdlValidator {
	
	public static val MISSING_MODULE = "missingModule";
	public static val EMPTY_ENUM = "emptyEnum";
	public static val EMPTY_DECTYPES = "emptyDecTypes"
	public static val EMPTY_CONSTR_MEETS = "emptyConstrMeets";
	public static val EMPTY_COMP_TIMELINES = "emptyCompTimelines";
	public static val MISSING_COMP_TYPE = "missingCompType";
	public static val EMPTY_SYNCHRONIZE = "emptySynchronize";
	public static val EMPTY_SYNCHRONIZATION = "emptySynchronization";
	public static val MISSING_PROBLEM_DOMAIN = "missingProblemDomain";
	
	public static val SIMPLE_GROUND_PARAMETERS = "simpleGroundHasParameters";
	public static val WRONG_NUMBER_PARAMETERS = "wrongNumberOfParameters";
	public static val TYPE_MISMATCH = "typeMismatch";
	public static val OUT_OF_RANGE = "outOfRange";
	
	public static val DUPLICATE_DOMAIN = "duplicateDomain"
	public static val DUPLICATE_IDENTIFIER = "duplicateIdentifier";
	
	@Inject
	private NumberValueConverter numConverter;
	
	@Inject
	private DomainUtils domainUtils;
	
	//Checks for missing values / empty references
	
	@Check
	def checkDomainHasTemporalModule(Domain domain) {
		if (domain.module == null)
			error("Missing temporal module specification",DOMAIN__MODULE,MISSING_MODULE);
	}
	
	@Check
	def checkEnumIsNotEmpty(EnumerationParameterType e) {
		if (e.values.isEmpty())
			error("Enumeration cannot be empty",ENUMERATION_PARAMETER_TYPE__VALUES,EMPTY_ENUM);
	}
	
	@Check
	def checkCompTypeHasDecision(StateVariableComponentType ct) {
		if (ct.decTypes.isEmpty())
			error("At least one decision must be specified",STATE_VARIABLE_COMPONENT_TYPE__DEC_TYPES,EMPTY_DECTYPES);
	}
	
	@Check
	def checkEmptyTransitionConstraint(TransitionConstraint c) {
		if (c.meets.isEmpty())
			warning("Empty transition constraint",TRANSITION_CONSTRAINT__MEETS,EMPTY_CONSTR_MEETS);
	}
	
	@Check
	def checkComponentWithoutTimelines(Component c) {
		if (c.timelines.isEmpty())
			warning("Component has no timelines",COMPONENT__TIMELINES,EMPTY_COMP_TIMELINES);
	}
	
	@Check
	def checkComponentWithoutType(Component c) {
		if (c.type==null)
			error("Missing component type",COMPONENT__TYPE,MISSING_COMP_TYPE);
	}
	
	@Check
	def checkEmptySynchronize(TimelineSynchronization ts) {
		if (ts.synchronizations.isEmpty())
			warning("Empty SYNCHRONIZE block",TIMELINE_SYNCHRONIZATION__SYNCHRONIZATIONS,EMPTY_SYNCHRONIZE);
	}
	
	@Check
	def checkEmptySynchronization(Synchronization s) {
		if (s.elements.isEmpty())
			warning("Empty synchronization",SYNCHRONIZATION__ELEMENTS,EMPTY_SYNCHRONIZATION);
	}
	
	@Check
	def checkProblemHasDomain(Problem p) {
		if (p.domain == null)
			error("Missing domain specification",PROBLEM__DOMAIN,MISSING_PROBLEM_DOMAIN);
	}



	@Check
	def checkSGSVZeroParameters(ComponentDecisionType cdt) {
		val ctype = cdt.eContainer as StateVariableComponentType;
		if (ctype != null && ctype.type==StateVariableType.SIMPLE_GROUND_STATE_VARIABLE)
			if (cdt.args.size()!=0)
				error("Decisions in SimpleGroundStateVariable types cannot have parameters",
					COMPONENT_DECISION_TYPE__ARGS,0,SIMPLE_GROUND_PARAMETERS);
	}

	//Checks for valid number of parameters
	
	private def void checkWrongNumberOfParameters(int expected, int actual, EStructuralFeature feat) {
		if (expected != actual) {
			error(String.format("Wrong number of parameters: got %d, expected %d",actual,expected),
				feat,Math.min(expected,actual),WRONG_NUMBER_PARAMETERS);
		}
	}

	@Check
	def checkCDNumberParameters(SVComponentDecision cd) {
		if (cd.value!=null)
			checkWrongNumberOfParameters(cd.value.args.size(),cd.paramValues.size(),SV_COMPONENT_DECISION__PARAM_VALUES);
	}
	
	private def getFormalParamList(EObject c) {
		val type = 
			switch c {
				SVComponentDecision: c.value
				RenewableResourceComponentDecision, 
				ConsumableResourceComponentDecision:  c
			}
		if (type==null) throw new AssertionError("Unknown class for formal parameter list");
		val list = 
			switch type {
				ComponentDecisionType: type.args
				RenewableResourceComponentDecision: null
				ConsumableResourceComponentDecision:  null
			}
		return list;
	}
	
	private def getActualParamList(EObject c) {
		val parlist =
			switch c {
				SVComponentDecision: c.paramValues
				RenewableResourceComponentDecision: Collections.singletonList(c.paramValue)
				ConsumableResourceComponentDecision:  Collections.singletonList(c.paramValue)
			} 
		if (parlist==null) throw new AssertionError("ParValue in unknown container");
		return parlist;
	}	
	
	private def checkEnumCompatibility(ParType formal, EnumLiteral el, EStructuralFeature feat) {
		val en = el.eContainer() as EnumerationParameterType;
		if (formal != en)
			warning(String.format("Type mismatch: got %s, expected %s",en.name,formal.name),
				feat,TYPE_MISMATCH);
	}
	
	private def checkNumberCompatibility(ParType formal, String nl, EStructuralFeature feat) {
		val n = numConverter.toValue(nl,null).intValue();
		if (!(formal instanceof NumericParameterType))
			warning(String.format("Type mismatch: got %s, expected %s","numeric constant",formal.name),
				feat,TYPE_MISMATCH)
		else {
			val range = (formal as NumericParameterType).range;
			try {
				val lb = numConverter.toValue(range.lb,null).intValue;
				val ub = numConverter.toValue(range.ub,null).intValue;
			if (n<lb || n>ub)
				warning(String.format("Numeric constant out of range"),feat,OUT_OF_RANGE);
			}
			catch (ValueConverterException e) {
				//ignore
			}
		}
	}
	
	@Check(NORMAL)
	def checkParamTypes(ParameterConstraint c) {
		val e = c.right?.enumeration;
		val n = c.right?.number;
		if (e==null && n==null)
			return;
		
		val parvalue = c.left?.^var?.ref;
		if (parvalue==null)
			return;
		val actualList = getActualParamList(parvalue.eContainer());
		val formalList = getFormalParamList(parvalue.eContainer());
		val idx = actualList.indexOf(parvalue);
		if (idx<0) throw new AssertionError("ParValue not found in container");
		if (formalList==null || idx>=formalList.size())
			return;
		val formal = formalList.get(idx)
		
		if (e != null) {
			checkEnumCompatibility(formal,e,PARAMETER_CONSTRAINT__RIGHT);
		}
		else if (n != null) {
			checkNumberCompatibility(formal,n,PARAMETER_CONSTRAINT__RIGHT);
		}
	}
	
	
	
	
	//Checks for unique names
	
	@Check
	def checkUniqueDomainName(Domain domain) {
		val domains = domainUtils.getAllDomainDescriptors(domain);
		val resUri = domain.eResource?.getURI();
		for (d: domains) {
			if (resUri.equals(d?.getEObjectURI()?.trimFragment()))
				return;
			if (d!=null && d.name.toString().equals(domain.name)) {
					error(String.format("Duplicate domain '%s'",domain.name),DOMAIN__NAME,DUPLICATE_DOMAIN);
					return;
				}
		}
	}
	
	private def getObjName(EObject obj)
	{
		return DdlNameProvider.getObjName(obj);
	}
	
	private def getByName(String name, List<EObject> list) {
		for (o : list)
			if (name.equals(getObjName(o)))
				return o;
		return null;
	} 
	
	private def checkDuplicateIdentifiers(EObject cont, EStructuralFeature feat)
	{
		val list = cont.eGet(feat) as List<EObject>;
		for (o : list) {
			val name = getObjName(o)
			if (getByName(name,list) != o)
				error(String.format("Duplicate identifier '%s'",name),feat,list.indexOf(o),DUPLICATE_IDENTIFIER);
		}
	}
	
	private def checkDuplicateEnumLiterals(EObject cont, EStructuralFeature feat)
	{
		val list = cont.eGet(feat) as List<EObject>;
		val domain = EcoreUtil2.getContainerOfType(cont,Domain);
		//check all EnumerationParameterType before this one
		for (e: domain.elements) {
			if (e instanceof EnumerationParameterType) {
				val otherList = e.eGet(feat) as List<EObject>;
				if (otherList == list)
					return;
				for (o : list) {
					val name = getObjName(o)
					if (getByName(name,otherList) != null)
						error(String.format("Duplicate identifier '%s'",name),feat,list.indexOf(o),DUPLICATE_IDENTIFIER);
				}
			}
		}
	}
	
	@Check
	def checkUnique(Domain domain) {
		checkDuplicateIdentifiers(domain,DOMAIN__ELEMENTS);
	}
	
	@Check
	def checkUnique(EnumerationParameterType en) {
		checkDuplicateIdentifiers(en,ENUMERATION_PARAMETER_TYPE__VALUES);
		checkDuplicateEnumLiterals(en,ENUMERATION_PARAMETER_TYPE__VALUES);
	}
	
	@Check
	def checkUnique(StateVariableComponentType type) {
		checkDuplicateIdentifiers(type,STATE_VARIABLE_COMPONENT_TYPE__DEC_TYPES);
		checkDuplicateIdentifiers(type,STATE_VARIABLE_COMPONENT_TYPE__TRANS_CONSTRAINT);
	}
	
	@Check
	def checkUnique(Component c) {
		checkDuplicateIdentifiers(c,COMPONENT__TIMELINES);
	}
	
	@Check
	def checkUnique(TimelineSynchronization ts) {
		checkDuplicateIdentifiers(ts,TIMELINE_SYNCHRONIZATION__SYNCHRONIZATIONS);
	}
}
