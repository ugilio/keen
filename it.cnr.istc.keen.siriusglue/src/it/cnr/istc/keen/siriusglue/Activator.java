package it.cnr.istc.keen.siriusglue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Activator extends Plugin {
	
	private static final Logger logger = Logger.getLogger(Activator.class);
	
	private static Activator instance;
	
	private Injector injector;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		injector = Guice.createInjector(new Module());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		injector = null;
		instance = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return instance;
	}
	
	public static Logger getLogger() {
		return logger;
	}

	public Injector getInjector() {
		return injector;
	}
	
}
