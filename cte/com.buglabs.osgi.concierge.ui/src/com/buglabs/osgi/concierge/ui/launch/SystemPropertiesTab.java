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

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration;

/**
 * 
 * @author Ken Gilmer - ken@buglabs.net
 *
 */
public class SystemPropertiesTab extends AbstractLaunchConfigurationTab {

	private Map systemProps;

	private Button enableLogger;

	private Button errorLevel;

	private Button warningLevel;

	private Button infoLevel;

	private Button debugLevel;

	private Button strictMode;

	private Button logQuiet;

	private Text bundleLocation;

	private TableViewer propViewer;

	private Button bundleDirButton;

	private Button decompressEmbedded;

	private Button enableSecurity;

	private Button removePropButton;

	private Button editPropButton;

	private Text jvmArgs;

	protected String jvmArgStr;

	public SystemPropertiesTab() {
		// systemProps = new Hashtable();
	}

	//Workaround
	private void put(String key, String value) {
		Map tempMap = new Hashtable();
		tempMap.putAll(systemProps);
		tempMap.put(key, value);
		systemProps = tempMap;
		refreshDialog();
	}
	
//	Workaround
	private void remove(Object key) {
		Map tempMap = new Hashtable();
		tempMap.putAll(systemProps);
		tempMap.remove(key);
		systemProps = tempMap;
		refreshDialog();
	}
	
	protected void refreshDialog() {
		setDirty(true);
		updateLaunchConfigurationDialog();
	}
	
