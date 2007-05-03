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
package com.buglabs.osgi.concierge.ui.wizards.newproject;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
import com.buglabs.osgi.concierge.ui.Activator;
import com.buglabs.osgi.concierge.ui.info.ProjectInfo;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class BundlePropertiesPage extends WizardPage {

	public static final String PAGE_TITLE = "Bundle Properties";
	private ProjectInfo projInfo;
	private Text txtBundleSymbolicName;
	private Text txtBundleVersion;
	private Text txtActivator;
	private Text txtBundleVendor;

	public BundlePropertiesPage(ProjectInfo pinfo) {
		super(PAGE_TITLE, PAGE_TITLE, Activator.getDefault().getImageDescriptor(Activator.IMAGE_CG_LOGO_WIZARD));
		projInfo = pinfo;
	}
	
	public boolean isPageComplete() {
		
		return super.isPageComplete();
	}
	
	public void  setDefaults() {
		String pname = projInfo.getProjectName();
		if(!pname.equals("")) {
			projInfo.setSymbolicName(pname);
		}
		
		projInfo.setVersion("1.0.0");
		projInfo.setActivator(ProjectUtils.formatNameToPackage(projInfo.getProjectName()) + ".Activator");
		updateControls();
	}
	
	private void updateControls() {
		txtBundleSymbolicName.setText(projInfo.getSymbolicName());
		txtBundleVersion.setText(projInfo.getVersion());
		txtBundleVendor.setText(projInfo.getVendor());
		txtActivator.setText(projInfo.getActivator());
	}

	public void createControl(Composite parent) {
		GridData gdFillH = new GridData(GridData.FILL_HORIZONTAL);
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setLayout(new GridLayout());
		
		GridData gdBundleProps = new GridData(GridData.FILL_HORIZONTAL);
		Group bundlePropsComp = new Group(top, SWT.BORDER);
		bundlePropsComp.setText("Bundle Properties");
		bundlePropsComp.setLayoutData(gdBundleProps);
		bundlePropsComp.setLayout(new GridLayout(2, false));
		
		Label lblBundleSymbolicName = new Label(bundlePropsComp, SWT.NONE);
		lblBundleSymbolicName.setText("Symbolic Name:");
		txtBundleSymbolicName = new Text(bundlePropsComp, SWT.BORDER);
		txtBundleSymbolicName.setLayoutData(gdFillH);
		txtBundleSymbolicName.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				projInfo.setSymbolicName(((Text) e.widget).getText());
				setPageComplete(true);
			}});
		
		Label lblBundleVersion = new Label(bundlePropsComp, SWT.NONE);
		lblBundleVersion.setText("Version:");
		txtBundleVersion = new Text(bundlePropsComp, SWT.BORDER);
		txtBundleVersion.setLayoutData(gdFillH);
		txtBundleVersion.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				projInfo.setVersion(((Text) e.widget).getText());
				setPageComplete(true);
			}});
		
		
		Label lblBundleVendor = new Label(bundlePropsComp, SWT.NONE);
		lblBundleVendor.setText("Vendor:");
		txtBundleVendor = new Text(bundlePropsComp, SWT.BORDER);
		txtBundleVendor.setLayoutData(gdFillH);
		txtBundleVendor.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				projInfo.setVendor(((Text) e.widget).getText());
				setPageComplete(true);
			}});
		
		Label lblActivator = new Label(bundlePropsComp, SWT.NONE);
		lblActivator.setText("Activator:");
		txtActivator = new Text(bundlePropsComp, SWT.BORDER);
		txtActivator.setLayoutData(gdFillH);
		txtActivator.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				projInfo.setActivator(((Text) e.widget).getText());
				setPageComplete(true);
			}});
		
		
		//Spacer
		Label spacer = new Label(top, SWT.NONE);
		
		Button btnGenerateActivator = new Button(top, SWT.CHECK);
		btnGenerateActivator.setSelection(projInfo.isGenerateActivator());
		btnGenerateActivator.setText("Generate default activator implementation");
		btnGenerateActivator.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				projInfo.setGenerateActivator(((Button) e.widget).getSelection());
			}});
		
		setControl(top);
	}

	public void setVisible(boolean visible) {
		if(visible) {
			setDefaults();
		}
		super.setVisible(visible);
	}
}
