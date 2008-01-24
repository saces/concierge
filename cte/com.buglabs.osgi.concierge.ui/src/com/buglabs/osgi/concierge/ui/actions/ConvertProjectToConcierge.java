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
package com.buglabs.osgi.concierge.ui.actions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.buglabs.osgi.concierge.core.OSGiCore;
import com.buglabs.osgi.concierge.core.utils.ConciergeUtils;
import com.buglabs.osgi.concierge.jdt.ConciergeClasspathContainerInitializer;
import com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainerInitializer;
import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
import com.buglabs.osgi.concierge.ui.Activator;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class ConvertProjectToConcierge extends Action {
	IProject project;

	public ConvertProjectToConcierge(IProject project) {
		this.project = project;
	}

	public void run() {
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			if(!project.hasNature(ConciergeProjectNature.ID)) {
//				ConciergeUtils.addNatureToProject(project, JavaCore.NATURE_ID, monitor);
				ConciergeUtils.addNatureToProject(project, ConciergeProjectNature.ID, monitor);
			}


			IJavaProject jproj = JavaCore.create(project);
			IPath ccPath = new Path(ConciergeClasspathContainerInitializer.ID);
			IPath importedCPath = new Path(OSGiBundleClassPathContainerInitializer.ID);

			addClasspathContainer(jproj, ccPath);
			addClasspathContainer(jproj, importedCPath);
			checkProjectSrcFolder(jproj);
			checkProjectBinFolder(jproj);

		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, OSGiCore.PLUGIN_ID, IStatus.ERROR, e.getMessage(), null));
		}
	}

	private void checkProjectBinFolder(IJavaProject jproj) throws JavaModelException {
		String outputLoc = jproj.getOutputLocation().toString();
		IPath wsRelativePath = jproj.getPath();
		if(!outputLoc.equals(wsRelativePath.toString())) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openWarning(shell, "Output Location", "Please set the output location from \"" +  outputLoc + " to " + jproj.getPath().toString());
		}
	}

	private void checkProjectSrcFolder(IJavaProject jproj) throws JavaModelException {

		List entries = ConciergeUtils.getClasspathEntries(jproj, IClasspathEntry.CPE_SOURCE);

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		if(entries.size() == 1) {
			IClasspathEntry cpe = (IClasspathEntry) entries.get(0);
			String loc = cpe.getPath().toString();
			IPath wsRelativePath = jproj.getPath();
			if(!loc.equals(wsRelativePath.toString())) {
				MessageDialog.openWarning(shell, "Source Location", "Please set the source location from \"" +  loc + " to " + jproj.getPath().toString());
			}	
		} else if(entries.size() > 1) {
			MessageDialog.openWarning(shell, "Source Location", "Please limit project to one source location.");
		}
	}

	private void addClasspathContainer(IJavaProject jproj, IPath ccPath) throws JavaModelException {
		if(!hasClasspathContainer(jproj, ccPath)) {
			List ces = Arrays.asList(jproj.getRawClasspath());

			Vector newces = new Vector();
			newces.addAll(ces);
			newces.add(JavaCore.newContainerEntry(ccPath));
			jproj.setRawClasspath((IClasspathEntry[]) newces.toArray(new IClasspathEntry[newces.size()]), new NullProgressMonitor());
		}
	}

	private boolean hasClasspathContainer(IJavaProject jproj, IPath path) {

		try {
			List ces = Arrays.asList(jproj.getRawClasspath());

			Iterator cesIter = ces.iterator();

			while(cesIter.hasNext()) {
				IClasspathEntry cc = (IClasspathEntry) cesIter.next();
				if(cc.getPath().equals(path)) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, OSGiCore.PLUGIN_ID, IStatus.ERROR, e.getMessage(), null));
		}

		return false;
	}
}
