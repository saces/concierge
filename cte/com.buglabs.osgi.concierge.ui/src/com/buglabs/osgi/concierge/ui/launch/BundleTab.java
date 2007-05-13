/* Copyright (c) 2007 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.buglabs.osgi.concierge.ui.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration;
import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;

/**
 * 
 * @author Ken Gilmer - ken@buglabs.net
 *
 */
public class BundleTab extends AbstractLaunchConfigurationTab {

	private List launchBundles;

	private List allBundles;

	private CheckboxTableViewer viewer;
	private Button selectAll;
	private Button selectNone;
	private Button initializeButton = null;
	
	private Button continueButton;

	private Group bundleGroup;

	private List installMap;

	private Group cgBundleGroup;

	private CheckboxTableViewer cgViewer;

	private Button cgSelectAll;

	private Button cgSelectNone;

	private List coreBundles;

	private List cgLaunchBundles;

	private List cgInstallMap;

	public BundleTab() {
	}

	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout());
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		continueButton = new Button(main, SWT.RADIO);
		continueButton.setText("Continue with pre-existing state");
		
		initializeButton = new Button(main, SWT.RADIO);
		initializeButton.setText("Initialize Runtime (Clear any previous state)");
		
		Composite spaceComp = new Composite(main, SWT.None);
		spaceComp.setLayout(new GridLayout());
		spaceComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(spaceComp, SWT.None).setText("  ");
		
		cgBundleGroup = new Group(spaceComp, SWT.None);
		cgBundleGroup.setText("Concierge Bundles");
		cgBundleGroup.setLayout(new GridLayout());
		cgBundleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite cgViewerComp = new Composite(cgBundleGroup, SWT.None);
		cgViewerComp.setLayout(StripGridLayoutMargins(new GridLayout(2, false)));
		cgViewerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite cgViewerBoxComp = new Composite(cgViewerComp, SWT.NONE);
		cgViewerBoxComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 120;
		cgViewerBoxComp.setLayoutData(gdata);
		cgViewer = CheckboxTableViewer.newCheckList(cgViewerBoxComp, SWT.FULL_SELECTION | SWT.BORDER);
		cgViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		cgViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
					cgLaunchBundles = Arrays.asList(cgViewer.getCheckedElements());
				refreshDialog();
			}
		});
		cgViewer.getTable().setHeaderVisible(true);
		cgViewer.getTable().setLinesVisible(true);
		addTableColumns(cgViewer);
		cgViewer.setContentProvider(new ConciergeCoreBundleProvider());
		cgViewer.setLabelProvider(new ConciergeCoreBundleLabelProvider());
		cgViewer.addDoubleClickListener(new ChangeCoreBundleStartStateListener());
		cgViewer.setSorter(new ViewerSorter());
		
		new Label(cgViewerBoxComp, SWT.None).setText("Double-click bundle to change initial state.");
		new Label(cgViewerBoxComp, SWT.NONE).setText("These bundles are configurable in the Concierge preference page.");
		
		Composite cgButtonComp = new Composite(cgViewerComp, SWT.None);
		cgButtonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		cgButtonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		cgSelectAll = new Button(cgButtonComp, SWT.None);
		cgSelectAll.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		cgSelectAll.setText("Select All");
		cgSelectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				cgViewer.setAllChecked(true);
				cgLaunchBundles = coreBundles;
				refreshDialog();
			}

		});

		
		cgSelectNone = new Button(cgButtonComp, SWT.None);
		cgSelectNone.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		cgSelectNone.setText("Deselect All");
		cgSelectNone.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				cgViewer.setAllChecked(false);
				cgLaunchBundles = new ArrayList(0);
				refreshDialog();
			}

		});
		
		
		// ----------------------------
		
		bundleGroup = new Group(spaceComp, SWT.None);
		bundleGroup.setText("Workspace Bundles");
		bundleGroup.setLayout(new GridLayout());
		bundleGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		
		Composite viewerComp = new Composite(bundleGroup, SWT.None);
		viewerComp.setLayout(StripGridLayoutMargins(new GridLayout(2, false)));
		viewerComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite viewerBoxComp = new Composite(viewerComp, SWT.NONE);
		viewerBoxComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		viewerBoxComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer = CheckboxTableViewer.newCheckList(viewerBoxComp, SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
					launchBundles = Arrays.asList(viewer.getCheckedElements());
				refreshDialog();
			}
		});
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		addTableColumns(viewer);
		viewer.setContentProvider(new ConciergeProjectProvider());
		viewer.setLabelProvider(new ConciergeProjectLabelProvider());
		viewer.addDoubleClickListener(new ChangeBundleStartStateListener());
		viewer.setSorter(new ViewerSorter());
		
		new Label(viewerBoxComp, SWT.None).setText("Double-click bundle to change initial state.");
		
		Composite buttonComp = new Composite(viewerComp, SWT.None);
		buttonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		selectAll = new Button(buttonComp, SWT.None);
		selectAll.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		selectAll.setText("Select All");
		selectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				viewer.setAllChecked(true);
				launchBundles = allBundles;
				refreshDialog();
			}

		});

		
		selectNone = new Button(buttonComp, SWT.None);
		selectNone.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		selectNone.setText("Deselect All");
		selectNone.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				viewer.setAllChecked(false);
				launchBundles = new ArrayList(0);
				refreshDialog();
			}

		});
		
		initializeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {		
				setViewerEnabled(initializeButton.getSelection());
				refreshDialog();
			}
		});

		setControl(main);
	}
	
	protected void refreshDialog() {
		setDirty(true);
		updateLaunchConfigurationDialog();
	}

	private void addTableColumns(CheckboxTableViewer viewer2) {
		Table t = viewer2.getTable();
		TableColumn tc = new TableColumn(t, SWT.None);
		tc.setWidth(240);
		tc.setText("Name");
		
		tc = new TableColumn(t, SWT.None);
		tc.setWidth(70);
		tc.setText("Initial State");
	}

	private void setViewerEnabled(boolean state) {
		selectAll.setEnabled(state);
		selectNone.setEnabled(state);
		viewer.getControl().setEnabled(state);
		viewer.setAllGrayed(!state);
		bundleGroup.setEnabled(state);
		
		cgSelectAll.setEnabled(state);
		cgSelectNone.setEnabled(state);
		cgViewer.getControl().setEnabled(state);
		cgViewer.setAllGrayed(!state);
		cgBundleGroup.setEnabled(state);

	}

	public String getName() {
		return "Bundles";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			allBundles = getWSCGProjectNames();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			allBundles = new Vector();
		}
		

		viewer.setInput(allBundles);
		try {
			
			launchBundles = configuration.getAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, new ArrayList());
			installMap = configuration.getAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, new ArrayList());
			viewer.setCheckedElements(launchBundles.toArray());

			if (configuration.getAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, true)) {
				initializeButton.setSelection(true);
			} else {
				continueButton.setSelection(true);
			}
			
			setViewerEnabled(initializeButton.getSelection());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		viewer.refresh();
		
		List cgFiles = ConciergeRuntime.getDefault().getConciergeJars();
		
		File frameworkJar = null;
		for (Iterator i = cgFiles.iterator(); i.hasNext();) {
			File f = (File) i.next();
			if (f.getName().equals("cg_framework.jar")) {
				frameworkJar = f;
			}
		}
		
		cgFiles.remove(frameworkJar);
		
		coreBundles = filesToStrings(cgFiles);
		cgViewer.setInput(coreBundles);
		try {
			cgLaunchBundles = configuration.getAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, new ArrayList());
			cgInstallMap = configuration.getAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, new ArrayList());
			cgViewer.setCheckedElements(cgLaunchBundles.toArray());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, launchBundles);
		configuration.setAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, installMap);
		
		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, cgLaunchBundles);
		configuration.setAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, cgInstallMap);
		
		configuration.setAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, initializeButton.getSelection());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, new ArrayList(0));
		configuration.setAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, new ArrayList());
		configuration.setAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, true);
		
		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, new ArrayList(0));
		configuration.setAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, new ArrayList());
	}

	private class ConciergeProjectProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {

			return ((List) inputElement).toArray(new String[((List) inputElement).size()]);
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	private class ConciergeProjectLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return (String) element;
			case 1:
				if (installMap == null || !installMap.contains(element)) {
					return "Start";
				} else {
					return "Install";
				}
			}
			
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

	}
	
	private class ConciergeCoreBundleProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((List)inputElement).toArray(new String[((List) inputElement).size()]);
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	private class ConciergeCoreBundleLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			File f = new File((String) element);
			switch(columnIndex) {
			case 0:
				return f.getName();
			case 1:
				if (cgInstallMap == null || !cgInstallMap.contains(element)) {
					return "Start";
				} else {
					return "Install";
				}
			}
			
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

	}
	
	private static List filesToStrings(List files) {
		List fileStrings = new ArrayList();
				
		for (Iterator i = files.iterator(); i.hasNext();) {
			fileStrings.add(((File)i.next()).getAbsolutePath());
		}
		
		return fileStrings;
	}
	
	private class ChangeBundleStartStateListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection s = (IStructuredSelection) event.getSelection();
			
			Object o = s.getFirstElement();
			//Workaround to trigger button updates
			ArrayList tmpList = new ArrayList();
				
			tmpList.addAll(installMap);
			
			installMap.clear();
			
			if (tmpList.contains(o)) {
				tmpList.remove(o);
			} else {
				tmpList.add(o);
			}

			installMap = tmpList;
			
			viewer.refresh();
			refreshDialog();
		}
		
	}
	
	private class ChangeCoreBundleStartStateListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection s = (IStructuredSelection) event.getSelection();
			
			Object o = s.getFirstElement();
			//Workaround to trigger button updates
			ArrayList tmpList = new ArrayList();
				
			tmpList.addAll(cgInstallMap);
			
			cgInstallMap.clear();
			
			if (tmpList.contains(o)) {
				tmpList.remove(o);
			} else {
				tmpList.add(o);
			}

			cgInstallMap = tmpList;
			
			cgViewer.refresh();
			refreshDialog();
		}
		
	}

	/**
	 * Builds a list of Concierge Projects
	 * 
	 * @return A list of projects in the workspace containing the Concierge
	 *         Project Nature.
	 * @throws CoreException
	 */
	public static List getWSCGProjectNames() throws CoreException {

		final List projects = new Vector();

		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		wsroot.accept(new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {

				if (resource.getType() == IResource.ROOT) {
					return true;
				} else if (resource.getType() == IResource.PROJECT) {
					IProject project = (IProject) resource;
					if (project.isOpen() && project.hasNature(ConciergeProjectNature.ID)) {
						projects.add(project.getName());
					}
				}

				return false;
			}
		});		

		return projects;
	}

	/**
	 * Removes margins from a GridLayout
	 * 
	 * @param layout
	 * @return
	 */
	private GridLayout StripGridLayoutMargins(GridLayout layout) {
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		return layout;
	}
}
