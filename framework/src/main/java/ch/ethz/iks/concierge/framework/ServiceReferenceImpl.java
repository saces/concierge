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

package ch.ethz.iks.concierge.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 */
final class ServiceReferenceImpl implements ServiceReference {
	/**
	 * the bundle object.
	 */
	Bundle bundle;

	/**
	 * the service object.
	 */
	private Object service;

	/**
	 * the service properties.
	 */
	final Dictionary properties;

	/**
	 * the bundles that are using the service.
	 */
	final Map useCounters = new HashMap(0);

	/**
	 * cached service objects if the registered service is a service factory.
	 */
	private HashMap cachedServices = null;

	/**
	 * the registration.
	 */
	ServiceRegistration registration;

	/**
	 * the next service id.
	 */
	private static long nextServiceID = 0;

	
	private final boolean isServiceFactory;
	
	/**
	 * these service properties must not be overwritten by property updates.
	 */
	private final static HashSet forbidden;
	static {
		forbidden = new HashSet(2);
		forbidden.add(Constants.SERVICE_ID.toLowerCase());
		forbidden.add(Constants.OBJECTCLASS.toLowerCase());
	}

	/**
	 * create a new service reference implementation instance.
	 * 
	 * @param bundle
	 *            the bundle.
	 * @param service
	 *            the service object.
	 * @param props
	 *            the service properties.
	 * @param clazzes
	 *            the interface classes that the service is registered under.
	 * @throws ClassNotFoundException
	 */
	ServiceReferenceImpl(final Bundle bundle, final Object service,
			final Dictionary props, final String[] clazzes) {
		if (service instanceof ServiceFactory) {
			isServiceFactory = true;
		} else {
			isServiceFactory = false;
			checkService(service.getClass(), clazzes);			
		}
		
		this.bundle = bundle;
		this.service = service;
		this.properties = props == null ? new Hashtable(2) : new Hashtable(
				props.size() + 2);
		if (props != null) {
			for (Enumeration keys = props.keys(); keys.hasMoreElements();) {
				final Object key = keys.nextElement();
				properties.put(key, props.get(key));
			}
		}
		properties.put(Constants.OBJECTCLASS, clazzes);
		properties.put(Constants.SERVICE_ID, new Long(++nextServiceID));
		final Integer ranking = props == null ? null : (Integer) props.get(Constants.SERVICE_RANKING);
		properties.put(Constants.SERVICE_RANKING, ranking == null ? new Integer(0): ranking);
		this.registration = new ServiceRegistrationImpl();
	}
	
	private void checkService(final Class service, final String[] clazzes) {
		Class current = service;
		final Set remaining = new HashSet(Arrays.asList(clazzes));
		while (current != null) {
			remaining.remove(current.getName());
			final Class[] implIfaces = current.getInterfaces();
			for (int i = 0; i < implIfaces.length; i++) {
				remaining.remove(implIfaces[i].getName());
				if (remaining.isEmpty()) {
					return;
				}
			}
			current = current.getSuperclass();
		}
		throw new IllegalArgumentException(
				"Service " + service.getName() + " does not implement the interfaces " + remaining);
	}

	void invalidate() {
		service = null;
		useCounters.clear();
		bundle = null;
		registration = null;
		if (cachedServices != null) {
			cachedServices = null;
		}
		final String[] keys = getPropertyKeys();
		for (int i=0; i<keys.length; i++) {
			properties.remove(keys[i]);			
		}
	}

