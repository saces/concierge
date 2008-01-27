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
package com.buglabs.osgi.concierge.ui.jobs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.buglabs.osgi.concierge.core.utils.ConciergeUtils;
import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
import com.buglabs.osgi.concierge.jdt.ConciergeClasspathContainerInitializer;
import com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainerInitializer;
import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
import com.buglabs.osgi.concierge.templates.GeneratorActivator;
import com.buglabs.osgi.concierge.ui.info.ProjectInfo;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class CreateConciergeProject extends WorkspaceModifyOperation {

	private ProjectInfo projInfo;
	private Vector classpathEntries;
	private IContainer srcContainer;
	private IContainer binContainer;
	
	public CreateConciergeProject(ProjectInfo projInfo) {
		this.projInfo = projInfo;
		classpathEntries = new Vector();
	}

	public List getClasspathEntries() {
		return classpathEntries;
	}
	
	public ProjectInfo getProjectInfo() {
		return projInfo;
	}
	
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = wsroot.getProject(projInfo.getProjectName());
		proj.create(monitor);
		proj.open(monitor);
		
		addNatures(proj, monitor);
		createBinFolder(proj, monitor);
		createSrcFolder(proj, monitor);		
		setProjectClassPath(proj, monitor);
		createManifest(proj, monitor);
		
		if(projInfo.isGenerateActivator()) {
			generateActivator(monitor);
		}
		
		
	}

	protected void addNatures(IProject proj, IProgressMonitor monitor) throws CoreException {
		ConciergeUtils.addNatureToProject(proj, JavaCore.NATURE_ID, monitor);
		ConciergeUtils.addNatureToProject(proj, ConciergeProjectNature.ID, monitor);
	}

	protected void generateActivator(IProgressMonitor monitor) throws CoreException {
		String contents = getActivatorContents().toString();
		
		String fileHandle = projInfo.getActivator().replace('.', '/');
		/*char[] charArray = fileHandle.toCharArray();
		charArray[0] = Character.toLowerCase(charArray[0]);
		fileHandle = new String(charArray);*/
		
		Path activatorpath = new Path(fileHandle + ".java");
		createDeepFile(srcContainer, activatorpath);
		IFile activator = srcContainer.getFile(activatorpath);
		writeContents(activator, contents, monitor);
	}

	protected StringBuffer getActivatorContents() {
		GeneratorActivator gen = new GeneratorActivator();
		return new StringBuffer(gen.generate(projInfo));
	}

	protected void createDeepFile(IContainer container, Path childpath) throws CoreException {
			
		IContainer localContainer = container;
			for(int i = 0; i < childpath.segmentCount() - 1; ++i) {
				IFolder folder = localContainer.getFolder(new Path(childpath.segment(i)));
				folder.create(true, true, new NullProgressMonitor());
				localContainer = folder;
			}
	}

	protected void writeContents(IFile file, String contents, IProgressMonitor monitor) throws CoreException {
		if(file.exists()) {
			file.delete(true, monitor);
		} 
		
			file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
			
	}

	private void createManifest(IProject proj, IProgressMonitor monitor) throws CoreException {
		IFolder metainf = proj.getFolder("META-INF");
		metainf.create(true, true, monitor);
		IFile manifest = metainf.getFile("MANIFEST.MF");
		String contents = getManifestContents().toString();		
		manifest.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}
	
	protected StringBuffer getManifestContents() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Manifest-Version: 1.0\n");
		buffer.append("Bundle-Name: " + ProjectUtils.formatName(projInfo.getProjectName()) + "\n");
		
		if(!projInfo.getActivator().trim().equals("")) {
			buffer.append("Bundle-Activator: " + projInfo.getActivator() + "\n");
		}
		
		if(!projInfo.getSymbolicName().trim().equals("")) {
			buffer.append("Bundle-SymbolicName: " + ProjectUtils.formatName(projInfo.getSymbolicName()) + "\n");
		}
		
		if(!projInfo.getVersion().trim().equals("")) {
			buffer.append("Bundle-Version: " + projInfo.getVersion() + "\n");
		}
		
		if(!projInfo.getVendor().trim().equals("")) {
			buffer.append("Bundle-Vendor: " + projInfo.getVendor() + "\n");
		}
		
		if(!projInfo.getExecutionEnvironment().trim().equals("")) {
			buffer.append("Bundle-RequiredExecutionEnvironment: " + projInfo.getExecutionEnvironment() + "\n");
		}
		
		return buffer;
	}

	protected void addClasspathEntries() {
		classpathEntries.add(createJREEntry());
		classpathEntries.add(JavaCore.newContainerEntry(new Path(ConciergeClasspathContainerInitializer.ID)));
		classpathEntries.add(JavaCore.newContainerEntry(new Path(OSGiBundleClassPathContainerInitializer.ID)));
	}

	private void createSrcFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		srcContainer = proj;//.getFolder("/");// proj.getFolder("src");
		
		if(srcContainer.getType() == IResource.FOLDER) {
			((IFolder) srcContainer).create(true, true, monitor);
		}
		classpathEntries.add(JavaCore.newSourceEntry(srcContainer.getFullPath()));
	}

	private void setProjectClassPath(IProject proj, IProgressMonitor monitor) throws JavaModelException {
		addClasspathEntries();
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setRawClasspath(getClassPathEntries(proj, monitor), null);
	}

	private void createBinFolder(IProject proj, IProgressMonitor monitor) throws CoreException {
		binContainer = proj;
		//bin.create(true, true, monitor);
		IJavaProject jproj = JavaCore.create(proj);
		jproj.setOutputLocation(binContainer.getFullPath(), monitor);
	}

	private IClasspathEntry[] getClassPathEntries(IProject project, IProgressMonitor monitor) {		
		return (IClasspathEntry[]) classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
	}
	
	public static IClasspathEntry createJREEntry() {
			IPath path  = JavaRuntime.newDefaultJREContainerPath();
		return JavaCore.newContainerEntry(path);
	}
}
