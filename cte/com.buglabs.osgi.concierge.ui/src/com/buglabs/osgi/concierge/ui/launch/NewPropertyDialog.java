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

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Ken Gilmer - ken@buglabs.net
 *
 */
public class NewPropertyDialog extends Dialog {

	private final Map systemProps;

	private Text nameText;
	
	private Text valueText;
	
	private String name;
	
	private String value;
	
	protected NewPropertyDialog(Shell parentShell, Map systemProps) {
		super(parentShell);
		this.systemProps = systemProps;

		setShellStyle(getShellStyle() | SWT.RESIZE);
		name = new String();
		value = new String();
	}

	
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText("Name: ");
		nameText = new Text(main, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				name = nameText.getText();
			}
			
		});
		nameText.setText(name);
		
		Label valueLabel = new Label(main, SWT.NONE);
		valueLabel.setText("Value: ");
		valueText = new Text(main, SWT.BORDER);
		valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		valueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				value = valueText.getText();
			}
		});
		valueText.setText(value);
		
		
		return main;
	}


	public String getName() {
		return name;
	}


	public String getValue() {
		return value;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setValue(String value) {
		this.value = value;
	}
	
}
