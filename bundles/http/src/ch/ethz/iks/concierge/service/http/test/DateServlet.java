package ch.ethz.iks.concierge.service.http.test;

import java.io.IOException;
import java.util.Date;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class DateServlet implements Servlet {
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		Date today = new Date();
		res.setContentType("text/plain");

		ServletOutputStream out = res.getOutputStream();
		out.println(today.toString());
	}

	public String getServletInfo() {
		return "Returns a string representation of the current time";
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public ServletConfig getServletConfig() {
		return null;
	}

	public void init(ServletConfig config) throws ServletException {
		
	}

}
