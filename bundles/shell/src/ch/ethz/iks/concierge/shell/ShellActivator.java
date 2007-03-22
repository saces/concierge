/* Copyright (c) 2006 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
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
package ch.ethz.iks.concierge.shell;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import ch.ethz.iks.concierge.shell.Shell.PackageAdminCommandGroup;
import ch.ethz.iks.concierge.shell.Shell.StartLevelCommandGroup;
import ch.ethz.iks.concierge.shell.commands.ShellCommandGroup;

/**
 * Bundle activator for the shell bundle.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public class ShellActivator implements BundleActivator {
	/**
	 * the shell instance.
	 */
	private Shell shell;

	/**
	 * the bundle context.
	 */
	static BundleContext context;

	/**
	 * called, when the bundle is started.
	 * 
	 * @param context
	 *            the bundle context.
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		ShellActivator.context = context;
		List plugins = new ArrayList();

		final ServiceReference pkgAdminRef = context
				.getServiceReference(PackageAdmin.class.getName());
		if (pkgAdminRef != null) {
			plugins.add(new PackageAdminCommandGroup((PackageAdmin) context
					.getService(pkgAdminRef)));
		}
		final ServiceReference startLevelRef = context
				.getServiceReference(StartLevel.class.getName());
		if (startLevelRef != null) {
			plugins.add(new StartLevelCommandGroup((StartLevel) context
					.getService(startLevelRef)));
		}
		shell = new Shell(System.out, System.err, (ShellCommandGroup[]) plugins
				.toArray(new ShellCommandGroup[plugins.size()]));
		context.addServiceListener(shell, "(" + Constants.OBJECTCLASS + "="
				+ ShellCommandGroup.class.getName() + ")");
	}

	/**
	 * called, when the bundle is stopped.
	 * 
	 * @param context
	 *            the bundle context.
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		Shell.running = false;
		shell.interrupt();
	}

}
