package org.osgi.service.event;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.osgi.framework.Filter;

/**
 * OSGi Event.
 * 
 * @author OSGi.
 */
public class Event {

	/**
	 * the topic.
	 */
	String topic;

	/**
	 * the properties.
	 */
	Hashtable properties;

	/**
	 * the separator.
	 */
	private static final String SEPARATOR = "/";

	/**
	 * the alpha grammar.
	 */
	private static final String alphaGrammar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	/**
	 * the token grammar.
	 */
	private static final String tokenGrammar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";

	/**
	 * create a new Event.
	 * 
	 * @param topic
	 *            the topic.
	 * @param properties
	 *            the properties.
	 */
	public Event(final String topic, final Dictionary properties) {
		this.topic = topic;
		validateTopicName();
		this.properties = new Hashtable();
		if (properties != null) {
			String key;
			Object value;
			for (Enumeration e = properties.keys(); e.hasMoreElements(); this.properties
					.put(key, value)) {
				key = (String) e.nextElement();
				value = properties.get(key);
			}

		}
		this.properties.put("event.topics", topic);
	}

	/**
	 * get the properties.
	 * 
	 * @return the properties.
	 */
	public final Dictionary properties() {
		return properties;
	}

	/**
	 * get a certain property.
	 * 
	 * @param name
	 *            the property name.
	 * @return the value.
	 */
	public final Object getProperty(final String name) {
		return properties.get(name);
	}

	/**
	 * get the property names.
	 * 
	 * @return the property names.
	 */
	public final String[] getPropertyNames() {
		String names[] = new String[properties.size()];
		Enumeration keys = properties.keys();
		for (int i = 0; keys.hasMoreElements(); i++) {
			names[i] = (String) keys.nextElement();
		}

		return names;
	}

	/**
	 * get the topic.
	 * 
	 * @return the topic.
	 */
	public final String getTopic() {
		return topic;
	}

	/**
	 * does the event match a filter.
	 * 
	 * @param filter
	 *            the filter.
	 * @return true or false.
	 */
	public final boolean matches(final Filter filter) {
		// TODO: should be matchCase for R4 ...
		return filter.match(properties);
	}

	/**
	 * does the event equals another object ?
	 * 
	 * @param object
	 *            the other object.
	 * @return true or false.
	 */
	public final boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof Event)) {
			return false;
		}
		Event event = (Event) object;
		return topic.equals(event.topic) && properties.equals(event.properties);
	}

	/**
	 * the hash code.
	 * 
	 * @return the hash code.
	 */
	public final int hashCode() {
		return topic.hashCode() ^ properties.hashCode();
	}

	/**
	 * get a string representation.
	 * 
	 * @return the string.
	 */
	public String toString() {
		return getClass().getName() + " [topic=" + topic + "]";
	}

	/**
	 * validate the topic name.
	 * 
	 */
	private void validateTopicName() {
		try {
			StringTokenizer st = new StringTokenizer(topic, SEPARATOR, true);
			validateToken(st.nextToken());
			for (; st.hasMoreTokens(); validateToken(st.nextToken())) {
				st.nextToken();
			}

		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("invalid topic");
		}
	}

	/**
	 * validate the token.
	 * 
	 * @param token
	 *            the token.
	 */
	private void validateToken(final String token) {
		int length = token.length();
		if (length < 1) {
			throw new IllegalArgumentException("invalid topic");
		}
		if (alphaGrammar.indexOf(token.charAt(0)) == -1) {
			throw new IllegalArgumentException("invalid topic");
		}
		for (int i = 1; i < length; i++) {
			if (tokenGrammar.indexOf(token.charAt(i)) == -1) {
				throw new IllegalArgumentException("invalid topic");
			}
		}

	}
}
