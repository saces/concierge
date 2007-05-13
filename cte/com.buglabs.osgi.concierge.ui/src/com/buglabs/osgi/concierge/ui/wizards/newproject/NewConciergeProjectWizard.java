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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.buglabs.osgi.concierge.ui.Activator;
import com.buglabs.osgi.concierge.ui.info.ProjectInfo;
import com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class NewConciergeProjectWizard extends Wizard implements INewWizard {

	private MainConciergeProjectPage mainPage;
	private BundlePropertiesPage propertiesPage;
	private ProjectInfo pinfo;
	private IDialogSettings settings;

	public NewConciergeProjectWizard() {
		setWindowTitle("Concierge OSGi Project");
		pinfo = new ProjectInfo();
		
		settings = Activator.getDefault().getDialogSettings().getSection(this.getClass().getName());
		
		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(this.getClass().getName());
		}
	}

	/**
	 * Sets the dialog settings
	 * @param settings
	 */
	protected void setSettings(IDialogSettings settings) {
		this.settings = settings;
	}
	
	public void addPages() {
		mainPage = new MainConciergeProjectPage(pinfo);
		propertiesPage = new BundlePropertiesPage(pinfo, settings);
		
		addPage(mainPage);
		addPage(propertiesPage);
	}

	public boolean performFinish() {
		if(this.getContainer().getCurrentPage() == mainPage) {
			propertiesPage.setDefaults();
		}
		
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		
		try {
			getContainer().run(false, false, new CreateConciergeProject(pinfo));
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		propertiesPage.saveDefaults();
		
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

}
