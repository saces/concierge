package ch.ethz.iks.concierge.service.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

class HttpServletConnection implements HttpServletRequest, HttpServletResponse {

	private final Socket socket;

	private ServletInputStream in;

	private BufferedReader reader;

	private final Dictionary requestHeader = new Hashtable(0);

	private final Cookie[] cookies;

	private final Dictionary attributes = new Hashtable(0);

	private final Dictionary parameter;

	private final String method;

	private final String res;

	private final String queryString;

	private final String protocol;

	private ServletOutputStream out;

	private PrintWriter writer;

	private final Dictionary responseHeader = new Hashtable(0);

	private final ArrayList responseCookies = new ArrayList(0);

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	private int status;

	private boolean committed;

	HttpServletConnection(final Socket socket) throws IOException {
		this.socket = socket;

		// parse HTTP header
		final DataInputStream dis = new DataInputStream(socket.getInputStream());
		String line = dis.readLine();
		String[] request = tokenize(line, " ");
		method = request[0];
		res = request[1];
		queryString = res.indexOf("?") > -1 ? tokenize(res, "?")[1] : null;
		parameter = queryString == null ? null : HttpUtils
				.parseQueryString(queryString);
		protocol = request[2];
		while (!(line = dis.readLine()).equals("")) {
			System.out.println(line);
			final String[] tokens = tokenize(line, ":");
			requestHeader.put(tokens[0], tokens[1]);
		}
		
		// get cookies
		cookies = null;
		
		// get session information
		// TODO: get the cookies.
		// "JSESSIONID";
	}

	private static String[] tokenize(String str, String delim) {
		final ArrayList result = new ArrayList();
		int last = -1;
		int pos = -1;
		while ((pos = str.indexOf(delim, ++last)) > -1) {
			result.add(str.substring(last, pos));
			last = pos;
		}
		result.add(str.substring(last));
		return (String[]) result.toArray(new String[result.size()]);
	}

	void commit() throws IOException {
		committed = true;
		if (status == 0) {
			status = 200;
		}
		DataOutputStream socketOut = new DataOutputStream(socket
				.getOutputStream());
		socketOut.writeBytes(protocol + " " + Integer.toString(status) + " "
				+ getStatus(status) + "\n");
		for (Enumeration keys = responseHeader.keys(); keys.hasMoreElements();) {
			final String key = (String) keys.nextElement();
			final Object value = responseHeader.get(key);
			socketOut.writeBytes(key + ": " + value + "\n");
		}

		// process cookies
		if (!responseCookies.isEmpty()) {
			Cookie[] cookies = (Cookie[]) responseCookies
					.toArray(new Cookie[responseCookies.size()]);
			StringBuffer buffer = new StringBuffer();

			for (int i = 0; i < cookies.length; i++) {
				String value;
				buffer.append(cookies[i].getName());
				buffer.append("=");
				buffer.append(cookies[i].getValue());
				buffer.append("; Version=" + cookies[i].getVersion());
				if ((value = cookies[i].getPath()) != null) {
					buffer.append("; Path=" + value);
				}
				if ((value = cookies[i].getDomain()) != null) {
					buffer.append("; Domain=" + value);
				}
			}
			responseHeader.put("Set-Cookie2", buffer.toString());
		}
		socketOut.writeBytes("\n");
		writer.flush();
		socketOut.write(buffer.toByteArray());
		socketOut.close();

		System.out.println("RESPONSE");
		System.out.println(buffer.toString());
	}

