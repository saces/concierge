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
package com.buglabs.osgi.concierge.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 * @author Angel Roman
 *
 */
public abstract class AbstractBundleCellModifier implements ICellModifier {
		private BundleTab tab;
		public AbstractBundleCellModifier(BundleTab tab) {
			this.tab = tab;
		}
		
		protected abstract List getInstallList();
		protected abstract void setInstallList(List list);
		protected abstract Map getStartMap();
		protected abstract void setStartMap(Map map);
		protected abstract Viewer getViewer();
		
		public boolean canModify(Object element, String property) {
			if(property.equals(BundleTab.NAME)) {
				return false;
			}

			return true;
		}

		public Object getValue(Object element, String property) {
			if(property.equals(BundleTab.NAME)) {
				return element;
			} else if(property.equals(BundleTab.INITIAL_STATE)) {
				if(getInstallList().contains(element)) {
					return BundleTab.INITIAL_STATE_INSTALL;
				} else {
					return BundleTab.INITIAL_STATE_START;
				}
			} else if(property.equals(BundleTab.START_LEVEL)) {
				Object value = getStartMap().get(element);
				if(value == null) {
					//Default concierge start level
					//the position in the combo box
					value = new Integer("0");
					return value;
				}
				return new Integer(Integer.parseInt((String)value) - 1);
			}
			return null;
		}

		public void modify(Object element, String property, Object value) {
			Object data = ((TableItem)element).getData();
			if(property.equals(BundleTab.INITIAL_STATE)) {
				if(value.equals(BundleTab.INITIAL_STATE_INSTALL)) {
					if(!getInstallList().contains(data)) {
						// Workaround to refresh buttons
						ArrayList tmpList = new ArrayList();
						tmpList.addAll(getInstallList());
						tmpList.add(data);
						setInstallList(tmpList);
					}
				} else {
					// Workaround to refresh buttons
					ArrayList tmpList = new ArrayList();
					tmpList.addAll(getInstallList());
					tmpList.remove(data);
					setInstallList(tmpList);	
				}
			} else if(property.equals(BundleTab.START_LEVEL)) {
				HashMap tempMap = new HashMap();
				tempMap.putAll(getStartMap());
				getStartMap().clear();
				tempMap.remove(data);
				tempMap.put(data, "" + new Integer(((Integer)value).intValue() + 1).toString());
				setStartMap(tempMap);
			}
			getViewer().refresh();
			tab.refreshDialog();
		}
	}