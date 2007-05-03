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
package com.buglabs.osgi.concierge.core.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class ConciergeUtils {

	public static void addNatureToProject(IProject proj, String nature_id, IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = proj.getDescription();
		Vector natureIds = new Vector();
		IJavaProject jProj = JavaCore.create(proj);
		
		natureIds.add(nature_id);
		natureIds.addAll(Arrays.asList(desc.getNatureIds()));
		desc.setNatureIds((String[]) natureIds.toArray(new String[natureIds.size()]));
		
		proj.setDescription(desc, monitor);
	}

	/**
	 * Return a list of IClasspathEntry of the type specified by the type argument for 
	 * the given IJavaProject.
	 * 
	 * @param jproj
	 * @param type IClasspathEntry.CPE_XXXX
	 * @return
	 * @throws JavaModelException
	 */
	public static List getClasspathEntries(IJavaProject jproj, int  type) throws JavaModelException {
		Vector entries = new Vector();
		Vector matchingEntries = new Vector();
		entries.addAll(Arrays.asList(jproj.getResolvedClasspath(true)));

		Iterator entryIter = entries.iterator();
		
		while(entryIter.hasNext()) {
			IClasspathEntry entry = (IClasspathEntry) entryIter.next();
			if(entry.getEntryKind() == type) {
				matchingEntries.add(entry);
			}
		}
		
		return matchingEntries;
	}
}
