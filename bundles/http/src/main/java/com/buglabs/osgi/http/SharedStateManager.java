/* Copyright (c) 2007 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
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

package com.buglabs.osgi.http;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;

/**
 * This object manages shared state of Http Service accesses by HttpServiceImpl
 * (OSGi interface) and SocketListener (Http Interface).
 * 
 * @author ken
 * 
 */
public class SharedStateManager implements LogService {
	private Map map;

	volatile private LogService logService;

	private Map httpContextMap;

	private Map servletConfigMap;

	public SharedStateManager(LogService logService) {
		this.logService = logService;
		map = new Hashtable();
	}

	public void addServlet(String name, Servlet servlet) {
		map.put(name, servlet);
	}

	public Servlet getServlet(String name) {
		return (Servlet) map.get(name);
	}

	synchronized public boolean removeServlet(String name) {
		if (map.containsKey(name)) {
			map.remove(name);
			return true;
		}
		return false;
	}

	public boolean hasServlet(String name) {
		return map.containsKey(name);
	}

	public void addHttpContext(String alias, HttpContext context) {
		if (httpContextMap == null) {
			httpContextMap = new Hashtable();
		}

		synchronized (httpContextMap) {
			if (!httpContextMap.containsKey(alias)) {
				httpContextMap.put(alias, context);
			}
		}
	}

	public HttpContext getHttpContext(String name) {
		return (HttpContext) httpContextMap.get(name);
	}

	/**
	 * Refer to OSGi R3 Spec Section 14.2
	 * 
	 * @param name
	 * @throws NamespaceException
	 */
	public static void validateAlias(String name) throws NamespaceException {
		if (!name.startsWith("/")) {
			throw new NamespaceException("Invalid alias: " + name);
		}
	}

	public void log(int level, String message, Throwable exception) {
		logService.log(level, message, exception);
	}

	public void log(int level, String message) {
		logService.log(level, message);
	}

	public void log(ServiceReference sr, int level, String message, Throwable exception) {
		logService.log(sr, level, message, exception);
	}

	public void log(ServiceReference sr, int level, String message) {
		logService.log(sr, level, message);
	}

	public void addServletConfig(String alias, ServletConfig config) {
		if (servletConfigMap == null) {
			servletConfigMap = new Hashtable();
		}

		synchronized (httpContextMap) {
			if (!servletConfigMap.containsKey(alias)) {
				servletConfigMap.put(alias, config);
			}
		}
	}

	public ServletConfig getServletConfig(String alias) {
		return (ServletConfig) servletConfigMap.get(alias);
	}

}
