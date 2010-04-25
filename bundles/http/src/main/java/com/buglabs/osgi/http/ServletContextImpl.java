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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.osgi.service.http.HttpContext;
import org.osgi.service.log.LogService;

/**
 * Basic ServletContext implementation. Exposes some metadata from Bundle
 * manifest.
 * 
 * @author ken
 * 
 */
public class ServletContextImpl implements ServletContext, ServletConfig {

	private final Dictionary initParams;

	private final Dictionary bundleHeaders;

	private final String alias;

	private Dictionary attribs;

	private final HttpContext defaultHttpContext;

	private final SharedStateManager sm;

	public ServletContextImpl(Dictionary initParams, Dictionary bundleHeaders, String alias, HttpContext defaultHttpContext,
			SharedStateManager sm) {
		this.initParams = initParams;
		this.bundleHeaders = bundleHeaders;
		this.alias = alias;
		this.defaultHttpContext = defaultHttpContext;
		this.sm = sm;
	}

	public Object getAttribute(String arg0) {
		if (attribs == null) {
			return null;
		}

		return attribs.get(arg0);
	}

	public Enumeration getAttributeNames() {
		if (attribs == null) {
			return new EmptyEnumeration();
		}

		return attribs.keys();
	}

	public ServletContext getContext(String arg0) {
		// TODO determine appropriate behavior.
		return this;
	}

	public String getInitParameter(String arg0) {
		return initParams.get(arg0).toString();
	}

	public Enumeration getInitParameterNames() {
		return initParams.keys();
	}

	public int getMajorVersion() {
		return 2;
	}

	public String getMimeType(String arg0) {
		return defaultHttpContext.getMimeType(arg0);
	}

	public int getMinorVersion() {
		return 3;
	}

	public RequestDispatcher getNamedDispatcher(String arg0) {
		throw new RuntimeException("Unimplemented feature.");
	}

	public String getRealPath(String arg0) {
		throw new RuntimeException("Unimplemented feature.");
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new RuntimeException("Unimplemented feature.");
	}

	public URL getResource(String arg0) throws MalformedURLException {
		return defaultHttpContext.getResource(arg0);
	}

	public InputStream getResourceAsStream(String arg0) {
		throw new RuntimeException("Unimplemented feature.");
	}

	public Set getResourcePaths(String arg0) {
		throw new RuntimeException("Unimplemented feature.");
	}

	public String getServerInfo() {
		//return bundleHeaders.get("Bundle-Name").toString() + " " + bundleHeaders.get("Bundle-Version") + " by "
		//				+ bundleHeaders.get("Bundle-Vendor");
		// Origin code is NPEing if headers not set...
		// print the whole bundle info for now.
		StringBuilder sb = new StringBuilder();
		Enumeration en = bundleHeaders.keys();
		boolean isFirst = true;
		while (en.hasMoreElements()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append("; ");
			}
			sb.append('\'');
			String k = (String) en.nextElement();
			sb.append(k);
			sb.append("'='");
			String v = (String) bundleHeaders.get(k);
			sb.append(v);
			sb.append('\'');
		}
		return sb.toString();
	}

	public Servlet getServlet(String arg0) throws ServletException {
		// Intended to always return null. See
		// http://java.sun.com/j2ee/1.4/docs/api/javax/servlet/ServletContext.html#getServlet(java.lang.String)
		return null;
	}

	public String getServletContextName() {
		return alias;
	}

	public Enumeration getServletNames() {
		return new Enumeration() {
			boolean flipper = true;

			public boolean hasMoreElements() {

				return flipper;
			}

			public Object nextElement() {
				flipper = false;
				return getServletContextName();
			}
		};
	}

	public Enumeration getServlets() {
		// Intended to return empty Enumeration.
		// http://java.sun.com/j2ee/1.4/docs/api/javax/servlet/ServletContext.html#getServlets()
		return new EmptyEnumeration();
	}

	public void log(String arg0) {
		sm.log(LogService.LOG_INFO, arg0);
	}

	public void log(Exception arg0, String arg1) {
		sm.log(LogService.LOG_ERROR, arg1, arg0);
	}

	public void log(String arg0, Throwable arg1) {
		sm.log(LogService.LOG_ERROR, arg0, arg1);
	}

	public void removeAttribute(String arg0) {
		if (attribs != null) {
			attribs.remove(arg0);
		}
	}

	public void setAttribute(String arg0, Object arg1) {
		if (attribs == null) {
			attribs = new Hashtable();
		}

		attribs.put(arg0, arg1);
	}

	/**
	 * An empty enumeration.
	 * 
	 * @author ken
	 * 
	 */
	private class EmptyEnumeration implements Enumeration {

		public boolean hasMoreElements() {
			return false;
		}

		public Object nextElement() {
			return null;
		}

	}

	public ServletContext getServletContext() {
		
		return this;
	}

	public String getServletName() {
		return alias;
	}
}
