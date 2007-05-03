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
package com.buglabs.osgi.concierge.ui.wizards.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;

import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
import com.buglabs.osgi.concierge.ui.info.BundleExportInfo;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 * @author ken
 * 
 */
public class ProjectAndDestinationPage extends WizardPage {
	public static final String PAGE_NAME = "Export Concierge Bundle";

	public static final String PAGE_DESC = "Export Concierge Bundle";

	private CheckboxTableViewer viewer;

	private Combo txtLocation;

	private BundleExportInfo expinfo;

	private IDialogSettings dialogSettings;

	private String natureID;

	private static final String exportDirectoriesKey = "Directories";
	
	public ProjectAndDestinationPage(BundleExportInfo expinfo,
			IDialogSettings settings, String natureID) {
		super(PAGE_NAME, PAGE_DESC, null);
		this.expinfo = expinfo;
		this.dialogSettings = settings;
		this.natureID = natureID;
	}
	
	public boolean isPageComplete() {
		setErrorMessage(null);
		setMessage("Select a project or set of projects to export and the desired location.");

		if (expinfo.getProjects().size() == 0) {
			setErrorMessage("Please select a project to export.");
			return false;
		}

		if (expinfo.getLocation() == null) {
			setErrorMessage("Please set the export location.");
			return false;
		}

		return true;
	}

	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.BORDER);
		top.setLayout(new GridLayout());

		Label lblSelectApp = new Label(top, SWT.NONE);
		lblSelectApp.setText("Select the project to export");

		viewer = CheckboxTableViewer.newCheckList(top, SWT.FULL_SELECTION);
		GridData viewerData = new GridData(GridData.FILL_HORIZONTAL);
		viewerData.heightHint = 150;
		viewer.getControl().setLayoutData(viewerData);

		viewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				ArrayList children = new ArrayList();

				if (inputElement instanceof IWorkspaceRoot) {
					IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
					IProject[] projects = root.getProjects();
					for (int i = 0; i < projects.length; ++i) {
						IProject project = projects[i];
						try {
							// TODO: Find the nature ID static String
							if (project.hasNature(natureID)) {
								children.add(project);
							}
						} catch (CoreException e) {
							// Purposely do nothing
						}

					}
				}

				return children.toArray();
			}

			public void dispose() {
				// TODO Auto-generated method stub

			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub

			}
		});

		viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IProject) {
					return ((IProject) element).getName();
				}

				return "";
			}
		});

		viewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				Object[] elems = viewer.getCheckedElements();
				if (elems.length == 0) {
					expinfo.getProjects().clear();
				} else {
					expinfo.getProjects().clear();
					expinfo.getProjects().addAll(Arrays.asList(elems));
				}

				setPageComplete(true);
			}
		});

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

		setCheckState(viewer, expinfo);

		Composite compLocation = new Composite(top, SWT.NONE);
		GridData compLocationData = new GridData(GridData.FILL_HORIZONTAL);
		compLocation.setLayoutData(compLocationData);

		compLocation.setLayout(new GridLayout(3, false));
		Label lblLocation = new Label(compLocation, SWT.NONE);
		lblLocation.setText("Location:");

		txtLocation = new Combo(compLocation, SWT.BORDER);
		loadPreviousSelections(txtLocation);
		txtLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLocation.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				expinfo.setLocation(((Combo) e.widget).getText());
				setPageComplete(true);
			}
		});

		Button btnBrowse = new Button(compLocation, SWT.PUSH);
		btnBrowse.setText("Browse");

		btnBrowse.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(getContainer()
						.getShell());
				String dir = d.open();

				if (dir != null) {
					expinfo.setLocation(dir);
					txtLocation.setText(dir);
					setPageComplete(true);
				}
			}
		});

		setControl(top);
	}

	private void loadPreviousSelections(Combo txtLocation2) {
		String[] dirs = dialogSettings.getArray(exportDirectoriesKey);

		if (dirs != null) {
			for (int i = 0; i < dirs.length; ++i) {
				txtLocation2.add(dirs[i]);
			}
		}
	}

	/**
	 * Get selected projects from workspace and check them in the dialog.
	 * 
	 * @param viewer2
	 * @param expinfo2
	 */
	private void setCheckState(CheckboxTableViewer viewer2,
			BundleExportInfo expinfo2) {
		viewer2.setCheckedElements(expinfo2.getSelectedProjects().toArray(
				new IProject[expinfo2.getSelectedProjects().size()]));
		expinfo2.getProjects().addAll(expinfo2.getSelectedProjects());
	}

	public BundleExportInfo getBundleExportInfo() {
		return expinfo;
	}

	public void updateDialogStoredState() {
		List existing = new ArrayList( Arrays.asList(txtLocation.getItems()));
		
		if (!existing.contains(txtLocation.getText())) {
			existing.add(0, txtLocation.getText());
			dialogSettings.put(exportDirectoriesKey, (String[]) existing.toArray(new String[existing.size()]));
		}
	}
}
