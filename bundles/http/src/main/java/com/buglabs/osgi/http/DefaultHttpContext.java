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
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

/**
 * A default HttpContext implementation. Refer to OSGi spec R3 Section 14.
 * 
 * @author ken
 * 
 */
public class DefaultHttpContext implements HttpContext {

    private final String DEFAULT_MIME_TYPE = "text/plain";

    /*
         * (non-Javadoc)
         * 
         * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
         */
    public String getMimeType(String name) {
	// This set of guesses provided by OSGi R3 Spec sectino 14.6

	String extension = getExtension(name);

	if (extension == null || extension.length() == 0) {
	    return DEFAULT_MIME_TYPE;
	}

	if (extension.equals("jpg") || extension.equals("jpeg")) {
	    return "image/jpeg";
	}

	if (extension.equals("gif")) {
	    return "image/gif";
	}

	if (extension.equals("css")) {
	    return "text/css";
	}

	if (extension.equals("txt")) {
	    return "text/plain";
	}

	if (extension.equals("wml")) {
	    return "text/vnd.wap.wml";
	}

	if (extension.equals("htm") || extension.equals("html")) {
	    return "txt/html";
	}

	if (extension.equals("wbmp")) {
	    return "image/vnd.wap.wbmp";
	}
	return DEFAULT_MIME_TYPE;
    }

    /**
         * Return extenstion section of a file path.
         * 
         * @param name
         * @return all characters after last '.' character in string in
         *         lowercase, or null.
         */
    private String getExtension(String name) {
	int pos = name.lastIndexOf('.');
	if (pos > 0 && pos < name.length()) {
	    return name.substring(pos + 1).toLowerCase();
	}

	return null;
    }

    public URL getResource(String name) {
	// TODO implement

	throw new RuntimeException("Unimplemented feature: getResource().");
    }

    public boolean handleSecurity(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
	return true;
    }

}
