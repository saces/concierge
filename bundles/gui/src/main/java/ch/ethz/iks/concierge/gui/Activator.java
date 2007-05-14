package ch.ethz.iks.concierge.gui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class Activator implements BundleActivator {

	static BundleContext context;
	
	private ConciergeGUI gui;
	
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		gui = new ConciergeGUI();
		gui.setVisible(true);
	}

	public void stop(BundleContext context) throws Exception {
		gui.setVisible(false);
		gui = null;
		Activator.context = null;		
	}
}