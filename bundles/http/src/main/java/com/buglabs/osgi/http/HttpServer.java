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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;

/**
 * This class listenens for client requests. It is currently single-threaded.
 * 
 * @author ken
 * 
 */
public class HttpServer extends Thread {

	private static final int CLIENT_TIMEOUT = 10;

	private static final int MAX_CLIENT_RETRY = 50000;

	private final int port;

	private ServerSocket socket;

	private final SharedStateManager ssm;

	public HttpServer(int port, SharedStateManager sm) {
		this.port = port;
		this.ssm = sm;

	}

	public void initialize() throws IOException {
		socket = new ServerSocket(port);
	}

	public void run() {
		Socket connection = null;
		while (true) {
			try {
				ssm.log(LogService.LOG_INFO, "HTTP Server waiting for connections...");
				connection = socket.accept();

				// Check to see if we got a poison pill. If so cleanup and exit.
				if (this.isInterrupted()) {
					ssm.log(LogService.LOG_INFO, "Server received shutdown-message.  Shutting down.");
					connection.close();
					socket.close();
					return;
				}

				ssm.log(LogService.LOG_DEBUG, "Http Server received new request.");
				
				// Now we wait until the client is sending us bytes, or give up if we wait too long.
				int reqCount = 0;
				while (reqCount < MAX_CLIENT_RETRY && connection.getInputStream().available() == 0) {
					try {
						Thread.sleep(CLIENT_TIMEOUT);
						reqCount++;
					} catch (InterruptedException e) {
						if (Thread.interrupted()) {
							ssm.log(LogService.LOG_INFO, "Server received shutdown-message.  Shutting down.");
							connection.close();
							socket.close();
							return;
						}
					}
				}
				
				// Chech to see if we have bytes available after max wait.  If not, abort.
				if (connection.getInputStream().available() == 0) {
					ssm.log(LogService.LOG_WARNING, "Client did not send any data, aborting connection.");
					continue;
				}
				
				// Build the HttpServletRequest object based on the client
				// connection.
				HttpServletRequest request = new ServletRequestImpl(connection, ssm);

				// Determine relative path from HTTP headers.
				String name = getAlias(request);

				if (name == null) {
					throw new ServletException("Unable to retrieve alias from servlet.  Request: " + request.getPathInfo());
				}

				// Make sure relative path is valid
				SharedStateManager.validateAlias(name);

				// Check to see if relative path as is maps to a registered
				// servlet.
				Servlet servlet = getServlet(name);

				if (servlet != null) {
					// Change the path to reflect the nesting of the servlet
					((ServletRequestImpl) request).setUri(null);
				} else {
					// Check to see if path can be resolved to sub alias.
					String subName = getSubAlias(name);

					if (subName != null) {
						String pathInfo = name.substring(subName.length());
						((ServletRequestImpl) request).setUri(pathInfo);
						name = subName;
						servlet = getServlet(name);
					}
				}

				// Construct the response object as we may want to pass an error
				// back.
				//OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
				HttpServletResponse response = new ServletResponseImp(connection.getOutputStream(), request);

				if (servlet == null) {
					handleNonMatchingAlias(response);
				} else {
					if (authenticateRequest(name, request, response)) {
						processRequest(getServlet(name), request, response);
						response.flushBuffer();						
					} else {
						ssm.log(LogService.LOG_WARNING, "Client authentication unsuccesful.");
					}
				}
			} catch (IOException e) {
				ssm.log(LogService.LOG_WARNING, "An I/O exception occurred: " + e.getMessage(), e);
			} catch (ServletException e) {
				ssm.log(LogService.LOG_ERROR, "An exception occured in a servlet: " + e.getMessage(), e);
			} catch (NamespaceException e) {
				ssm.log(LogService.LOG_ERROR, "A namespace exception occured in a servlet: " + e.getMessage(), e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Refer to OSGi R3 Spec Section 14.2
	 * 
	 * @param name
	 * @throws IOException
	 * @throws NamespaceException
	 */
	private void handleNonMatchingAlias(HttpServletResponse bsresp) throws IOException {
		bsresp.sendError(404);
	}

	/**
	 * @param connection
	 * @param bsreq
	 * @return
	 */
	public static String getAlias(HttpServletRequest bsreq) {
		String path = bsreq.getPathInfo();

		if (path == null) {
			return null;
		}

		// Strip off the param list if one exists.
		int p = path.indexOf("?");
		if (p > -1) {
			return path.substring(0, p);
		}

		return path;
	}

	/**
	 * Refer to OSGi R3 Spec Section 14.4.6
	 * 
	 * @param alias
	 * @return
	 */
	private Servlet getServlet(String alias) {
		// First check for a direct match:
		return (Servlet) ssm.getServlet(alias);
	}

	/**
	 * Reduce the path to determine if sub path is alias for existing servlet.
	 * 
	 * @param alias
	 * @return
	 */
	private String getSubAlias(String alias) {
		String[] elements = split(alias, "/");
		// Now try to parse the path to see if a Servlet is registered upstream.
		int index = alias.length();
		for (int i = elements.length - 1; i > 0; --i) {
			index -= (elements[i].length() + 1);
			String subAlias = alias.substring(0, index);
			if (ssm.getServlet(subAlias) != null) {
				return subAlias;
			}
		}

		return null;
	}

	/**
	 * Refer to OSGi Spec R3 Section 14.4.1
	 * 
	 * @param servlet
	 * @return
	 * @throws IOException
	 */
	private boolean authenticateRequest(String alias, HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpContext c = ssm.getHttpContext(alias);

		if (c != null) {
			return c.handleSecurity(request, response);
		}

		throw new RuntimeException("Servlet " + alias + " has no associated HttpContext.");
	}

	private void processRequest(Servlet servlet, HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		if (servlet != null) {
			servlet.service(request, response);
		}
	}

	/**
	 * custom string splitting function as CDC/Foundation does not include
	 * String.split();
	 * 
	 * @param s
	 *            Input String
	 * @param seperator
	 * @return
	 */
	public static String[] split(String s, String seperator) {
		if (s.length() == 0 || seperator.length() == 0) {
			return (new String[0]);
		}

		List tokens = new ArrayList();
		String token;
		int index_a = 0;
		int index_b = 0;

		while (true) {
			index_b = s.indexOf(seperator, index_a);
			if (index_b == -1) {
				token = s.substring(index_a);

				if (token.length() > 0) {
					tokens.add(token);
				}

				break;
			}
			token = s.substring(index_a, index_b);
			token.trim();
			if (token.length() >= 0) {
				tokens.add(token);
			}
			index_a = index_b + seperator.length();
		}
		String[] str_array = new String[tokens.size()];
		for (int i = 0; i < str_array.length; i++) {
			str_array[i] = (String) (tokens.get(i));
		}
		return str_array;
	}
}
