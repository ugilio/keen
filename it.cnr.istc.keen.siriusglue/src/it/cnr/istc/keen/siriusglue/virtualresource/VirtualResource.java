package it.cnr.istc.keen.siriusglue.virtualresource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class VirtualResource extends ResourceImpl {

	private URI realResourceURI;
	
	private static URIHandler uriHandler = new VirtualResourceURIHandler();

	public VirtualResource(URI uri) {
		super(uri);
		updateRealResourceURI(uri);
	}

	public VirtualResource() {
		super();
	}
	
	@Override
	public void setURI(URI uri) {
		updateRealResourceURI(uri);
		super.setURI(uri);
	}

	
	private void updateRealResourceURI(URI virtualURI) {
		realResourceURI = getRealResourceURI(virtualURI);
	}

	private void replaceProxyURI(EObject object) {
		InternalEObject o = (InternalEObject)object;
		if (o.eIsProxy())
			o.eSetProxyURI(Utils.copyURI("virtual", o.eProxyURI()));
	}
	
	protected URIConverter getURIConverter() {
		addURIHandler();
		return super.getURIConverter();
	}
	
	private void addURIHandler() {
		List<URIHandler> uriHandlers = super.getURIConverter().getURIHandlers();
		if (!uriHandlers.contains(uriHandler)) {
			uriHandlers.add(0, uriHandler);
		}
	}
	
	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		//Do nothing
	}
	
	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options)
	{
		Resource realResource = getResourceSet().getResource(realResourceURI, true);
		
		EcoreUtil.resolveAll(realResource);
		EList<EObject> origContents = realResource.getContents();

		// initialize contents
		EList<EObject> thisContents = super.getContents();
		if (thisContents != null && !thisContents.isEmpty())
			thisContents.clear();

		assert thisContents != null : "contents should not be null";
		for (EObject root : origContents)
			thisContents.add(EcoreUtil.copy(root));
		//This should not be necessary!
		for (Iterator<EObject> it = getAllContents(); it.hasNext(); )
			replaceProxyURI(it.next());
	}

	public URI getRealResourceURI() {
		return realResourceURI;
	}
	
	public static URI getVirtualResourceURI(URI uri) {
		return Utils.copyURI("virtual", uri);
	}
	
	public static URI getRealResourceURI(URI uri) {
		return Utils.copyURI("platform", uri);
	}
}
