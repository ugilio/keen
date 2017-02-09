package it.cnr.istc.keen.siriusglue;

import com.google.inject.AbstractModule;

import it.cnr.istc.keen.siriusglue.synchronizer.Synchronizer;
import it.cnr.istc.keen.siriusglue.synchronizer.SynchronizerFactory;

public class Module extends AbstractModule {

	@Override
	protected void configure() {
		bind(Synchronizer.class).toProvider(SynchronizerFactory.class);
	}

}
