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
package com.buglabs.osgi.concierge.jdt;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.buglabs.osgi.concierge.core.BundleModelManager;
import com.buglabs.osgi.concierge.core.OSGiCore;
import com.buglabs.osgi.concierge.core.utils.ManifestUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 * 
 */
public class OSGiBundleClassPathContainer implements
IClasspathContainer {

	public static final String ID = "com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainer";

	IJavaProject project;
	Vector projectsOfInterest;
	Vector jarsOfInterest;
	private Vector cpes;

	public OSGiBundleClassPathContainer(IJavaProject project) {
		this.project = project;
		cpes = new Vector();
		projectsOfInterest = new Vector();
		jarsOfInterest = new Vector();
	}

	public IClasspathEntry[] getClasspathEntries() {
		//find projects that export packages this project is interested in.
		addExportingProjects();

		//add jar files in Bundle-ClassPath header entry to classpaths
		addBundleClassPathEntries();

		return (IClasspathEntry[]) cpes.toArray(new IClasspathEntry[cpes.size()]);
	}

	private void addBundleClassPathEntries() {
		IFile manifest = ProjectUtils.getManifestFile(project.getProject());

		if(manifest != null) {
			if(manifest.exists()) {
				try {
					List bundleCPElements = ManifestUtils.getBundleClassPath(manifest.getContents());
					if(bundleCPElements != null) {
						Iterator bcpeIter =  bundleCPElements.iterator();

						while(bcpeIter.hasNext()) {
							String bcpeStr = (String) bcpeIter.next();

							if(!bcpeStr.equals(".") && !bcpeStr.equals("./")) {
								//Support OSGi R3 Bundle-ClassPath entries, only jars
								if(bcpeStr.endsWith(".jar")) {
									IProject iproj = project.getProject();
									IFile jarFile = iproj.getFile(bcpeStr);
									if(jarFile != null) {
										if(jarFile.exists()) {
											if(!jarsOfInterest.contains(bcpeStr)) {
												cpes.add(JavaCore.newLibraryEntry(jarFile.getLocation(), null, null, true));
												jarsOfInterest.add(bcpeStr);
											}
										}
									}
								}
							}
						}
					}

				} catch (IOException e) {
					OSGiCore.getDefault().getLog().log(new Status(IStatus.ERROR, OSGiCore.PLUGIN_ID, IStatus.ERROR, e.getMessage(), null));
				} catch (CoreException e) {
					OSGiCore.getDefault().getLog().log(new Status(IStatus.ERROR, OSGiCore.PLUGIN_ID, IStatus.ERROR, e.getMessage(), null));
				}
			}
		}
	}

	private void addExportingProjects() {
		//get all imports
		BundleModelManager bmm = OSGiCore.getDefault().getBundleModelManager();
		List imports = bmm.getProjectImports(project.getProject());

		Iterator importIter = imports.iterator();

		while(importIter.hasNext()) {
			String importStr = (String) importIter.next();
			IProject exportingProject = bmm.findProjectThatExports(importStr, project.getProject());
			if(exportingProject != null) {
				if(!projectsOfInterest.contains(exportingProject)) {
					projectsOfInterest.add(exportingProject);
					cpes.add(JavaCore.newProjectEntry(exportingProject.getFullPath()));
				}
			}
		}
	}

	public String getDescription() {
		return "OSGi Bundle Classpath";
	}

	public int getKind() {
		return K_APPLICATION;
	}

	public IPath getPath() {
		return new Path(ID);
	}
}
