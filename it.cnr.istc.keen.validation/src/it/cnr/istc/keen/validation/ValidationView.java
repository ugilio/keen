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
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import it.cnr.istc.keen.validation.ValidationResultElement.ResultValue;
import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;


public class ValidationView extends ViewPart {

	public static final String ID = "it.cnr.istc.keen.validation.ValidationView";

	private TreeViewer viewer;
	private ValidationResult input;
	private ValidationTopPanel topBar;
	private ValidationProgressBar progressBar;

	class ContentProvider implements IStructuredContentProvider, ITreeContentProvider
	{
		private ValidationResult data;

		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
			data = (ValidationResult)newInput;
		}
		
		public void dispose()
		{
			data = null;
		}
		
		public Object[] getElements(Object parent)
		{
			return getChildren(parent);
		}
		
		public Object getParent(Object child)
		{
			if (child == data)
				return null;
			if (child instanceof List<?>)
				return data;
			if ((child instanceof ValidationResultElement) && (data != null))
				return data.getList();
			if (child instanceof ValidationResultElement.ResultValue)
			{
				return ((ValidationResultElement.ResultValue)child).getParent();
			}
			return null;
		}
		
		public Object [] getChildren(Object parent)
		{
			if (data == null)
				return new Object[0];
			if (parent == data)
				return new Object[] {data.getList()};
			if (parent == data.getList())
				return data.getList().toArray();
			if (parent instanceof ValidationResultElement)
				return ((ValidationResultElement)parent).getValues();
			return new Object[0];
		}
		
