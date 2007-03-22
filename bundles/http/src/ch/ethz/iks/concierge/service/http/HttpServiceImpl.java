package ch.ethz.iks.concierge.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import ch.ethz.iks.concierge.service.http.test.DateServlet;
import ch.ethz.iks.concierge.service.http.test.SnoopServlet;

public final class HttpServiceImpl implements HttpService {

	/**
	 * alias -> servlet object
	 */
	private HashMap servletRegistrations = new HashMap();

	private static final HttpContext DEFAULT_HTTP_CONTEXT = new HttpContextImpl();

	private static HashMap configs = new HashMap();
	static {
		configs.put(DEFAULT_HTTP_CONTEXT, new ServletConfigImpl(
				new Hashtable(0), DEFAULT_HTTP_CONTEXT));
	}

	/*
	 * TODO: for debugging purposes only !
	 */
	public static void main(String[] args) throws IOException {
		new HttpServiceImpl();
	}

	public HttpServiceImpl() throws IOException {
		new HttpServerThread(80).start();
	}

	/**
	 * 
	 * @see org.osgi.service.http.HttpService#createDefaultHttpContext()
	 */
	public HttpContext createDefaultHttpContext() {
		return DEFAULT_HTTP_CONTEXT;
	}

	/**
	 * 
	 * @see org.osgi.service.http.HttpService#registerResources(java.lang.String,
	 *      java.lang.String, org.osgi.service.http.HttpContext)
	 */
	public void registerResources(String alias, String name, HttpContext context)
			throws NamespaceException {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @see org.osgi.service.http.HttpService#registerServlet(java.lang.String,
	 *      javax.servlet.Servlet, java.util.Dictionary,
	 *      org.osgi.service.http.HttpContext)
	 */
	public void registerServlet(String alias, Servlet servlet,
			Dictionary initparams, HttpContext context)
			throws ServletException, NamespaceException {

		if (servletRegistrations.get(alias) != null) {
			throw new NamespaceException(
					"There is already a registration for alias " + alias);
		}

		if (context == null) {
			context = DEFAULT_HTTP_CONTEXT;
		}

		ServletConfig config = (ServletConfig) configs.get(context);
		if (config == null) {
			config = new ServletConfigImpl(initparams, context);
			configs.put(context, config);
		}

		servletRegistrations.put(alias, servlet);
		servlet.init(config);
	}

	/**
	 * 
	 * @see org.osgi.service.http.HttpService#unregister(java.lang.String)
	 */
	public void unregister(String alias) {
		servletRegistrations.remove(alias);
	}

	/**
	 * 
	 * @author rjan
	 * 
	 */
	private static class ServletConfigImpl implements ServletConfig,
			ServletContext {
		private final Dictionary initparams;

		private final Dictionary attributes = new Hashtable(0);

		private HttpContext context;

		private ServletConfigImpl(Dictionary initparams, HttpContext context) {
			this.initparams = initparams;
			this.context = context;
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
		 */
		public String getInitParameter(String name) {
			return (String) initparams.get(name);
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletConfig#getInitParameterNames()
		 */
		public Enumeration getInitParameterNames() {
			return initparams.keys();
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletConfig#getServletContext()
		 */
		public ServletContext getServletContext() {
			return this;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getContext(java.lang.String)
		 */
		public ServletContext getContext(String uripath) {
			return null;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getMajorVersion()
		 */
		public int getMajorVersion() {
			// TODO Auto-generated method stub
			return 2;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getMinorVersion()
		 */
		public int getMinorVersion() {
			return 2;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
		 */
		public String getMimeType(String file) {
			return context.getMimeType(file);
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletContext#getResource(java.lang.String)
		 */
		public URL getResource(String path) throws MalformedURLException {
			return context.getResource(path);
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
		 */
		public InputStream getResourceAsStream(String path) {
			try {
				return context.getResource(path).openStream();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return null;
			}
		}

		/*
		 * 
		 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
		 */
		public RequestDispatcher getRequestDispatcher(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
		 * @deprecated
		 */
		public Servlet getServlet(String name) throws ServletException {
			return null;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getServletNames()
		 * @deprecated
		 */
		public Enumeration getServletNames() {
			return null;
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getServlets()
		 * @deprecated
		 */
		public Enumeration getServlets() {
			return null;
		}

		/*
		 * 
		 * @see javax.servlet.ServletContext#log(java.lang.String)
		 */
		public void log(String msg) {
			// TODO Auto-generated method stub

		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletContext#log(java.lang.Exception,
		 *      java.lang.String)
		 * @deprecated
		 */
		public void log(Exception exception, String msg) {
			log(msg, exception);
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#log(java.lang.String,
		 *      java.lang.Throwable)
		 */
		public void log(String message, Throwable throwable) {
			// TODO Auto-generated method stub

		}

		/*
		 * 
		 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
		 */
		public String getRealPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletContext#getServerInfo()
		 */
		public String getServerInfo() {
			return "Concierge HttpService";
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
		 */
		public Object getAttribute(String name) {
			return attributes.get(name);
		}

		/**
		 * 
		 * 
		 * @see javax.servlet.ServletContext#getAttributeNames()
		 */
		public Enumeration getAttributeNames() {
			return attributes.keys();
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
		 *      java.lang.Object)
		 */
		public void setAttribute(String name, Object object) {
			attributes.put(name, object);
		}

		/**
		 * 
		 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
		 */
		public void removeAttribute(String name) {
			attributes.remove(name);
		}

	}

	private static class HttpContextImpl implements HttpContext {

		/**
		 * 
		 * 
		 * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
		 */
		public String getMimeType(String name) {
			return null;
		}

		/**
		 * 
		 * 
		 * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
		 */
		public URL getResource(String name) {
			return getClass().getResource(name);
		}

		/**
		 * 
		 * 
		 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
		 *      javax.servlet.http.HttpServletResponse)
		 */
		public boolean handleSecurity(HttpServletRequest request,
				HttpServletResponse response) throws IOException {
			return true;
		}
	}

	private class HttpServerThread extends Thread {
		private ServerSocket serverSocket;

		private Servlet test = new DateServlet();
		private Servlet snoop = new SnoopServlet();

		private HttpServerThread(int port) throws IOException {
			serverSocket = new ServerSocket(80);
		}

		public void run() {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					HttpServletConnection scon = new HttpServletConnection(
							socket);
					// test.service(scon, scon);
					
					snoop.service(scon, scon);
					scon.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
