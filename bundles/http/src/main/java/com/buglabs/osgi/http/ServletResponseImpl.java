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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.buglabs.osgi.http.pub.DynamicByteBuffer;

/**
 * Basic HttpServletResponse implementation for Bug Labs HTTP Server.
 * 
 * @author ken
 * 
 */
public class ServletResponseImpl implements HttpServletResponse {
	private static final String SPACE = " ";

	private static final String CRLF = "\r\n";

	private final OutputStream outputStream;

	private boolean isBinaryResponse = false;

	private boolean committed = false;

	private String writerEncoding = "ISO-8859-1";

	private PrintWriter writer;

	private String contentType;

	private int httpStatus;

	private final HttpServletRequest request;

	private BinaryOutputBuffer binaryBuffer;

	private final Dictionary header;

	public ServletResponseImpl(OutputStream outputStream, HttpServletRequest request) {
		this.outputStream = outputStream;
		this.request = request;
		httpStatus = 200;
		header = new Hashtable();
	}

	public void flushBuffer() throws IOException {
		if (isBinaryResponse) {
			byte[] m = binaryBuffer.getBytes();
			outputStream.write(m);
			outputStream.flush();
		} else if (!isBinaryResponse && writer != null) {
			writer.flush();
		} else {
			writeHeaders(outputStream);
			outputStream.flush();
		}
	}

	public ServletOutputStream getOutputStream() throws IOException {
		isBinaryResponse = true;

		if (binaryBuffer != null) {
			// throw error, can only be called once.
			throw new IOException("Can only get output stream once.");
		}

		binaryBuffer = new BinaryOutputBuffer();

		writeHeaders(binaryBuffer);

		return binaryBuffer;
	}

	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			// throw error, can only be called once.
			throw new IOException("can only get writer once.");
		}

		writeHeaders(outputStream);
		OutputStreamWriter osw = new OutputStreamWriter(outputStream, writerEncoding);
		writer = new PrintWriter(osw, true);

		return writer;
	}

	public boolean isCommitted() {
		return committed;
	}

	/**
	 * Emit header data to output stream based on current state of Response
	 * object. Written based on specification defined here:
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
	 * 
	 * @param w
	 * @throws IOException
	 */
	private void writeHeaders(OutputStream os) throws IOException {
		StringBuffer sb = new StringBuffer();

		// Status-Line
		sb.append(request.getProtocol());
		sb.append(SPACE);
		sb.append(httpStatus);
		sb.append(SPACE);
		sb.append(generateHttpCodeDescription(httpStatus));
		sb.append(CRLF);

		// Headers
		if (contentType != null) {
			sb.append("Content-Type: ");
			sb.append(contentType);

			sb.append("; charset=");
			sb.append(writerEncoding);

			sb.append(CRLF);
		}

		Enumeration en = header.keys();
		while (en.hasMoreElements()) {
			String k = (String) en.nextElement();
			sb.append(k);
			sb.append(": ");
			String v = (String) header.get(k);
			sb.append(v);
			sb.append(CRLF);
		}

		sb.append("Connection: close");

		sb.append(CRLF);
		sb.append(CRLF);

		synchronized (os) {
			os.write(sb.toString().getBytes());
		}
	}

	public void addCookie(Cookie arg0) {
		throw new RuntimeException("This feature is not implmemented: addCookie()");
	}

	public void addDateHeader(String arg0, long arg1) {
		throw new RuntimeException("This feature is not implmemented: addDateHeader()");
	}

	public void addHeader(String arg0, String arg1) {
		throw new RuntimeException("This feature is not implmemented: addHeader()");
	}

	public void addIntHeader(String arg0, int arg1) {
		throw new RuntimeException("This feature is not implmemented: addIntHeader()");
	}

	public boolean containsHeader(String arg0) {
		throw new RuntimeException("This feature is not implmemented: addIntHeader()");
	}

	public String encodeRedirectURL(String arg0) {
		throw new RuntimeException("This feature is not implmemented: encodeRedirectURL()");
	}

	public String encodeRedirectUrl(String arg0) {
		// Deprecated per Servlet API javadoc
		return null;
	}

	public String encodeURL(String arg0) {
		throw new RuntimeException("This feature is not implmemented: encodeURL()");
	}

	public String encodeUrl(String arg0) {
		// Deprecated per Servlet API javadoc
		return null;
	}

	public void sendError(int arg0) throws IOException {
		httpStatus = arg0;

		if (writer == null) {
			writer = getWriter();
		}

		writer.println(generateError(arg0, null));
	}

	public void sendError(int arg0, String arg1) throws IOException {
		httpStatus = arg0;

		if (writer == null) {
			writer = getWriter();
		}

		writer.println(generateError(arg0, arg1));
	}

	public void sendRedirect(String arg0) throws IOException {
		throw new RuntimeException("This feature is not implmemented: sendRedirect()");
	}

	public void setDateHeader(String name, long time) {
		setHeader(name, ServletRequestImpl.headerdateformat.format(new Date(time)));
	}

	public void setHeader(String name, String value) {
		header.put(name, value);
	}

	public void setIntHeader(String name, int value) {
		header.put(name, Integer.toString(value));
	}

	public void setStatus(int status) {
		httpStatus = status;
	}

	public void setStatus(int arg0, String arg1) {
		throw new RuntimeException("This feature is not implmemented: setStatus()");
	}

	public int getBufferSize() {
		throw new RuntimeException("This feature is not implmemented: getBufferSize()");
	}

	public String getCharacterEncoding() {
		return writerEncoding;
	}

	public Locale getLocale() {
		throw new RuntimeException("This feature is not implmemented: getLocale()");
	}

	public void reset() {
		throw new RuntimeException("This feature is not implmemented: reset()");
	}

	public void resetBuffer() {
		throw new RuntimeException("This feature is not implmemented: resetBuffer()");
	}

	public void setBufferSize(int arg0) {
		throw new RuntimeException("This feature is not implmemented: setBufferSize()");
	}

	public void setContentLength(int length) {
		setIntHeader("Content-Length", length);
	}

	public void setContentType(String arg0) {
		// check to see if charset is specified

		String[] elems = HttpServer.split(arg0, ";");

		if (elems.length == 2) {
			contentType = elems[0];
			writerEncoding = elems[1];
		} else {
			contentType = elems[0];
		}
	}

	public void setLocale(Locale arg0) {
		throw new RuntimeException("This feature is not implmemented: setLocale()");
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

	/**
	 * Generate simple http error messages.
	 * 
	 * @param arg0
	 * @param message
	 * @return
	 */
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
	 * A ServelOutputStream that buffers writes until getBytes() is called.
	 * 
	 * @author ken
	 * 
	 */
	private class BinaryOutputBuffer extends ServletOutputStream {

		private DynamicByteBuffer dbb;

		public BinaryOutputBuffer() {
			dbb = new DynamicByteBuffer();
		}

		public void write(int arg0) throws IOException {
			dbb.append((byte) arg0);
		}

		public byte[] getBytes() {
			return dbb.toArray();
		}

	}
}
