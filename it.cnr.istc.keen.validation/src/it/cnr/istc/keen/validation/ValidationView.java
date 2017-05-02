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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import it.cnr.istc.keen.validation.ValidationResultElement.ResultValue;
import it.cnr.istc.keen.validation.ValidationResultElement.StatusType;


public class ValidationView extends ViewPart {

	public static final String ID = "it.cnr.istc.keen.validation.ValidationView";

	private TreeViewer viewer;
	private SashForm sash;
	private Text outputText;
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
		if (viewer != null) {
			viewer.setInput(data);
			Control c = err==0 ? viewer.getControl() : null;
			sash.setMaximizedControl(c);
		}
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
		
		createMainArea(parent);
	}
	
	private void createMainArea(Composite parent)
	{
		sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		createTreeView(sash);
		
		createErrorDetail(sash);
		
		sash.setWeights(new int[]{50,50});
		sash.setMaximizedControl(viewer.getControl());

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				//toggle output text visibility
				Control control = sash.getMaximizedControl();
				Control tree = viewer.getControl();
				if (control == tree)
					sash.setMaximizedControl(null);
				else
					sash.setMaximizedControl(tree);
			}
		});
		
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object o = ((IStructuredSelection)sel).getFirstElement();
					if ((o instanceof ResultValue))
					{
						ResultValue rv = (ResultValue)o;
						if (rv.status==StatusType.ERR) {
							outputText.setText(rv.output);
							return;
						}
					}
				}
				outputText.setText("");
			}
		});
	}
	
	private void createTreeView(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		//drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setAutoExpandLevel(2);
		viewer.setInput(input);
	}
	
	private void createErrorDetail(Composite parent)
	{
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		gridLayout.numColumns=1;
		c.setLayout(gridLayout);
		
		Label label = new Label(c, SWT.LEFT);
		label.setText("TIGA Output: ");
		outputText = new Text(c, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		outputText.setFont(JFaceResources.getTextFont());
		outputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		//The following is to auto-hide scrollbars
		//See http://stackoverflow.com/questions/8547428/how-to-implement-auto-hide-scrollbar-in-swt-text-component
		Listener scrollBarListener = new Listener () {
			  @Override
			  public void handleEvent(Event event) {
			    Text t = (Text)event.widget;
			    Rectangle r1 = t.getClientArea();
			    Rectangle r2 = t.computeTrim(r1.x, r1.y, r1.width, r1.height);
			    Point p = t.computeSize(SWT.DEFAULT,  SWT.DEFAULT,  true);
			    t.getHorizontalBar().setVisible(r2.width <= p.x);
			    t.getVerticalBar().setVisible(r2.height <= p.y);
			    if (event.type == SWT.Modify) {
			      t.getParent().layout(true);
			      t.showSelection();
			    }
			  }
			};
		outputText.addListener(SWT.Resize, scrollBarListener);
		outputText.addListener(SWT.Modify, scrollBarListener);		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
