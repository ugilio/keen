package it.cnr.istc.keen.ui.labeling

import static it.cnr.istc.keen.ddl.DdlPackage.Literals.*;

import org.eclipse.emf.ecore.EClass
import java.util.concurrent.ConcurrentHashMap

class DdlImageProvider {
	
	private ConcurrentHashMap<EClass,String> icons;
	
	def getDdlImage() {
		return "ddl_file.png";
	}
	
	def getPdlImage() {
		return "pdl_file.png";
	}
	
	def image(EClass cls) {
		var icon = icons.get(cls);
		if (icon==null)
			icon=handleSuperType(cls);
		if ("".equals(icon))
			icon=null;
		return icon;
	}
	
	private def handleSuperType(EClass cls) {
		val sup = cls.getESuperTypes();
		for (st : sup)
			if (icons.containsKey(st))
			{
				val ic = icons.get(st);
				icons.put(cls,ic);
				return ic;
			}
		//Avoid looking up superclasses each time if this icon does not exist
		icons.put(cls,"");
		return null;
	}
	
	new() {
		icons=new ConcurrentHashMap(30);
		icons.put(DOMAIN,"domain.png");
		icons.put(TEMPORAL_MODULE,"module.png");
		icons.put(ENUMERATION_PARAMETER_TYPE,"enumtype.png");
		icons.put(ENUM_LITERAL,"enumliteral.png");
		icons.put(NUMERIC_PARAMETER_TYPE,"numerictype.png");
		icons.put(COMPONENT_TYPE,"componenttype.png");
		icons.put(COMPONENT_DECISION_TYPE,"decisiontype.png");
		icons.put(TRANSITION_CONSTRAINT,"constraint.png");
		icons.put(COMPONENT,"component.png");
		icons.put(TIMELINE,"timeline.png");
		icons.put(TIMELINE_SYNCHRONIZATION,"timeline.png");
		icons.put(SYNCHRONIZATION,"synchronization.png");
		icons.put(PROBLEM,"problem.png");
	}
}