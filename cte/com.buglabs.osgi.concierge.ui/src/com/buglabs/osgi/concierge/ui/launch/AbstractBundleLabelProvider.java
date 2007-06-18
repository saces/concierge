package com.buglabs.osgi.concierge.ui.launch;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractBundleLabelProvider extends LabelProvider implements ITableLabelProvider {
	
		protected abstract List getInstallList();
		protected abstract Map getStartLevelMap();
		
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			File f = new File((String) element);
			switch(columnIndex) {
			case 0:
				return f.getName();
			case 1:
				if (getInstallList() == null || !getInstallList().contains(element)) {
					return "Start";
				} else {
					return "Install";
				}
			case 2:
				//Start Level
				if(getStartLevelMap() != null && getStartLevelMap().containsKey(element)) {
					int value = Integer.parseInt(getStartLevelMap().get(element).toString());
					return new Integer(value).toString();
				} else {
					//Default start level for concierge
					return "1";
				}
			}
			return "";
		}
}
