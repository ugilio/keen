package it.cnr.istc.keen.scoping

import org.eclipse.xtext.scoping.impl.DefaultGlobalScopeProvider
import org.eclipse.xtext.scoping.IScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.EClass
import com.google.common.base.Predicate
import org.eclipse.xtext.resource.IEObjectDescription
import it.cnr.istc.keen.ddl.Problem
import it.cnr.istc.keen.ddl.Domain
import org.eclipse.xtext.scoping.impl.ImportScope
import org.eclipse.xtext.scoping.impl.ImportNormalizer
import java.util.Collections
import com.google.inject.Inject
import org.eclipse.xtext.util.IResourceScopeCache
import org.eclipse.xtext.resource.ISelectable
import com.google.inject.Provider
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.scoping.impl.MultimapBasedSelectable
import org.eclipse.xtext.resource.IResourceDescription

class DdlGlobalScopeProvider extends DefaultGlobalScopeProvider {

	@Inject
	private IResourceScopeCache cache = IResourceScopeCache.NullImpl.INSTANCE;
	
	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;
	
	override getScope(Resource context, boolean ignoreCase, EClass type, Predicate<IEObjectDescription> filter) {
		//If it's not a problem, it can only see its own file
		val problem = getProblem(context);
		if (problem == null)
			return IScope.NULLSCOPE;
			
		//To avoid cycles: Domains are always globally visible
		if (type.instanceClass==Domain)
		{
			//return a global scope made of domains only
			return super.getScope(context,ignoreCase,type,[o |
					val uri = o.getEObjectURI();
					uri.fragment.split("/").length==3 && uri.fragment.startsWith("//@domain")
			]);
		}
		//Else, problem.domain's resolution should not be in progress
		val domain = problem.domain;
		if (domain!=null && !domain.eIsProxy())
		{
			var qn = qualifiedNameProvider.getFullyQualifiedName(domain);
			var res = domain.eResource;
			val uri = res.URI;
			val resdesc = context.resourceDescriptions.getResourceDescription(uri);				
			val descs = getAllExported(res,resdesc);
			val normalizer = new ImportNormalizer(qn,true,ignoreCase);
			
			return new ImportScope(Collections.singletonList(normalizer),IScope.NULLSCOPE,descs,type,ignoreCase);
		}
		return IScope.NULLSCOPE;
	}	
	
	private def getAllExported(Resource resource, IResourceDescription resdesc) {
		return cache.get("allExported", resource, new Provider<ISelectable>() {
			override get() {
				return new MultimapBasedSelectable(resdesc.exportedObjects);
			}
		});
	}

 	private def getProblem(Resource res)
	{
		val contents = res.getContents();
		if (contents.size>0)
		{
			val root = contents.get(0);
			for (c : root.eContents)
				if (c instanceof Problem)
					return (c as Problem);
		}
		return null;
	}
}