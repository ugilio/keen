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