		public boolean hasChildren(Object parent)
		{
			return (parent == data) ||
					((data != null) && (data.getList() == parent)) ||
					(parent instanceof ValidationResultElement); 
		}
	}
	
	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider
	{
		private Image domOkImage;
		private Image domMaybeImage;		
		private Image domFailImage;
		private Image domErrImage;
		private Image compOkImage;
		private Image compMaybeImage;
		private Image compFailImage;
		private Image compErrImage;
		private Image valueOkImage;
		private Image valueMaybeImage;
		private Image valueFailImage;
		private Image valueErrImage;
		
		public LabelProvider()
		{
			super();
			initImages();
		}
		
		private Image createImage(String fileName)
		{
			URL url = Activator.getDefault().getBundle().getEntry(fileName);
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			return desc.createImage();
		}
		
		private void initImages()
		{
			domOkImage = createImage("icons/tsuiteok.gif");
			domMaybeImage = createImage("icons/tsuitemaybe.png");
			domFailImage = createImage("icons/tsuitefail.gif");
			domErrImage = createImage("icons/tsuiteerror.gif");
			compOkImage = createImage("icons/testok.gif");
			compMaybeImage = createImage("icons/testmaybe.png");
			compFailImage = createImage("icons/testfail.gif");
			compErrImage = createImage("icons/testerr.gif");
			valueOkImage = createImage("icons/validate_ok.png");
			valueMaybeImage = createImage("icons/validate_maybe.png");
			valueFailImage = createImage("icons/validate_fail.png");
			valueErrImage = createImage("icons/validate_err.png");
		}
		
		@Override
		public void dispose()
		{
			domOkImage.dispose();
			domMaybeImage.dispose();
			domFailImage.dispose();
			domErrImage.dispose();
			compOkImage.dispose();
			compMaybeImage.dispose();
			compFailImage.dispose();
			compErrImage.dispose();
			valueOkImage.dispose();
			valueMaybeImage.dispose();
			valueFailImage.dispose();
			valueErrImage.dispose();
			super.dispose();
		}
		
		private String formatList(List<ValidationResultElement> list)
		{
			String name = "Results";
			if (!list.isEmpty())
				name = list.get(0).getDomainName();
			int ok = 0;
			for (ValidationResultElement e : list)
				if (e.getStatus()==StatusType.OK)
					ok++;
			double duration = list.stream().flatMap(v -> Arrays.stream(v.getValues())).collect(Collectors.summingLong(e -> e.duration))/1000d;
			return String.format("%s (%d/%d) (%.3fs)",name,ok,list.size(),duration);
		}
		
		private String formatComponent(ValidationResultElement element)
		{
			String name = element.getComponentName();
			int ok = 0;
			ResultValue[] result = element.getValues();
			for (int i = 0; i < result.length; i++)
				if (result[i].status==StatusType.OK)
					ok++;
			double duration = Arrays.stream(result).collect(Collectors.summingLong(e -> e.duration)) /1000d; 
			return String.format("%s (%d/%d) (%.3fs)", name,ok,result.length,duration);
		}
		
		private String formatValue(ValidationResultElement.ResultValue value)
		{
			String result = value.status.name();
			double secs = value.duration/1000d;
			return String.format("[%s] %s (%.3fs)",result,value.value,secs);
		}
		
		@SuppressWarnings("unchecked")
		public String getText(Object obj)
		{
			if (obj instanceof List<?>)
				return formatList((List<ValidationResultElement>)obj);
			if (obj instanceof ValidationResultElement)
				return formatComponent((ValidationResultElement)obj);
			if (obj instanceof ValidationResultElement.ResultValue)
				return formatValue((ValidationResultElement.ResultValue)obj);
			return obj.toString();
		}
		
		private Image getIconNameForList(List<ValidationResultElement> list)
		{
			StatusType result = StatusType.OK;
			for (ValidationResultElement e : list)
			{
				StatusType st = e.getStatus();
				if (st.compareTo(result)>0)
					result = st;
			}
			switch (result)
			{
				case OK:
					return domOkImage;
				case MAYBE:
					return domMaybeImage;
				case FAILED:
					return domFailImage;
				case ERR:
					return domErrImage;
				default:
					return domErrImage;
			}
		}
		
		private Image getIconNameForTimeline(ValidationResultElement element)
		{
			switch (element.getStatus())
			{
				case OK:
					return compOkImage;
				case MAYBE:
					return compMaybeImage;
				case FAILED:
					return compFailImage;
				case ERR:
					return compErrImage;
				default:
					return compErrImage;
			}
		}
		
		private Image getIconNameForValue(ValidationResultElement.ResultValue value)
		{
			switch (value.status)
			{
				case OK:
					return valueOkImage;
				case MAYBE:
					return valueMaybeImage;
				case FAILED:
					return valueFailImage;
				case ERR:
					return valueErrImage;
				default:
					return valueErrImage;
			}
		}
		
		@SuppressWarnings("unchecked")
		public Image getImage(Object obj)
		{
			Image img = null;
			if (obj instanceof List)
				img = getIconNameForList((List<ValidationResultElement>)obj);
			if (obj instanceof ValidationResultElement)
				img = getIconNameForTimeline((ValidationResultElement)obj);
			if (obj instanceof ValidationResultElement.ResultValue)
				img = getIconNameForValue((ValidationResultElement.ResultValue)obj);
			return img;
		}
	}
	
	public ValidationView()
	{
		super();
	}
	
	public void setInput(ValidationResult data)
	{
		int tot = 0;
		int ok = 0;
		int maybe = 0;
		int fail = 0;
		int err = 0;
		if (data!=null)
			for (ValidationResultElement e : data.getList())
			{
				StatusType st = e.getStatus();
				switch (st)
				{
					case OK:
						ok++; break;
					case MAYBE:
						maybe++; break;
					case FAILED:
						fail++; break;
					case ERR:
						err++; break;
					default:
						err++; break;
				}
				tot++;
			}
		input = data;
		if (viewer != null)
			viewer.setInput(data);
		if (topBar != null)
			topBar.updateInfo(ok, maybe, fail, err);
		if (progressBar!=null)
			progressBar.reset(ok!=tot, false, tot, tot);
	}
	
	private void createTopBar(Composite parent)
	{
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);
		layout.numColumns=2;

		topBar = new ValidationTopPanel(composite);
		topBar.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		progressBar = new ValidationProgressBar(composite);
		progressBar.reset(false, true, 0, 0);
		progressBar.setLayoutData(
				new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	}

	public void createPartControl(Composite parent)
	{
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		gridLayout.numColumns=1;
		parent.setLayout(gridLayout);
		
		createTopBar(parent);
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));		
		
		//drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setAutoExpandLevel(2);
		viewer.setInput(input);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
