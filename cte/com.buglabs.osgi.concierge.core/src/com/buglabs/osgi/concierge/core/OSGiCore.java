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
package com.buglabs.osgi.concierge.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Angel Roman - roman@mdesystems.com
 */
public class OSGiCore extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.buglabs.osgi.concierge.core";
	public static final String EXT_POINT_PACKAGE_PROVIDER = "com.buglabs.osgi.concierge.core.packageproviders";

	// The shared instance
	private static OSGiCore plugin;

	private BundleModelManager bundleModelManager;

	/**
	 * The constructor
	 */
	public OSGiCore() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static OSGiCore getDefault() {
		return plugin;
	}

	synchronized public BundleModelManager getBundleModelManager() {
		if(bundleModelManager == null) {
			bundleModelManager = BundleModelManager.getInstance();
		}

		return bundleModelManager;
	}

	/**
	 * Log an exception to the Eclipse error log.
	 * @param e
	 */
	public static void logException(Exception e) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), null));
	}

	public static IPackageProvider[] getPackageProviders() {

		List pprovs = new ArrayList();

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "packageproviders").getExtensions();
		for(int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] confElements = extensions[i].getConfigurationElements();
			for(int j = 0; j < confElements.length; ++j) {
				try {
					IPackageProvider prov = (IPackageProvider) confElements[j].createExecutableExtension("class");
					pprovs.add(prov);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Unable to create executable extension for " + confElements[j].getName(), e));
				}
			}
		}

		return (IPackageProvider[]) pprovs.toArray(new IPackageProvider[pprovs.size()]);
	}
}
