package it.cnr.istc.keen.siriusglue.synchronizer;

import com.google.inject.Inject;

public class Controller {
	
	private DiagramSaveListener diagramSaveListener;
	private ResourceSaveListener resourceSaveListener;
	
	@Inject
	public Controller(DiagramSaveListener diagramSaveListener, ResourceSaveListener resourceSaveListener) {
		this.diagramSaveListener = diagramSaveListener;
		this.resourceSaveListener = resourceSaveListener;
	}

	public void disableAllSaveNotifications() {
		resourceSaveListener.disableResourceChangeListener();
		diagramSaveListener.disableSiriusNotifications();
	}
	
	public void enableAllSaveNotifications() {
		resourceSaveListener.enableResourceChangeListener();
		diagramSaveListener.enableSiriusNotifications();
	}
}
