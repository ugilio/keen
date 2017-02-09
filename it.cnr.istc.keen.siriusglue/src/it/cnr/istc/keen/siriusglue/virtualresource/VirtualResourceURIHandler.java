package it.cnr.istc.keen.siriusglue.virtualresource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;

public class VirtualResourceURIHandler extends URIHandlerImpl {
	
	private static InputStream nullInputStream = new InputStream() {
		@Override
		public int read() throws IOException {
			return -1;
		}
	};
	
	private static OutputStream nullOutputStream = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			//Do nothing
		}
	};

	@Override
	public boolean canHandle(URI uri) {
		return "virtual".equals(uri.scheme());
	}

	@Override
	public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
		return nullInputStream;
	}

	@Override
	public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
		return nullOutputStream;
	}
	
	@Override
	public boolean exists(URI uri, Map<?, ?> options)
	{
		URI realURI = VirtualResource.getRealResourceURI(uri);
		IFile file = 
		ResourcesPlugin.getWorkspace().getRoot().getFile(
				new Path(realURI.toPlatformString(true)));
		return file.exists();
	}
	
}
