package ch.ethz.iks.concierge.service.http;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class HttpServiceActivator implements BundleActivator {

	static BundleContext context;
	/**
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		HttpServiceActivator.context = context;
		
	}

	public void stop(BundleContext context) throws Exception {
		
	}

}
