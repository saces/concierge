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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator {

	private int port = 80;

	private SharedStateManager sm;

	private HttpServer serverThread;

	public void start(BundleContext context) throws Exception {
		// We assume that a log service is available.
		ServiceReference sr = context.getServiceReference(LogService.class.getName());
		LogService ls = null;
		if (sr != null) {
			ls = (LogService) context.getService(sr);
		}
		sm = new SharedStateManager(ls);

		String portNum = context.getProperty("org.osgi.service.http.port");

		if (portNum != null) {
			port = Integer.parseInt(portNum);
		}

		//Start the http server
		serverThread = new HttpServer(port, sm);
		serverThread.initialize();
		serverThread.start();
		
		//Register the Http Server osgi service.
		Dictionary props = new Hashtable();
		props.put("port", "" + port);
		props.put("openPort", new Integer(port));

		context.registerService(HttpService.class.getName(), new HttpServiceImpl(sm, context.getBundle().getHeaders()), props);
	}

	public void stop(BundleContext context) throws Exception {
		serverThread.interrupt();
		sendPoisonPill();
	}

	private void sendPoisonPill()  {
		//Connecting to the socket should be enough to wake the server so that it may shutdown.
		try {
			Socket s = new Socket("127.0.0.1", port);
			s.close();
		} catch (UnknownHostException e) {
			//Swallow any exceptions, not relevent at bundle level.
		} catch (IOException e) {
		}
	}

}
