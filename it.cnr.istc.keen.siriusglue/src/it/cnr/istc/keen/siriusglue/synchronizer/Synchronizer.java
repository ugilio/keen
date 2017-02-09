package it.cnr.istc.keen.siriusglue.synchronizer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

import com.google.inject.Inject;

public class Synchronizer implements IStartup {

	public EditorListener editorListener;

	@Inject
	public Synchronizer(EditorListener editorListener) {
		this.editorListener = editorListener;
	}
	
	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				editorListener.install();
			}
		});
	}
}
