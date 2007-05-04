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
 * 
 * $Id$
 */
package ch.ethz.iks.concierge.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.osgi.framework.Filter;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.TopicPermission;

/**
 * <p>
 * encapsulated the mapping between an {@link EventHandler} that has subscribed
 * and the topics it is interested it. Optionally, a {@link Filter} can be
 * provided that will be checked against the properties of incoming events.
 * </p>
 * 
 * @author Jan S. Rellermeyer, ETH Zürich
 */
final class Subscription {
	/**
	 * the <code>EventHandler</code>.
	 */
	private EventHandler handler;

	/**
	 * an array of topics.
	 */
	private String[] topics;

	/**
	 * a filter.
	 */
	private Filter filter;

	/**
	 * hidden default constructor.
	 */
	private Subscription() {
	}

	/**
	 * creates a new EventHandlerSubscription instance.
	 * 
	 * @param handler
	 *            an <code>EventHandler</code> that wants to subscribe.
	 * @param topics
	 *            an array of strings representing the topics.
	 * @param filter
	 *            a <code>Filter</code> for matching event properties.
	 */
	Subscription(final EventHandler eventHandler, final String[] topics,
			final Filter filter) {
		// security check
		if (EventAdminImpl.security != null) {
			ArrayList checkedTopics = new ArrayList(topics.length);
			for (int i = 0; i < topics.length; i++) {
				try {
					EventAdminImpl.security
							.checkPermission(new TopicPermission(topics[i],
									TopicPermission.SUBSCRIBE));
					checkedTopics.add(topics[i]);
				} catch (SecurityException se) {
					System.err
							.println("Bundle does not have permission for subscribing to "
									+ topics[i]);
				}
			}
			this.topics = (String[]) checkedTopics
					.toArray(new String[checkedTopics.size()]);
		} else {
			this.topics = topics;
		}
		this.handler = eventHandler;
		this.filter = filter;
	}

	/**
	 * sends an <code>Event</code> to the <code>EventHandler</code>.
	 * 
	 * @param event
	 *            the <code>Event</code>.
	 */
	void sendEvent(final Event event) {
		try {
			handler.handleEvent(event);
		} catch (Exception shield) {
			shield.printStackTrace();
		}
	}

	/**
	 * get the handler.
	 * 
	 * @return the handler.
	 */
	EventHandler getHandler() {
		return handler;
	}

	/**
	 * checks if an event matches the subscribed topics and the filter, if
	 * present.
	 * 
	 * @param event
	 *            the <code>Event</code>
	 * @return <code>true</code> for the case that the event matches,
	 *         <code>false</code> otherwise.
	 */
	boolean matches(final Event event) {
		if (topics != null && !stringMatch(topics, event.getTopic())) {
			return false;
		}
		if (filter != null && !filter.match(event.properties())) {
			return false;
		}
		return true;
	}

	/**
	 * checks if a string matches a pattern string. Either, the two strings must
	 * be identical or the pattern must contain a wildcard and imply the topic.
	 * 
	 * @param pattern
	 *            a pattern string. E.g. <code>ch/ethz/iks/*</code>.
	 * @param topic
	 *            a topic. E.g. <code>ch/ethz/iks/SAMPLE_TOPIC</code>.
	 * @return <code>true</code> if the topic matches the pattern string,
	 *         <code>false</code> otherwise.
	 */
	private static boolean stringMatch(final String pattern, final String topic) {
		final StringTokenizer strTokens = new StringTokenizer(pattern, "/");
		final StringTokenizer topicTokens = new StringTokenizer(topic, "/");
		while (strTokens.hasMoreTokens()) {
			String current = strTokens.nextToken();
			if (!topicTokens.hasMoreTokens()) {
				return false;
			}
			if (current.equals("*") && !strTokens.hasMoreTokens()) {
				return true;
			}
			if (!current.equals(topicTokens.nextToken())) {
				return false;
			}
		}
		if (topicTokens.hasMoreTokens()) {
			return false;
		}
		return true;
	}

	/**
	 * checks if a string matches one of the pattern strings.
	 * 
	 * @param patterns
	 *            an array of pattern strings.
	 * @param topic
	 *            a topic. E.g. <code>ch.ethz.iks.SampleTopic</code>.
	 * @return <code>true</code> if the topic matches the pattern string,
	 *         <code>false</code> otherwise.
	 */
	private static boolean stringMatch(final String[] patterns,
			final String topic) {
		for (int i = 0; i < patterns.length; i++) {
			if (stringMatch(patterns[i], topic)) {
				return true;
			}
		}
		return false;
	}

	void update(final String[] topics, final Filter filter) {
		this.topics = topics;
		this.filter = filter;
	}

	/**
	 * get a string representation of the <code>EventHandlerSubscription</code>.
	 * 
	 * @return a <code>String</code> representation.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[EventHandlerSubscription] ");
		buffer.append(handler.getClass().getName());
		buffer.append(", topics ");
		if (topics != null) {
			buffer.append(Arrays.asList(topics));
		} else {
			buffer.append("*");
		}
		if (filter != null) {
			buffer.append(", filter '");
			buffer.append(filter);
			buffer.append("'");
		}
		return buffer.toString();
	}
}
