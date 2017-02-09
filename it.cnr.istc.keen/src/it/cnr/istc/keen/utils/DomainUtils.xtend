package it.cnr.istc.keen.utils

import org.eclipse.emf.ecore.EObject
import com.google.inject.Inject
import org.eclipse.xtext.resource.IResourceDescriptionsProvider
import java.util.Collections
import org.eclipse.xtext.resource.IEObjectDescription

class DomainUtils {
	private static final String DOMAINCLASSNAME = it.cnr.istc.keen.ddl.Domain.getSimpleName();

	@Inject
	private IResourceDescriptionsProvider resDescProvider;

	def getAllDomainDescriptors(EObject object) {
		val res = object.eResource;
		var rs = res?.resourceSet;
		if (rs==null)
			return Collections.<IEObjectDescription>emptyList();
		var all = resDescProvider.getResourceDescriptions(rs);
		return all.exportedObjects.filter[o|o.EClass.name.equals(DOMAINCLASSNAME)];
	}	
}