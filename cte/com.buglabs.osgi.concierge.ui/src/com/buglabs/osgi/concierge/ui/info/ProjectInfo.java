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
 *    - Neither the name of Bug Labs nor the names of its contributors may be
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
package com.buglabs.osgi.concierge.ui.info;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class ProjectInfo {
	private String projectName;
	private String version;
	private String activator;
	private String vendor;
	private String symbolicName;
	private boolean generateActivator;
	
	public ProjectInfo(){
		version = "1.0.0";
		vendor = "";
		symbolicName = "";
		activator = "";
		projectName = "";
		generateActivator = true;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isGenerateActivator() {
		return generateActivator;
	}

	public void setGenerateActivator(boolean generateActivator) {
		this.generateActivator = generateActivator;
	}
	
	public String getActivatorName() {
		if(getActivator().equals("")) {
			return "";
		}
		
		String name = getActivator();
		int index = getActivator().lastIndexOf(".");
		
		if(index > 0 && index < name.length()) {
			name = name.substring(index + 1, name.length());
		}
		
		return name;
	}
	
	public String getActivatorPackage() {
		if(getActivator().equals("")) {
			return "";
		}
		
		String name = getActivator();
		
		int index = name.lastIndexOf(".");
		if(index > 0) {
			name = name.substring(0, index);
		}
		
		return name.toLowerCase();
	}
}