	/**
	 * get the bundle that has registered the service.
	 * 
	 * @return the bundle object.
	 * @see org.osgi.framework.ServiceReference#getBundle()
	 * @category ServiceReference
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * get a property.
	 * 
	 * @param key
	 *            the key.
	 * @return the value or null, if the entry does not exist.
	 * @see org.osgi.framework.ServiceReference#getProperty(java.lang.String)
	 * @category ServiceReference
	 */
	public Object getProperty(final String key) {
		// first, try the original case
		Object result = properties.get(key);
		if (result != null) {
			return result;
		}

		// then, try the lower case variant
		result = properties.get(key.toLowerCase());
		if (result != null) {
			return result;
		}

		// bad luck, try case insensitive matching of the keys
		for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
			String k = (String) keys.nextElement();
			if (k.equalsIgnoreCase(key)) {
				result = properties.get(k);
				break;
			}
		}
		return result;
	}

	/**
	 * get all property keys.
	 * 
	 * @return the array of all keys.
	 * @see org.osgi.framework.ServiceReference#getPropertyKeys()
	 * @category ServiceReference
	 */
	public String[] getPropertyKeys() {
		final ArrayList keys = new ArrayList(properties.size());
		final Enumeration keyEnum = properties.keys();
		while (keyEnum.hasMoreElements()) {
			keys.add(keyEnum.nextElement());
		}
		return (String[]) keys.toArray(new String[keys.size()]);
	}

	/**
	 * get the using bundles.
	 * 
	 * @return the array of all bundles.
	 * @see org.osgi.framework.ServiceReference#getUsingBundles()
	 * @category ServiceReference
	 */
	public Bundle[] getUsingBundles() {
		if (useCounters.isEmpty()) {
			return null;
		}
		return (Bundle[]) useCounters.keySet().toArray(
				new Bundle[useCounters.size()]);
	}

	/**
	 * get the service object. If the service is a service factory, a cached
	 * value might be returned.
	 * 
	 * @param theBundle
	 *            the requesting bundle.
	 * @return the service object.
	 */
	Object getService(final Bundle theBundle) {
		if (service == null) {
			return null;
		}
		Integer counter = (Integer) useCounters.get(theBundle);
		if (counter == null) {
			counter = new Integer(1);
		} else {
			counter = new Integer(counter.intValue() + 1);
		}
		useCounters.put(theBundle, counter);

		if (isServiceFactory) {			
			if (cachedServices == null) {
				cachedServices = new HashMap(1);
			}
			final Object cachedService = cachedServices.get(theBundle);
			if (cachedService != null) {
				return cachedService;
			}
			final ServiceFactory factory = (ServiceFactory) service;
			final Object factoredService = factory.getService(theBundle, registration);
			cachedServices.put(theBundle, factoredService);
			return factoredService;
		}
		return service;
	}

	/**
	 * unget the service.
	 * 
	 * @param theBundle
	 *            the using bundle.
	 * @return <tt>false</tt> if the context bundle's use count for the
	 *         service is zero or if the service has been unregistered;
	 *         <tt>true</tt> otherwise.
	 */
	boolean ungetService(final Bundle theBundle) {
		if (service == null) {
			return false;
		}
		Integer counter = (Integer) useCounters.get(theBundle);
		if (counter == null) {
			return false;
		}
		if (counter.intValue() == 1) {
			useCounters.remove(theBundle);
			if (isServiceFactory) {
				((ServiceFactory) service).ungetService(theBundle, registration,
						cachedServices.get(theBundle));
				cachedServices.remove(theBundle);
			}			
			return false;
		} else {
			counter = new Integer(counter.intValue() - 1);
			useCounters.put(theBundle, counter);
			return true;
		}
	}

	/**
	 * get a string representation of the service reference implementation.
	 * 
	 * @return the string.
	 * @category Object
	 */
	public String toString() {
		return "ServiceReference{" + service + "}";
	}

	/**
	 * The service registration. It is a private inner class since this entity
	 * is just once returned to the registrar and never retrieved again. It is
	 * more an additional facet of the service than a separate entity.
	 * 
	 * @author Jan S. Rellermeyer, IKS, ETH Zurich
	 */
	private final class ServiceRegistrationImpl implements ServiceRegistration {

		/**
		 * get the service reference.
		 * 
		 * @return the service reference.
		 * @see org.osgi.framework.ServiceRegistration#getReference()
		 * @category ServiceRegistration
		 */
		public ServiceReference getReference() {
			if (service == null) {
				throw new IllegalStateException(
						"Service has already been uninstalled");
			}
			return ServiceReferenceImpl.this;
		}

		/**
		 * set some new service properties.
		 * 
		 * @param newProps
		 *            the new service properties.
		 * @see org.osgi.framework.ServiceRegistration#setProperties(java.util.Dictionary)
		 * @category ServiceRegistration
		 */
		public void setProperties(final Dictionary newProps) {
			/*
			 * The values
			 * for service.id and objectClass must not be overwritten
			 */

			if (service == null) {
				throw new IllegalStateException(
						"Service has already been uninstalled");
			}

			final HashMap cases = new HashMap(properties.size());
			for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
				final String key = (String) keys.nextElement();
				final String lower = key.toLowerCase();
				if (cases.containsKey(lower)) {
					throw new IllegalArgumentException(
							"Properties contain the same key in different case variants");
				}
				cases.put(lower, key);
			}
			for (Enumeration keys = newProps.keys(); keys.hasMoreElements();) {
				final Object key = keys.nextElement();
				final Object value = newProps.get(key);
				final String lower = ((String) key).toLowerCase();
				
				if (!forbidden.contains(lower)) {
					final Object existing = cases.get(lower);
					if (existing != null) {
						if (existing.equals(key)) {
						properties.remove(existing);
						} else {
							throw new IllegalArgumentException(
							"Properties already exists in a different case variant");												
						}
					}
					properties.put(key, value);
				}
			}

			Framework.notifyServiceListeners(ServiceEvent.MODIFIED,
					ServiceReferenceImpl.this);
		}

		/**
		 * unregister the service.
		 * 
		 * @see org.osgi.framework.ServiceRegistration#unregister()
		 * @category ServiceRegistration
		 */
		public void unregister() {
			if (service == null) {
				throw new IllegalStateException(
						"Service has already been uninstalled");
			}

			Framework.unregisterService(ServiceReferenceImpl.this);
			service = null;
		}
	}
}
