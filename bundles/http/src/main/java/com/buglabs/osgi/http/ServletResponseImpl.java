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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A very basic http response object.
 * 
 * @author ken
 * 
 */
class ServletResponseImpl implements HttpServletResponse {
	private static final String SPACE = " ";

	private static final String CRLF = "\r\n";

	private final OutputStreamWriter writer;

	private boolean committed = false;

	private String contentType;

	private Locale locale;

	private List cookies;

	private int contentLength;

	private Dictionary headers;

	private int status;

	/**
	 * Flag for determing if HTTP headers have already been written to output
	 * stream.
	 */
	private boolean headersWritten = false;

	private final HttpServletRequest request;

	public ServletResponseImpl(OutputStreamWriter writer, HttpServletRequest request) {
		this.writer = writer;
		this.request = request;
		this.locale = Locale.US;
		this.status = 200;
	}

	public void flushBuffer() throws IOException {
		if (!headersWritten && !committed) {
			writeHeaders(writer);
		}
		
		writer.flush();
	}

	public int getBufferSize() {
		return 0;
	}

	public String getCharacterEncoding() {
		return writer.getEncoding();
	}

	public Locale getLocale() {
		// TODO Find out what this should be set to.
		return locale;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (!headersWritten) {
			writeHeaders(writer);
		}

		return new ServletOutputStream() {

			public void write(int arg0) throws IOException {
				writer.write(arg0);
			}

		};
	}

	/**
	 * Emit header data to output stream based on current state of Response
	 * object. Written based on specification defined here:
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
	 * 
	 * @param w
	 * @throws IOException
	 */
	private void writeHeaders(Writer w) throws IOException {
		StringBuffer sb = new StringBuffer();

		// Status-Line
		sb.append(request.getProtocol());
		sb.append(SPACE);
		sb.append(status);
		sb.append(SPACE);
		sb.append(generateHttpCodeDescription(status));
		sb.append(CRLF);

		// Headers
		if (contentType != null) {
			sb.append("Content-Type: ");
			sb.append(contentType);
			sb.append(CRLF);
		}
		sb.append("Connection: close");

		sb.append(CRLF);
		sb.append(CRLF);

		synchronized (w) {
			w.write(sb.toString());
		}

		headersWritten = true;
		committed = true;
	}

	public PrintWriter getWriter() throws IOException {
		if (!headersWritten) {
			writeHeaders(writer);
		}

		return new PrintWriter(writer);
	}

	public boolean isCommitted() {

		return committed;
	}

	public void reset() {

	}

	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setContentLength(int arg0) {
		contentLength = arg0;
	}

	public void setContentType(String arg0) {
		contentType = arg0;
	}

	public void setLocale(Locale arg0) {
		this.locale = arg0;
	}

	public void addCookie(Cookie arg0) {
		if (cookies == null) {
			cookies = new ArrayList();
		}
		cookies.add(arg0);
	}

	public void addDateHeader(String arg0, long arg1) {
		if (headers == null) {
			headers = new Hashtable();
		}

		headers.put(arg0, new Date(arg1));
	}

	public void addHeader(String arg0, String arg1) {
		if (headers == null) {
			headers = new Hashtable();
		}

		headers.put(arg0, arg1);
	}

	public void addIntHeader(String arg0, int arg1) {
		if (headers == null) {
			headers = new Hashtable();
		}

		headers.put(arg0, new Integer(arg1));
	}

	public boolean containsHeader(String arg0) {
		return headers.get(arg0) != null;
	}

	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	public String encodeRedirectUrl(String arg0) {
		return arg0;
	}

	public String encodeURL(String arg0) {
		return arg0;
	}

	public String encodeUrl(String arg0) {
		return arg0;
	}

	public void sendError(int arg0) throws IOException {
		status = arg0;
		if (!headersWritten) {
			writeHeaders(writer);
		}
		writer.write(generateError(arg0, null));
		writer.close();
		committed = true;
	}

	public void sendError(int arg0, String arg1) throws IOException {
		status = arg0;
		if (!headersWritten) {
			writeHeaders(writer);
		}
		writer.write(generateError(arg0, arg1));
		writer.close();
		committed = true;
	}

	public void sendRedirect(String arg0) throws IOException {
		throw new RuntimeException("Unimplemented feature.");
	}

	public void setDateHeader(String arg0, long arg1) {
		addDateHeader(arg0, arg1);
	}

	public void setHeader(String arg0, String arg1) {
		addHeader(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		addIntHeader(arg0, arg1);
	}

	public void setStatus(int arg0) {
		status = arg0;
	}

	public void setStatus(int arg0, String arg1) {
		status = arg0;
	}

	private String generateError(int arg0, String message) {
		if (message == null) {
			message = "";
		}

		String desc = generateHttpCodeDescription(arg0);
		return "<html><head><title>" + arg0 + " " + desc + "</title><body><h1>" + desc + "</h1><p>" + message + "</p><hr><address>"
				+ request.getServerName() + " at " + request.getRequestURI() + " on port " + request.getServerPort()
				+ "</address></body></html>";
	}

	/**
	 * Error descriptions taken from
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
	 * 
	 * @param arg0
	 * @return
	 */
	private static String generateHttpCodeDescription(int arg0) {
		String errorDesc = "Undefined code: " + arg0;
		// TODO Complete this table.
		switch (arg0) {
		case 100:
			errorDesc = "Continue";
			break;
		case 101:
			errorDesc = "Switching Protocols";
			break;
		case 200:
			errorDesc = "OK";
			break;
		case 201:
			errorDesc = "Created";
			break;
		case 204:
			errorDesc = "No Content";
			break;
		case 202:
			errorDesc = "Accepted";
			break;
		case 203:
			errorDesc = "Non-Authoritative Information";
			break;
		case 400:
			errorDesc = "Bad Request";
			break;
		case 401:
			errorDesc = "Unauthorized";
			break;
		case 402:
			errorDesc = "Payment Required";
			break;
		case 403:
			errorDesc = "Forbidden";
			break;
		case 404:
			errorDesc = "Not Found";
			break;
		case 405:
			errorDesc = "Method Not Allowed";
			break;
		case 406:
			errorDesc = "Not Acceptable";
			break;
		case 500:
			errorDesc = "Internal Server Error";
			break;
		case 501:
			errorDesc = "Not Implemented";
			break;
		case 502:
			errorDesc = "Bad Gateway";
			break;
		case 503:
			errorDesc = "Service Unavailable";
			break;
		case 504:
			errorDesc = "Gateway Time-out";
			break;
		}

		return errorDesc;
	}
}