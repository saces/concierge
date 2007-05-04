package ch.ethz.iks.concierge.event;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

public class EventAdminActivator implements BundleActivator {

	/**
	 * 
	 */
	static BundleContext context;

	/**
	 * 
	 */
	private EventAdminImpl eventAdmin;

	/**
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		EventAdminActivator.context = context;
		eventAdmin = new EventAdminImpl();
		context.addBundleListener(eventAdmin);
		context.addServiceListener(eventAdmin);
		context.addFrameworkListener(eventAdmin);
		context.registerService(EventAdmin.class.getName(), eventAdmin, null);
	}

	/**
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		context.removeBundleListener(eventAdmin);
		context.removeServiceListener(eventAdmin);
		context.removeFrameworkListener(eventAdmin);
		eventAdmin.running = false;
		EventAdminActivator.context = null;
	}

}
