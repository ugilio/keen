package it.cnr.istc.keen.siriusglue;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

public class ExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return Activator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return Activator.getInstance().getInjector();
	}
	
}
