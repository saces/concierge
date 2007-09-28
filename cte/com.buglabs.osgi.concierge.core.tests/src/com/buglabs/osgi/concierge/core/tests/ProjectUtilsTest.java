package com.buglabs.osgi.concierge.core.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
import com.buglabs.osgi.concierge.ui.info.ProjectInfo;
import com.buglabs.osgi.concierge.ui.jobs.CreateConciergeProject;

import junit.framework.TestCase;

public class ProjectUtilsTest extends TestCase {
	
	public void testExportToJar() throws CoreException, IOException, InvocationTargetException, InterruptedException
	{
		IProject project = createNestedJarProject("TestProject");
		File jarLoc = new File("/tmp");

		File exportJar = ProjectUtils.exporToJar(jarLoc, project);
		assertTrue(exportJar.exists());
	}
	
	public IProject createNestedJarProject(String name) throws CoreException, IOException, InvocationTargetException, InterruptedException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		NullProgressMonitor nm  = new NullProgressMonitor();
		
		ProjectInfo projInfo = new ProjectInfo();
		projInfo.setProjectName(name);
		projInfo.setActivator("project.Activator");
		projInfo.setGenerateActivator(true);
		projInfo.setSymbolicName(name);
		

		IProject proj = root.getProject(name);
		if(proj.exists()) {
			proj.delete(true, nm);
		}
		
		CreateConciergeProject ccp = new CreateConciergeProject(projInfo);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.run(true, false, ccp);
		
		ws.run(new IWorkspaceRunnable(){

			public void run(IProgressMonitor monitor) throws CoreException {
				
			}}, nm);
		
		
		assertTrue(proj.exists());
		
		IFolder jarsFolder = proj.getFolder("jars");
		if(!jarsFolder.exists()) {
			jarsFolder.create(true, true, nm);
		}
		
		
		IFile xercesJar = jarsFolder.getFile("xerces.jar");
		
		if(!xercesJar.exists()) {
			URL url = Activator.getDefault().getBundle().getResource("testjars/xerces.jar");
			InputStream xercesIS = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("testjars/xerces.jar"), false );
			
			xercesJar.create(xercesIS, true, nm);
		}
		
		assertTrue(xercesJar.exists());
		
		return proj;
		
	}
}