	private static String getStatus(final int status) {
		switch (status) {

		case SC_CONTINUE:
			return "Continue";
		case SC_SWITCHING_PROTOCOLS:
			return "Switching Protocols";
		case SC_OK:
			return "OK";
		case SC_CREATED:
			return "Created";
		case SC_ACCEPTED:
			return "Accepted";
		case SC_NON_AUTHORITATIVE_INFORMATION:
			return "Non-Authoritative Information";
		case SC_NO_CONTENT:
			return "No Content";
		case SC_RESET_CONTENT:
			return "Reset Content";
		case SC_PARTIAL_CONTENT:
			return "Partial Content";
		case SC_MULTIPLE_CHOICES:
			return "Multiple Choices";
		case SC_MOVED_PERMANENTLY:
			return "Moved Permanently";
		case SC_MOVED_TEMPORARILY:
			return "Moved Temporarily";
		case SC_SEE_OTHER:
			return "See Other";
		case SC_NOT_MODIFIED:
			return "Not Modified";
		case SC_USE_PROXY:
			return "Use Proxy";
		case SC_BAD_REQUEST:
			return "Bad Request";
		case SC_UNAUTHORIZED:
			return "Unauthorized";
		case SC_PAYMENT_REQUIRED:
			return "Payment Required";
		case SC_FORBIDDEN:
			return "Forbidden";
		case SC_NOT_FOUND:
			return "Not Found";
		case SC_METHOD_NOT_ALLOWED:
			return "Method Not Allowed";
		case SC_NOT_ACCEPTABLE:
			return "Not Acceptable";
		case SC_PROXY_AUTHENTICATION_REQUIRED:
			return "Proxy Authentication Required";
		case SC_REQUEST_TIMEOUT:
			return "Request Time-out";
		case SC_CONFLICT:
			return "Conflict";
		case SC_GONE:
			return "Gone";
		case SC_LENGTH_REQUIRED:
			return "Length Required";
		case SC_PRECONDITION_FAILED:
			return "Precondition Failed";
		case SC_REQUEST_ENTITY_TOO_LARGE:
			return "Request Entity Too Large";
		case SC_REQUEST_URI_TOO_LONG:
			return "Request-URI Too Large";
		case SC_UNSUPPORTED_MEDIA_TYPE:
			return "Unsupported Media Type";
		case SC_INTERNAL_SERVER_ERROR:
			return "Internal Server Error";
		case SC_NOT_IMPLEMENTED:
			return "Not Implemented";
		case SC_BAD_GATEWAY:
			return "Bad Gateway";
		case SC_SERVICE_UNAVAILABLE:
			return "Service Unavailable";
		case SC_GATEWAY_TIMEOUT:
			return "Gateway Time-out";
		case SC_HTTP_VERSION_NOT_SUPPORTED:
			return "HTTP Version not supported";
		default:
			return "";
		}

	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 * @category HttpServletRequest
	 */
	public long getDateHeader(String name) {
		String value = (String) requestHeader.get(name);
		return value == null ? -1 : Date.parse(value);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 * @category HttpServletRequest
	 */
	public String getHeader(String name) {
		return (String) requestHeader.get(name);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 * @category HttpServletRequest
	 */
	public Enumeration getHeaderNames() {
		return requestHeader.keys();
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 * @category HttpServletRequest
	 */
	public int getIntHeader(String name) {
		String value = (String) requestHeader.get(name);
		return value == null ? -1 : Integer.parseInt(value);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 * @category HttpServletRequest
	 */
	public String getMethod() {
		return method;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 * @category HttpServletRequest
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 * @category HttpServletRequest
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 * @category ServletRequest
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 * @category ServletRequest
	 */
	public Enumeration getAttributeNames() {
		return attributes.keys();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 * @category ServletRequest
	 */
	public String getCharacterEncoding() {
		return getHeader("Character-Encoding");
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getContentLength()
	 * @category ServletRequest
	 */
	public int getContentLength() {
		return getIntHeader("Content-Length");
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getContentType()
	 * @category ServletRequest
	 */
	public String getContentType() {
		return getHeader("Content-Type");
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getInputStream()
	 * @category ServletRequest
	 */
	public ServletInputStream getInputStream() throws IOException {
		if (reader != null) {
			throw new IllegalStateException(
					"getReader has already been called. Cannot get a Reader and an InputStream simultaneously");
		}
		if (in == null) {
			final InputStream socketIn = socket.getInputStream();
			in = new ServletInputStream() {
				public int read() throws IOException {
					return socketIn.read();
				}
			};
		}
		return in;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 * @category ServletRequest
	 */
	public String getParameter(String name) {
		if (parameter == null) {
			return null;
		}
		Object result = parameter.get(name);
		if (result == null) {
			return null;
		} else if (result instanceof String) {
			return (String) result;
		} else if (result instanceof String[]) {
			return ((String[]) result)[0];
		}
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 * @category ServletRequest
	 */
	public Enumeration getParameterNames() {
		return parameter == null ? new Vector(0).elements() : parameter.keys();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 * @category ServletRequest
	 */
	public String[] getParameterValues(String name) {
		if (parameter == null) {
			return null;
		}
		Object result = parameter.get(name);
		if (result == null) {
			return null;
		} else if (result instanceof String) {
			return new String[] { (String) result };
		} else if (result instanceof String[]) {
			return (String[]) result;
		}
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getProtocol()
	 * @category ServletRequest
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getReader()
	 * @category ServletRequest
	 */
	public BufferedReader getReader() throws IOException {
		if (in != null) {
			throw new IllegalStateException(
					"getInputStream has already been called. Cannot get a Reader and an InputStream simultaneously");
		}
		if (reader == null) {
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
		}
		return reader;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 * @deprecated
	 */
	public String getRealPath(String path) {
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 * @category ServletRequest
	 */
	public String getRemoteAddr() {
		return socket.getInetAddress().getHostAddress();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 * @category ServletRequest
	 */
	public String getRemoteHost() {
		return socket.getInetAddress().getHostName();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getScheme()
	 * @category ServletRequest
	 */
	public String getScheme() {
		// TODO check, if secure
		return "http";
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getServerName()
	 * @category ServletRequest
	 */
	public String getServerName() {
		return socket.getLocalAddress().getHostName();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#getServerPort()
	 * @category ServletRequest
	 */
	public int getServerPort() {
		return socket.getLocalPort();
	}

	/**
	 * 
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 * @category ServletRequest
	 */
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	/*
	 * HttpServletResponse methods
	 */

	/*
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie cookie) {
		responseCookies.add(cookie);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String name) {
		return responseHeader.get(name) != null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 * @deprecated
	 */
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int,
	 *      java.lang.String)
	 * @category HttpServletResponse
	 */
	public void sendError(int sc, String msg) throws IOException {
		if (committed)
			throw new IllegalStateException("Response is committed");
		status = sc;
		buffer.reset();
		ServletOutputStream out = getOutputStream();
		out.println("<html><body><h1>" + getStatus(sc) + "</h1>" + msg
				+ "</body></html>");
		committed = true;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 * @category HttpServletResponse
	 */
	public void sendError(int sc) throws IOException {
		if (committed)
			throw new IllegalStateException("Response is committed");
		status = sc;
		buffer.reset();
		ServletOutputStream out = getOutputStream();
		out.println("<html><body><h1>" + getStatus(sc) + "</h1></body></html>");
		committed = true;
	}

	/*
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String location) throws IOException {
		if (committed)
			throw new IllegalStateException("Response is committed");

	}

	/**
	 * TODO: check, if this is correct !!!
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
	 *      long)
	 * @category HttpServletResponse
	 */
	public void setDateHeader(String name, long date) {
		responseHeader.put(name, new Date(date).toString());
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
	 *      java.lang.String)
	 * @category HttpServletResponse
	 */
	public void setHeader(String name, String value) {
		responseHeader.put(name, value);
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
	 *      int)
	 * @category HttpServletResponse
	 */
	public void setIntHeader(String name, int value) {
		responseHeader.put(name, Integer.toString(value));
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 * @category HttpServletResponse
	 */
	public void setStatus(int sc) {
		status = sc;
	}

	/**
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int,
	 *      java.lang.String)
	 * @deprecated
	 */
	public void setStatus(int sc, String sm) {
		status = sc;
	}

	/**
	 * 
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 * @category ServletResponse
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter already called.");
		}
		if (out == null) {
			out = new ServletOutputStream() {
				public void write(int b) throws IOException {
					buffer.write(b);
				}
			};
		}
		return out;
	}

	/**
	 * 
	 * @see javax.servlet.ServletResponse#getWriter()
	 * @category ServletResponse
	 */
	public PrintWriter getWriter() throws IOException {
		if (out != null) {
			throw new IllegalStateException("getWriter already called.");
		}
		if (writer == null) {
			// TODO: use charset
			writer = new PrintWriter(new OutputStreamWriter(buffer), true);
		}
		return writer;
	}

	/**
	 * 
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 * @category ServletResponse
	 */
	public void setContentLength(int len) {
		responseHeader.put("Content-Length", Integer.toString(len));
	}

	/**
	 * 
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 * @category ServletResponse
	 */
	public void setContentType(String type) {
		responseHeader.put("Content-Type", type);
	}

}