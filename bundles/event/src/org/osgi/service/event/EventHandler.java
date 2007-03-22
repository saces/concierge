package org.osgi.service.event;

/**
 * EventHandler.
 * 
 * @author OSGi
 */
public interface EventHandler {

	/**
	 * handle an event.
	 * 
	 * @param event
	 *            event.
	 */
	void handleEvent(final Event event);

}
