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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
import com.buglabs.osgi.concierge.ui.Activator;
import com.buglabs.osgi.concierge.ui.info.BundleExportInfo;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 * @author kgilmer
 * 
 */
public class ExportBundlesWizard extends Wizard implements IExportWizard {

	BundleExportInfo expinfo;
	private ProjectAndDestinationPage page1;
	private IDialogSettings settings;
	private String natureID = ConciergeProjectNature.ID;
	private static final String DS_CG_EXPORT_DIRECTORIES = "DS_CG_EXPORT_DIRECTORIES";
	
	public ExportBundlesWizard() {
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
	
	/**
	 * Sets the project nature id of projects to export.
	 * @param natureID
	 */
	protected void setNatureID(String natureID) {
		this.natureID = natureID;
	}

	public void addPages() {
		page1 = new ProjectAndDestinationPage(expinfo, settings, natureID);
		addPage(page1);
	}
	
	protected WizardPage getProjectAndDestinationPage() {
		return page1;
	}

	public boolean performFinish() {

		File loc = new File(expinfo.getLocation());

		if(!loc.exists()) {
			MessageDialog.openWarning(getShell()," Invalid Export Location", loc.getAbsolutePath() + "no longer exists. Please select an alternate location");
			return false;
		}
		
		Iterator projectsIter = expinfo.getProjects().iterator();

		while (projectsIter.hasNext()) {
			IProject proj = (IProject) projectsIter.next();
			try {
				ProjectUtils.exporToJar(loc, proj);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		page1.updateDialogStoredState();
		
		return true;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		expinfo = new BundleExportInfo();

		List selectedProjects = new ArrayList();

		for (Iterator i = selection.iterator(); i.hasNext();) {
			
			IAdaptable ia = (IAdaptable) i.next();
			
			selectedProjects.add(ia.getAdapter(IProject.class));
		}

		expinfo.setSelectedProjects(selectedProjects);
	}
}
