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
package com.buglabs.osgi.concierge.devtools.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.buglabs.osgi.concierge.core.BundleModelManager;
import com.buglabs.osgi.concierge.core.OSGiCore;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class BundleModelManagerView extends ViewPart {

	public BundleModelManagerView() {
		
	}

	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new FillLayout());
		
		ListViewer viewer = new ListViewer(top);
		viewer.setContentProvider(new IStructuredContentProvider(){

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}

			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof BundleModelManager) {
					return ((BundleModelManager) inputElement).getProjects().toArray();
				}
				
				return new Object[0];
			}});
		
		viewer.setLabelProvider(new LabelProvider(){
			public String getText(Object element) {
				if(element instanceof IAdaptable) {
					IProject proj = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
					if(proj != null) {
						return proj.getName();
					}
				}
				
				return null;
			}
		});
		
		viewer.setInput(OSGiCore.getDefault().getBundleModelManager());
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
