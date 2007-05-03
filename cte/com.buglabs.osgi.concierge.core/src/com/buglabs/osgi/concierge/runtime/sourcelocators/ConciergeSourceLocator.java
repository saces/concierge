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
package com.buglabs.osgi.concierge.runtime.sourcelocators;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class ConciergeSourceLocator extends AbstractSourceLookupDirector {

	public void initializeParticipants() {
		//Theoritcally the source containers should be added by a source computer.
		addParticipants(new ISourceLookupParticipant[]{new JavaSourceLookupParticipant()});
		ArrayList sourceContainers =  new ArrayList();
		sourceContainers.addAll(Arrays.asList(getSourceContainers()));
		
		sourceContainers.add(new WorkspaceSourceContainer());
		
		List jars = getSourceJars();
		Iterator iter = jars.iterator();
		
		while(iter.hasNext()) {
			File jar = (File) iter.next();
			ExternalArchiveSourceContainer easc = new ExternalArchiveSourceContainer(jar.getAbsolutePath(), true);
			sourceContainers.add(easc);
		}
		
		setSourceContainers((ISourceContainer[]) sourceContainers.toArray(new ISourceContainer[sourceContainers.size()]));
	}

	protected List getSourceJars() {
		return ConciergeRuntime.getDefault().getConciergeJars();
	}
}
