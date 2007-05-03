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
package com.buglabs.osgi.concierge.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.login.Configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
import com.buglabs.osgi.concierge.jdt.ConciergeClasspathContainer;
import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;

/**
 * Clients implementing launch configuration delegates should subclass
 * this class.
 * 
 * @author Ken Gilmer - ken@buglabs.net, Angel Roman - roman@mdesystems.com
 * 
 */
public class ConciergeLaunchConfiguration extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	public static final String LAUNCH_BUNDLE_LIST_CONFIG = "LAUNCH_BUNDLE_LIST";
	public static final String INITIALIZE_RUNTIME = "INITIALIZE_RUNTIME";
	public static final String SYSTEM_PROPERTIES = "SYSTEM_PROPERTIES";
	public static final String INSTALL_MAP = "START_MAP";
	public static final String LAUNCH_CORE_BUNDLE_LIST_CONFIG = "LAUNCH_CORE_BUNDLE_LIST_CONFIG";
	public static final String CORE_INSTALL_MAP = "CORE_INSTALL_MAP";
	private File propsFile;
	private File initXargsFile;

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		Vector vmargs = new Vector();

		String MAIN_CLASS = getMainClassFullyQualifiedName();

		
		IClasspathEntry[] ices = getClassPathEntries();
		List classpath = getClassPath();
		
		for (int i = 0; i < ices.length; ++i) {
			classpath.add(ices[i].getPath().toString());
		}

		VMRunnerConfiguration vmconfig = new VMRunnerConfiguration(MAIN_CLASS, (String[]) classpath.toArray(new String[classpath.size()]));
		List vmArgs = getVMArguments();
		if(vmArgs.size() > 0) {
			vmargs.addAll(vmArgs);
		}
		// vmconfig.setVMArguments(new String[]{"-Dorg.osgi.service.http.port="
		// + getHttpPort(configuration),
		// "-Dbug.console.port=3334",
		// "-Djava.library.path=" + SWTHelper.getSWTLocation()});

		try {
			String props = getSystemPropertiesContents(configuration).toString();
			propsFile = createSystemPropertiesFile(props);
			if (propsFile != null && propsFile.exists()) {
				vmargs.add("-Dproperties=" + propsFile.getAbsolutePath());
			}

			String initXargs = getInitXargsContents(configuration).toString();
			initXargsFile = createInitXargsFile(initXargs);
			if (initXargsFile != null) {
				if (initXargsFile.exists()) {
					vmargs.add("-Dxargs=" + initXargsFile.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vmconfig.setVMArguments((String[]) vmargs.toArray(new String[vmargs.size()]));
		// vmconfig.setProgramArguments(new String[]{"-console", "-clean",
		// "-ws", Platform.getWS()});

		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		IVMRunner vmRunner = vmInstall.getVMRunner(mode);
		vmRunner.run(vmconfig, launch, null);
	}

	protected List getVMArguments() {
		return new ArrayList(0);
	}

	protected List getClassPath() {
		return new ArrayList();
	}

	protected IClasspathEntry[] getClassPathEntries() {
		ConciergeClasspathContainer cc = new ConciergeClasspathContainer();
		return cc.getClasspathEntries();
	}

	protected String getMainClassFullyQualifiedName() {
		return  "ch.ethz.iks.concierge.framework.Framework";
	}

	private File createInitXargsFile(String initXargs) throws IOException {
		File stateLocation = ConciergeRuntime.getDefault().getStateLocation().toFile();

		File initXargsFile = new File(stateLocation, "init.xargs");

		if (initXargsFile.exists()) {
			initXargsFile.delete();
		}

		FileWriter fw = new FileWriter(initXargsFile);
		fw.write(initXargs);
		fw.close();

		return initXargsFile;
	}

	/**
	 * Create system properties file based on string list passed from launch configuration UI.
	 * @param contents
	 * @return
	 * @throws IOException
	 */
	private File createSystemPropertiesFile(String contents) throws IOException {
		File stateLocation = ConciergeRuntime.getDefault().getStateLocation().toFile();

		File file = new File(stateLocation, "system.properties");

		if (file.exists()) {
			file.delete();
		}

		FileWriter fw = new FileWriter(file);
		fw.write(contents);
		fw.close();

		System.out.println("wrote " + file.getAbsolutePath());

		return file;
	}

	protected StringBuffer getInitXargsContents(ILaunchConfiguration configuration) throws CoreException, IOException, URISyntaxException {
		StringBuffer buffer = new StringBuffer();

		//Put some helpful comments in the file.
		buffer.append("# Generated by Concierge Tools for Eclipse plugin version ");
		buffer.append(ConciergeRuntime.getDefault().getBundle().getHeaders().get("Bundle-Version"));
		buffer.append(".\n\n");

		//Pass initilization parameter to runtime if specified in the launch configuration.
		if (configuration.getAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, true)) {
			buffer.append("-init\n");			
		}

//		get init.xargs content
		List workspaceBundles = getWorkspaceBundles(configuration);
		// export workspace projects as jars
		List projectJars = exportProjectsAsjars(workspaceBundles);

		List jars = getBundleJars(configuration);
		jars.addAll(projectJars);
		
		
		List installBundles = getInstallBundles(configuration);
		installBundles.addAll(getCoreInstallBundles(configuration));
		Iterator iter = jars.iterator();

		while (iter.hasNext()) {
			File jar = (File) iter.next();
			buffer.append(getInitialCommand(jar, installBundles) + jar.toURI().toString() + "\n");
		}

		return buffer;
	}

	protected List getWorkspaceBundles(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LAUNCH_BUNDLE_LIST_CONFIG, new ArrayList());
	}
	
	protected List getCoreBundles(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LAUNCH_CORE_BUNDLE_LIST_CONFIG, new ArrayList());
	}

	protected List getInstallBundles(ILaunchConfiguration configuration) throws CoreException, IOException, URISyntaxException {
		List projectNames = configuration.getAttribute(INSTALL_MAP, new ArrayList());
		List jarList = new ArrayList();
		
		
		
		for (Iterator i = projectNames.iterator(); i.hasNext();) {
			String prjName = (String) i.next();
			
			jarList.add(ConciergeLaunchConfiguration.getProjectJarURL(getProjectFromName(prjName)));
		}
		
		return jarList;
	}
	
	protected List getCoreInstallBundles(ILaunchConfiguration configuration) throws CoreException, IOException, URISyntaxException {
		List fileStrings = configuration.getAttribute(CORE_INSTALL_MAP, new ArrayList());
		
		List corePathFiles = new ArrayList();
		for (Iterator i = fileStrings.iterator(); i.hasNext();) {
			String fs = (String) i.next();
			corePathFiles.add(new File(fs));
		}
		
		return corePathFiles;
	}

	private IProject getProjectFromName(String prjName) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
	}

	protected List getBundleJars(ILaunchConfiguration configuration) throws CoreException {
		List corePaths = getCoreBundles(configuration);
		List corePathFiles = new ArrayList();
		for (Iterator i = corePaths.iterator(); i.hasNext();) {
			String fs = (String) i.next();
			corePathFiles.add(new File(fs));
		}
		
		return corePathFiles;
	}

	protected String getInitialCommand(File jar, List installBundles) {
		
		String bundleName = jar.getName();
		if (hasStringElement(installBundles, bundleName)) {
			return "-install ";
		}

		return "-istart ";
	}
	
	private boolean hasStringElement(List list, String name) {
		for (Iterator i = list.iterator(); i.hasNext();) {
			Object o = i.next();
			File f = new File(((URL)o).getFile());
		
			if (f.getName().equals(name)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Retrieve system properties string list passed from launch configuration UI.
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected StringBuffer getSystemPropertiesContents(ILaunchConfiguration configuration) throws CoreException {
		StringBuffer sb = new StringBuffer();

		Map properties = configuration.getAttribute(ConciergeLaunchConfiguration.SYSTEM_PROPERTIES, new Hashtable());

		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String k = (String) i.next();
			String v = (String) properties.get(k);

			sb.append(k);
			sb.append("=");
			sb.append(v);
			sb.append("\n");
		}

		return sb;
	}

	private List exportProjectsAsjars(List workspaceBundles) throws CoreException, IOException {
		Vector jars = new Vector();

		File bundlesLoc = getBundlesLocation();

		if(bundlesLoc.exists()) {
			bundlesLoc.delete();
		}
		
		bundlesLoc.mkdir();
		
		List cgProjects = ProjectUtils.getWSCGProjects();
		Iterator projIter = cgProjects.iterator();

		while (projIter.hasNext()) {
			IProject proj = (IProject) projIter.next();

			if (workspaceBundles.contains(proj.getName())) {
				File jar =  ProjectUtils.exporToJar(bundlesLoc, proj);
				jars.add(jar);
			}
		}
		
		return jars;
	}

	private static File getBundlesLocation() {
		return ConciergeRuntime.getDefault().getBundlesLocation();
	}

	public static URL getProjectJarURL(IProject proj) throws IOException, URISyntaxException, CoreException {
		return ProjectUtils.getProjectJarURL(getBundlesLocation(), proj);
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return true;
	}

	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return true;
	}
}