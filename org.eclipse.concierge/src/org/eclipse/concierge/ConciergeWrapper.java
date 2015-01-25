package org.eclipse.concierge;

import org.osgi.framework.BundleException;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class ConciergeWrapper implements WrapperListener {

	Concierge concierge;

	public static void main(String[] args) {
		// Start the application.  If the JVM was launched from the native
		//  Wrapper then the application will wait for the native Wrapper to
		//  call the application's start method.  Otherwise the start method
		//  will be called immediately.
		WrapperManager.start(new ConciergeWrapper(), args);
	}

	/**
	 * Called whenever the native wrapper code traps a system control signal
	 *  against the Java process.  It is up to the callback to take any actions
	 *  necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
	 *    WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or
	 *    WRAPPER_CTRL_SHUTDOWN_EVENT
	 *
	 * @param event The system control signal.
	 */
	public void controlEvent(int event) {
		if(WrapperManager.isControlledByNativeWrapper()) {
			// The Wrapper will take care of this event
		} else
			// We are not being controlled by the Wrapper, so
			//  handle the event ourselves.
			if((event == WrapperManager.WRAPPER_CTRL_C_EVENT) ||
				(event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT) ||
				(event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
				WrapperManager.stop(0);
	}

	public Integer start(String[] args) {

		WrapperManager.signalStarting(600000);

		try {
			concierge = Concierge.doMain(args);
		} catch (Exception e) {
			e.printStackTrace();
			return Integer.valueOf(-1);
		}
		return null;
	}

	public int stop(int exitCode) {

		WrapperManager.signalStopping(120000);

		try {
			concierge.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
		concierge = null;
		return exitCode;
	}
}
