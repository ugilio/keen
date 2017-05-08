package it.cnr.istc.keen.verification;

import java.io.InputStream;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class VerifyDialogOK extends TitleAreaDialog
{
	public VerifyDialogOK(Shell parentShell)
	{
		super(parentShell);
	}
	
	@Override
	public void create()
	{
	    super.create();
	    setTitle("Plan Verification");
	    setMessage("The plan has been successfully verified",
	    		IMessageProvider.INFORMATION);
	    Shell shell = getShell();
	    int w = shell.getSize().x;
	    shell.setSize(shell.computeSize(w, SWT.DEFAULT));
	}

	private Image loadImage()
	{
	    try
	    {
	        InputStream str = getClass().getClassLoader().getResourceAsStream("icons/validate.png");
	        try
	        {
	            //return new Image(getParentShell().getDisplay(), "/home/giulio/ercp/it.cnr.istc.pst.keen/icons/validate.png");
	            return new Image(getParentShell().getDisplay(), str);
	        }
	        finally
	        {
	            str.close();
	        }
	    }
	    catch (Exception e)
	    {
	        return null;
	    }
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(parent.getFont());
		
		Label l = new Label(composite, SWT.NONE);
		l.setText("Details:");
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		
		l.setLayoutData(gridData);
		
		createDetailArea(composite);
		
		return composite;
		
	}	
	
	protected Control createDetailArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		//layout.marginHeight = 0;
		layout.marginWidth = 10;
		//layout.verticalSpacing = 0;
		//layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(parent.getFont());
		
		Label icon = new Label(composite, SWT.NONE);
		icon.setImage(loadImage());
		icon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		Label l2 = new Label(composite, SWT.LEFT);
		l2.setText("Dynamic controllability: ok");
		l2.setLayoutData(gridData);

		return composite;
	}

	  @Override
	  protected void createButtonsForButtonBar(Composite parent)
	  {
		  createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	  }
}

