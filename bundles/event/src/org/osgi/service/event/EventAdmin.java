package org.osgi.service.event;

// Referenced classes of package org.osgi.service.event:
//            Event

/**
 * the EventAdmin service.
 */
public interface EventAdmin {

	/**
	 * post an event.
	 * 
	 * @param event
	 *            the event.
	 */
	void postEvent(final Event event);

	/**
	 * send an event.
	 * 
	 * @param event
	 *            the event.
	 */
	void sendEvent(final Event event);

}
