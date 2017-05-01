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
package it.cnr.istc.keen.validation;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ValidationTopPanel extends Composite
{
	private Text statusText;
	private Text failCountText;
	private Image failIcon;
	private Text errCountText;
	private Image errIcon;

	public ValidationTopPanel(Composite parent)
	{
		super(parent, SWT.WRAP);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns= 8;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		setLayout(gridLayout);

		Label label= new Label(this, SWT.NONE);
		label.setText("Result: ");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		statusText= new Text(this, SWT.READ_ONLY);
		statusText.setText("not run");
		//statusText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		statusText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

		errIcon = loadImage("icons/error_ovr.gif");
		errCountText = createStatusText("Errors: ");
		
		failIcon = loadImage("icons/failed_ovr.gif");
		failCountText = createStatusText("Failures: ");
		
		//this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	}
	
	private Image loadImage(String iconStr)
	{
		Label icon = new Label(this, SWT.NONE);
		URL url = Activator.getDefault().getBundle().getEntry(iconStr);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		Image theImage = desc.createImage();
		icon.setImage(theImage);
		icon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		return theImage;
	}
	
	private Text createStatusText(String text)
	{
		Label label= new Label(this, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		Text theText = new Text(this, SWT.READ_ONLY);
		theText.setText("0");
		//failCountText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		theText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return theText;
	}
	

	@Override
	public void dispose()
	{
		failIcon.dispose();
		errIcon.dispose();
	}

	public void updateInfo(int okCount, int maybeCount, int failCount, int errCount)
	{
		int tot = okCount + maybeCount + failCount + errCount;
		String msg=String.format("%s (%d/%d)",okCount==tot ? "PASSED" : "FAILED",okCount,tot);
		statusText.setText(msg);
		failCountText.setText(""+failCount);
		errCountText.setText(""+errCount);
		statusText.redraw();
		failCountText.redraw();
		redraw();
	}
}
