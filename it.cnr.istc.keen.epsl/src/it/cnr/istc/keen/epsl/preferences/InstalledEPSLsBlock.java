/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Giulio Bernardi - adapted for EPSL
 *     
 * Stripped-down copy of org.eclipse.jdt.internal.debug.ui.jres.InstalledJREsBlock (Neon)
 *******************************************************************************/
package it.cnr.istc.keen.epsl.preferences;

import it.cnr.istc.keen.epsl.EpslInstallImpl;
import it.cnr.istc.keen.epsl.EpslRegistry;
import it.cnr.istc.keen.epsl.IEPSLInstall;
import it.cnr.istc.keen.epsl.utils.SWTFactory;
import it.cnr.istc.keen.epsl.wizards.AddEpslInstallWizard;
import it.cnr.istc.keen.epsl.wizards.EditEpslInstallWizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class InstalledEPSLsBlock implements ISelectionProvider {

	private Composite fControl;
	private List<IEPSLInstall> fPlanners = new ArrayList<IEPSLInstall>();
	private CheckboxTableViewer fPlannerList;

	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;
	private Button fCopyButton;
	private Button fSearchButton;

	private int fSortColumn = 0;

	private ListenerList<ISelectionChangedListener> fSelectionListeners = new ListenerList<>();
	private ISelection fPrevSelection = new StructuredSelection();

	private Table fTable;

	private static String fgLastUsedID;

	class EPSLContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object input) {
			return fPlanners.toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}

	class EPSLLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {
		Font bold = null;

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IEPSLInstall) {
				IEPSLInstall epsl = (IEPSLInstall) element;
				switch (columnIndex) {
				case 0:
					if (fPlannerList.getChecked(element))
						return String.format("%s (default)", epsl.getName());
					return epsl.getName();
				case 1:
					return epsl.getInstallLocation().getAbsolutePath();
				}
			}
			return element.toString();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return null;
			}
			return null;
		}

		@Override
		public Font getFont(Object element) {
			if (fPlannerList.getChecked(element)) {
				if (bold == null) {
					Font dialogFont = JFaceResources.getDialogFont();
					FontData[] fontData = dialogFont.getFontData();
					for (int i = 0; i < fontData.length; i++) {
						FontData data = fontData[i];
						data.setStyle(SWT.BOLD);
					}
					Display display = getDisplay();
					bold = new Font(display, fontData);
				}
				return bold;
			}
			return null;
		}

		@Override
		public void dispose() {
			if (bold != null) {
				bold.dispose();
			}
			super.dispose();
		}
	}

	private Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}
	
	@Override
	public ISelection getSelection() {
		return new StructuredSelection(fPlannerList.getCheckedElements());
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(fPrevSelection)) {
				fPrevSelection = selection;
				Object epsl = ((IStructuredSelection)selection).getFirstElement();
				if (epsl == null) {
					fPlannerList.setCheckedElements(new Object[0]);
				} else {
					fPlannerList.setCheckedElements(new Object[] { epsl });
					fPlannerList.reveal(epsl);
				}
				fPlannerList.refresh(true);
				fireSelectionChanged();
			}
		}
	}

	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite parent = SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
		fControl = parent;

		SWTFactory.createLabel(parent, "Installed &EPSL planners:", 2);

		fTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		fTable.setLayoutData(gd);
		fTable.setFont(font);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		TableColumn column = new TableColumn(fTable, SWT.NULL);
		column.setText("Name");
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByName();
				fPlannerList.refresh(true);
			}
		});
		int defaultwidth = 350/2 +1;
		column.setWidth(defaultwidth);

		column = new TableColumn(fTable, SWT.NULL);
		column.setText("Location");
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByLocation();
				fPlannerList.refresh(true);
			}
		});
		column.setWidth(defaultwidth);

		fPlannerList = new CheckboxTableViewer(fTable);
		fPlannerList.setLabelProvider(new EPSLLabelProvider());
		fPlannerList.setContentProvider(new EPSLContentProvider());
		fPlannerList.setUseHashlookup(true);
		// by default, sort by name
		sortByName();

		fPlannerList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});

		fPlannerList.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setCheckedItem((IEPSLInstall) event.getElement());
				} else {
					setCheckedItem(null);
				}
			}
		});

		fPlannerList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent e) {
				if (!fPlannerList.getSelection().isEmpty()) {
					editInstall();
				}
			}
		});
		fTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (fRemoveButton.isEnabled()) {
						removeInstalls();
					}
				}
			}
		});

		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);

		fAddButton = SWTFactory.createPushButton(buttons, "&Add...", null);
		fAddButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				addInstall();
			}
		});

		fEditButton = SWTFactory.createPushButton(buttons, "&Edit...", null);
		fEditButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				editInstall();
			}
		});

		fCopyButton = SWTFactory.createPushButton(buttons, "Dupli&cate...", null);
		fCopyButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				copyInstall();
			}
		});

		fRemoveButton = SWTFactory.createPushButton(buttons, "&Remove", null);
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				removeInstalls();
			}
		});

		SWTFactory.createVerticalSpacer(parent, 1);

		fSearchButton = SWTFactory.createPushButton(buttons, "&Search...", null);
		fSearchButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				search();
			}
		});

		fillWithWorkspacePlanners();
		enableButtons();
		fAddButton.setEnabled(true);
	}

	protected void copyInstall() {
		IStructuredSelection selection = (IStructuredSelection) fPlannerList.getSelection();
		@SuppressWarnings("unchecked")
		Iterator<IEPSLInstall> it = selection.iterator();

		ArrayList<EpslInstallImpl> newEntries = new ArrayList<EpslInstallImpl>();
		while (it.hasNext()) {
			IEPSLInstall selectedPlanner = it.next();
			EpslInstallImpl epsl = new EpslInstallImpl(selectedPlanner, createUniqueId());
			epsl.setName(generateName(selectedPlanner.getName()));
			EditEpslInstallWizard wizard = new EditEpslInstallWizard(epsl,
					fPlanners.toArray(new IEPSLInstall[fPlanners.size()]));
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			int dialogResult = dialog.open();
			if (dialogResult == Window.OK) {
				EpslInstallImpl result = wizard.getResult();
				if (result != null) {
					newEntries.add(result);
				}
			} else if (dialogResult == Window.CANCEL){
				break;
			}
		}
		if (newEntries.size() > 0) {
			fPlanners.addAll(newEntries);
			fPlannerList.refresh();
			fPlannerList.setSelection(new StructuredSelection(newEntries.toArray()));
		} else {
			fPlannerList.setSelection(selection);
		}
		fPlannerList.refresh(true);
	}

	public String generateName(String name) {
		if (!isDuplicateName(name)) {
			return name;
		}
		if (name.matches(".*\\(\\d*\\)")) { //$NON-NLS-1$
			int start = name.lastIndexOf('(');
			int end = name.lastIndexOf(')');
			String stringInt = name.substring(start + 1, end);
			int numericValue = Integer.parseInt(stringInt);
			String newName = name.substring(0, start + 1) + (numericValue + 1) + ")"; //$NON-NLS-1$
			return generateName(newName);
		}
		return generateName(name + " (1)"); //$NON-NLS-1$
	}

	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener listener : fSelectionListeners) {
			listener.selectionChanged(event);
		}
	}

	private void sortByName() {
		fPlannerList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IEPSLInstall) && (e2 instanceof IEPSLInstall)) {
					IEPSLInstall left = (IEPSLInstall) e1;
					IEPSLInstall right = (IEPSLInstall) e2;
					return left.getName().compareToIgnoreCase(right.getName());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 1;
	}

	@SuppressWarnings("unchecked")
	private boolean selectedPlannersEditable() {
		IStructuredSelection selection = (IStructuredSelection) fPlannerList.getSelection();
		for (Iterator<Object> it = selection.iterator(); it.hasNext();) {
			Object o = it.next();
			if (!(o instanceof EpslInstallImpl))
				return false;
		}
		return true;
	}

	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) fPlannerList.getSelection();
		int selectionCount = selection.size();
		boolean editable = selectedPlannersEditable();
		fEditButton.setEnabled(selectionCount == 1 && editable);
		fCopyButton.setEnabled(selectionCount > 0);
		if (selectionCount > 0 && selectionCount <= fPlannerList.getTable().getItemCount() && editable) {
			fRemoveButton.setEnabled(true);
		} else {
			fRemoveButton.setEnabled(false);
		}
	}

	private void sortByLocation() {
		fPlannerList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IEPSLInstall) && (e2 instanceof IEPSLInstall)) {
					IEPSLInstall left = (IEPSLInstall) e1;
					IEPSLInstall right = (IEPSLInstall) e2;
					return left.getInstallLocation().getAbsolutePath()
							.compareToIgnoreCase(right.getInstallLocation().getAbsolutePath());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 2;
	}

	public Control getControl() {
		return fControl;
	}

	protected void setPlanners(IEPSLInstall[] planners) {
		fPlanners.clear();
		for (int i = 0; i < planners.length; i++)
			fPlanners.add(planners[i]);
		fPlannerList.setInput(fPlanners);
		fPlannerList.refresh();
	}

	public IEPSLInstall[] getPlanners() {
		return fPlanners.toArray(new IEPSLInstall[fPlanners.size()]);
	}

	private void addInstall() {
		AddEpslInstallWizard wizard = new AddEpslInstallWizard(fPlanners.toArray(new IEPSLInstall[fPlanners.size()]));
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		if (dialog.open() == Window.OK) {
			EpslInstallImpl result = wizard.getResult();
			if (result != null) {
				boolean makeselection = fPlanners.size() < 1;
				fPlanners.add(result);
				fPlannerList.refresh();
				fPlannerList.setSelection(new StructuredSelection(result));
				fPlannerList.refresh(true);
				if (makeselection)
					setCheckedItem(result);
			}
		}
	}

	public void plannerAdded(IEPSLInstall planner) {
		boolean makeselection = fPlanners.size() < 1;
		fPlanners.add(planner);
		fPlannerList.refresh();
		fPlannerList.refresh(true);
		if (makeselection)
			setCheckedItem(planner);
	}

	public boolean isDuplicateName(String name) {
		for (int i = 0; i < fPlanners.size(); i++) {
			IEPSLInstall epsl = fPlanners.get(i);
			if (epsl.getName().equals(name))
				return true;
		}
		return false;
	}

	private void editInstall() {
		IStructuredSelection selection = (IStructuredSelection) fPlannerList.getSelection();
		EpslInstallImpl epsl = (EpslInstallImpl) selection.getFirstElement();
		if (epsl == null)
			return;

		EditEpslInstallWizard wizard = new EditEpslInstallWizard(epsl,
				fPlanners.toArray(new IEPSLInstall[fPlanners.size()]));
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		if (dialog.open() == Window.OK) {
			EpslInstallImpl result = wizard.getResult();
			if (result != null) {
				int index = fPlanners.indexOf(epsl);
				fPlanners.remove(index);
				fPlanners.add(index, result);
				fPlannerList.setSelection(new StructuredSelection(result));
				fPlannerList.refresh(true);
			}
		}
	}

	private void removeInstalls() {
		IStructuredSelection selection = (IStructuredSelection) fPlannerList.getSelection();
		IEPSLInstall[] entries = new IEPSLInstall[selection.size()];
		@SuppressWarnings("unchecked")
		Iterator<IEPSLInstall> iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			entries[i] = iter.next();
			i++;
		}
		removePlanners(entries);
	}

	public void removePlanners(IEPSLInstall[] entries) {
		for (int i = 0; i < entries.length; i++) {
			fPlanners.remove(entries[i]);
		}
		fPlannerList.refresh();
		IStructuredSelection curr = (IStructuredSelection) getSelection();
		IEPSLInstall[] installs = getPlanners();
		if (installs.length < 1) {
			fPrevSelection = null;
		}
		if (curr.size() == 0 && installs.length == 1) {
			setSelection(new StructuredSelection(installs[0]));
		} else {
			fireSelectionChanged();
		}
		fPlannerList.refresh(true);
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	private String createUniqueId() {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (EpslRegistry.findById(id) != null || id.equals(fgLastUsedID));
		fgLastUsedID = id;
		return id;
	}

	private void search() {
		// choose a root directory for the search
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Select a directory to search in:");
		dialog.setText("Directory Selection");
		String path = dialog.open();
		if (path == null)
			return;

		// ignore installed locations
		final Set<File> exstingLocations = new HashSet<File>();
		for (IEPSLInstall p : fPlanners) {
			exstingLocations.add(p.getInstallLocation());
		}

		// search
		final File rootDir = new File(path);
		final List<File> locations = new ArrayList<File>();

		IRunnableWithProgress r = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("=Searching...", IProgressMonitor.UNKNOWN);
				search(rootDir, locations, exstingLocations, monitor);
				monitor.done();
			}
		};

		try {
			ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell()) {
				@Override
				protected void createCancelButton(Composite parent) {
					cancel = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.STOP_LABEL, true);
					if (arrowCursor == null)
						arrowCursor = new Cursor(cancel.getDisplay(), SWT.CURSOR_ARROW);
					cancel.setCursor(arrowCursor);
					setOperationCancelButtonEnabled(enableCancelButton);
				}
			};
			progress.run(true, true, r);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
			// canceled
			return;
		}

		if (locations.isEmpty()) {
			String messagePath = path.replaceAll("&", "&&");
			MessageDialog.openInformation(getShell(), "Information", "No EPSL installation found in " + messagePath);
		} else {
			for(File location: locations) {
				EpslInstallImpl epsl = new EpslInstallImpl(createUniqueId());
				String name = location.getName();
				String nameCopy = new String(name);
				int i = 1;
				while (isDuplicateName(nameCopy)) {
					nameCopy = name + '(' + i++ + ')';
				}
				epsl.setName(nameCopy);
				epsl.setInstallLocation(location);
				plannerAdded(epsl);
			}
		}
	}

	private boolean validatePlannerLocation(File directory) {
		File f = new File(directory.getPath(), EpslRegistry.EPSL_JAR_NAME);
		return (f.exists() && f.isFile());
	}

	protected void search(File directory, List<File> found, Set<File> ignore, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		String[] names = directory.list();
		if (names == null) {
			return;
		}
		List<File> subDirs = new ArrayList<File>();
		for (int i = 0; i < names.length; i++) {
			if (monitor.isCanceled()) {
				return;
			}
			File file = new File(directory, names[i]);
			try {
				monitor.subTask(String.format("Found %d - Searching %s", found.size(),
						file.getCanonicalPath().replaceAll("&", "&&")));
			} catch (IOException e) {
			}
			if (file.isDirectory() && !ignore.contains(file)) {
				boolean validLocation = false;

				if (validatePlannerLocation(file)) {
					found.add(file);
					validLocation = true;
				}
				if (!validLocation)
					subDirs.add(file);
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = subDirs.remove(0);
			search(subDir, found, ignore, monitor);
			if (monitor.isCanceled())
				return;
		}

	}

	public void setCheckedItem(IEPSLInstall item) {
		if (item == null) {
			setSelection(new StructuredSelection());
		} else {
			setSelection(new StructuredSelection(item));
		}
	}

	public IEPSLInstall getCheckedItem() {
		Object[] objects = fPlannerList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (IEPSLInstall) objects[0];
	}

	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
		int columnCount = fTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			settings.put(qualifier + ".columnWidth" + i, fTable.getColumn(i).getWidth());	 //$NON-NLS-1$
		}
		settings.put(qualifier + ".sortColumn", fSortColumn); //$NON-NLS-1$
	}

	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
		fPlannerList.getTable().layout(true);
		restoreColumnWidths(settings, qualifier);
		try {
			fSortColumn = settings.getInt(qualifier + ".sortColumn"); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			fSortColumn = 1;
		}
		switch (fSortColumn) {
		case 1:
			sortByName();
			break;
		case 2:
			sortByLocation();
			break;
		}
	}

	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
		int columnCount = fTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			int width = -1;
			try {
				width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
			} catch (NumberFormatException e) {}

			if ((width <= 0) || (i == fTable.getColumnCount() - 1)) {
				fTable.getColumn(i).pack();
			} else {
				fTable.getColumn(i).setWidth(width);
			}
		}
	}

	protected void fillWithWorkspacePlanners() {
		setPlanners(EpslRegistry.getInstalledPlanners());
	}
}