	public void createControl(final Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group logGroup = new Group(main, SWT.None);
		logGroup.setText("Logging");
		logGroup.setLayout(new GridLayout(2, false));

		Composite logComp = new Composite(logGroup, SWT.None);
		logComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		logComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		enableLogger = new Button(logComp, SWT.CHECK);
		enableLogger.setText("Enable System Logger");
		enableLogger.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Group levelGroup = new Group(logGroup, SWT.None);
		levelGroup.setText("Log Level");
		levelGroup.setLayout(new GridLayout());

		errorLevel = new Button(levelGroup, SWT.RADIO);
		errorLevel.setText("Error");
		errorLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put("ch.ethz.iks.concierge.log.level", "1");
			}
		});
		warningLevel = new Button(levelGroup, SWT.RADIO);
		warningLevel.setText("Warning");
		warningLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put("ch.ethz.iks.concierge.log.level", "2");
			}
		});

		infoLevel = new Button(levelGroup, SWT.RADIO);
		infoLevel.setText("Information");
		infoLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put("ch.ethz.iks.concierge.log.level", "3");
			}
		});

		debugLevel = new Button(levelGroup, SWT.RADIO);
		debugLevel.setText("Debug");
		debugLevel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				put("ch.ethz.iks.concierge.log.level", "4");
			}
		});

		logQuiet = new Button(logComp, SWT.CHECK);
		logQuiet.setText("Quiet Mode");
		logQuiet.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Group bundleGroup = new Group(main, SWT.None);
		bundleGroup.setText("General");
		bundleGroup.setLayout(new GridLayout(2, false));
		bundleGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		Label lblBundleLocation = new Label(bundleGroup, SWT.None);
		lblBundleLocation.setText("Non-workspace Bundle Location");
		GridData gdata = new GridData();
		gdata.horizontalSpan = 2;
		lblBundleLocation.setLayoutData(gdata);
		bundleLocation = new Text(bundleGroup, SWT.BORDER);
		bundleLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bundleLocation.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				put("ch.ethz.iks.concierge.jars", bundleLocation.getText());
			}

		});

		bundleDirButton = new Button(bundleGroup, SWT.None);
		bundleDirButton.setText("...");
		bundleDirButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell());
				String path = null;
				if ((path = fd.open()) != null) {
					put("ch.ethz.iks.concierge.jars", path);
					bundleLocation.setText(path);
				}
			}

		});

		Composite generalComp = new Composite(bundleGroup, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		generalComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		generalComp.setLayoutData(gdata);

		strictMode = new Button(generalComp, SWT.CHECK);
		strictMode.setText("Strict Mode");

		decompressEmbedded = new Button(generalComp, SWT.CHECK);
		decompressEmbedded.setText("Decompress Embedded Jars");

		enableSecurity = new Button(generalComp, SWT.CHECK);
		enableSecurity.setText("Enable Security");

		Composite propComp = new Composite(main, SWT.None);
		GridLayout layout = new GridLayout(2, false);
		propComp.setLayout(layout);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		propComp.setLayoutData(gdata);

		propViewer = new TableViewer(propComp, SWT.None | SWT.FULL_SELECTION | SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 90;
		propViewer.getTable().setLayoutData(gdata);
		propViewer.setContentProvider(new PropertyContentProvider());
		propViewer.setLabelProvider(new PropertyLabelProvider());
		propViewer.getTable().setLinesVisible(true);
		propViewer.setSorter(new ViewerSorter());
		propViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				removePropButton.setEnabled(propViewer.getSelection() != null);
				editPropButton.setEnabled(propViewer.getSelection() != null);
			}

		});

		propViewer.getTable().setHeaderVisible(true);
		TableColumn tc = new TableColumn(propViewer.getTable(), SWT.None);
		tc.setText("Property Name");
		tc.setWidth(200);
		tc = new TableColumn(propViewer.getTable(), SWT.None);
		tc.setText("Value");
		tc.setWidth(200);

		Composite propButtonComp = new Composite(propComp, SWT.None);
		propButtonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
		propButtonComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button newPropButton = new Button(propButtonComp, SWT.NONE);
		newPropButton.setText("New...");
		newPropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		newPropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				NewPropertyDialog dialog = new NewPropertyDialog(parent.getShell(), systemProps);

				if (dialog.open() == Dialog.OK) {
					put(dialog.getName(), dialog.getValue());
				}
			}

		});
		
		editPropButton = new Button(propButtonComp, SWT.NONE);
		editPropButton.setText("Edit...");
		editPropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		editPropButton.setEnabled(false);
		editPropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				NewPropertyDialog dialog = new NewPropertyDialog(parent.getShell(), systemProps);
				
				String name = (String) ((IStructuredSelection) propViewer.getSelection()).getFirstElement();
				String value = (String) systemProps.get(name);
				
				dialog.setName(name);
				dialog.setValue(value);
				
				if (dialog.open() == Dialog.OK) {
					remove(name);
					put(dialog.getName(), dialog.getValue());
				}
			}

		});

		removePropButton = new Button(propButtonComp, SWT.NONE);
		removePropButton.setText("Remove");
		removePropButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		removePropButton.setEnabled(false);
		removePropButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent event) {
				remove(((IStructuredSelection) propViewer.getSelection()).getFirstElement());
			}

		});
		
		Group jvmArgGroup = new Group(main, SWT.NONE);
		jvmArgGroup.setText("VM Arguments");
		jvmArgGroup.setLayout(new GridLayout());
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		jvmArgGroup.setLayoutData(gdata);
		
		jvmArgs = new Text(jvmArgGroup, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.heightHint = 60;
		jvmArgs.setLayoutData(gdata);
		jvmArgs.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				jvmArgStr = jvmArgs.getText();
				refreshDialog();
			}

		});
		
		setControl(main);
	}

	public String getName() {
		return "Concierge";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			systemProps = configuration.getAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, new Hashtable());
			propViewer.setInput(systemProps);
			enableLogger.addSelectionListener(new BooleanSelectionListener("ch.ethz.iks.concierge.log.enabled", enableLogger));
			logQuiet.addSelectionListener(new BooleanSelectionListener("ch.ethz.iks.concierge.log.quiet", logQuiet));
			strictMode.addSelectionListener(new BooleanSelectionListener("ch.ethz.iks.concierge.strictStartup", strictMode));
			decompressEmbedded.addSelectionListener(new BooleanSelectionListener("ch.ethz.iks.concierge.decompressEmbedded",
					decompressEmbedded));
			enableSecurity.addSelectionListener(new BooleanSelectionListener("ch.ethz.iks.concierge.security.enabled", enableSecurity));

			String ll = (String) systemProps.get("ch.ethz.iks.concierge.log.level");
			
			jvmArgStr = configuration.getAttribute(ConciergeLaunchConfiguration.JVM_ARGUMENTS, "");

			if (ll != null) {
				switch (Integer.parseInt(ll)) {
				case 1:
					errorLevel.setSelection(true);
					break;
				case 2:
					warningLevel.setSelection(true);
					break;
				case 3:
					infoLevel.setSelection(true);
					break;
				case 4:
					debugLevel.setSelection(true);
					break;
				}
			}

			String bundleDir = configuration.getAttribute("ch.ethz.iks.concierge.jars", "");
			if (bundleDir != null && bundleDir.length() > 0) {
				bundleLocation.setText(bundleDir);
			}
			
			String args = configuration.getAttribute(ConciergeLaunchConfiguration.JVM_ARGUMENTS, "");
			if (args != null) {
				jvmArgs.setText(args);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, systemProps);
		configuration.setAttribute(ConciergeLaunchConfiguration.JVM_ARGUMENTS, jvmArgStr);
		propViewer.setInput(systemProps);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
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

	private class BooleanSelectionListener implements SelectionListener {

		private final String prop;

		public BooleanSelectionListener(String prop, Button control) {
			this.prop = prop;

			String val = (String) systemProps.get(prop);

			if (val != null && val.length() > 0) {
				boolean v = Boolean.getBoolean(val);
				control.setSelection(v);
			} else {
				control.setSelection(false);
			}
		}

		public void widgetDefaultSelected(SelectionEvent arg0) {
		}

		public void widgetSelected(SelectionEvent arg0) {
			Button b = (Button) arg0.getSource();
			update(b.getSelection());
			refreshDialog();
		}

		private void update(boolean value) {
			put(prop, Boolean.toString(value));
		}

	}

	private class PropertyContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			Map props = (Map) inputElement;
			return props.keySet().toArray(new String[props.size()]);
		}

		public void dispose() {			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class PropertyLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String key = (String) element;

			String val = (String) SystemPropertiesTab.this.systemProps.get(key);

			switch (columnIndex) {
			case 0:
				return key;
			case 1:
				return val;
			}

			return null;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
}
