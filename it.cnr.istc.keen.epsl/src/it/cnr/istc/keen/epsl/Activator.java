package it.cnr.istc.keen.epsl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Injector;

import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;
import it.cnr.istc.keen.ui.internal.KeenActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "it.cnr.istc.keen.epsl"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private Map<String, IRunModeExtension> extensions;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initExtensions();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public Injector getInjector() {
		return KeenActivator.getInstance().getInjector(KeenActivator.IT_CNR_ISTC_KEEN_DDL);
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage()));
	}
	
	private void initExtensions() {
		HashMap<String,IRunModeExtension> extensions = new HashMap<>();
		IConfigurationElement[] confElements = 
				RegistryFactory.getRegistry().getConfigurationElementsFor(IRunModeExtension.ID);
		for (IConfigurationElement ce: confElements)
		{
			String mode = ce.getAttribute("mode");
			try
			{
				Object o = ce.createExecutableExtension("class");
				if (o instanceof IRunModeExtension)
					extensions.put(mode, (IRunModeExtension)o);
			}
			catch (CoreException e)
			{
				IStatus err = new Status(e.getStatus().getSeverity(), PLUGIN_ID, e.getMessage(), e);
				log(err);
			}
		}
		this.extensions = Collections.unmodifiableMap(extensions);
	}
	
	public Map<String, IRunModeExtension> getExtensions(){
		return extensions;
	}
	
}
