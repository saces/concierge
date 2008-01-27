package com.buglabs.osgi.concierge.core.builder;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.GenericDescription;
import org.eclipse.osgi.service.resolver.GenericSpecification;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Version;

/**
 * Implementation of {@link BundleDescription}
 * @author akravets
 *
 */
public class BundleDescriptionImpl implements BundleDescription{

	public boolean attachFragments() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean dynamicFragments() {
		// TODO Auto-generated method stub
		return false;
	}

	public long getBundleId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public State getContainingState() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleDescription[] getDependents() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getExecutionEnvironments() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExportPackageDescription[] getExportPackages() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleDescription[] getFragments() {
		// TODO Auto-generated method stub
		return null;
	}

	public GenericDescription[] getGenericCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	public GenericSpecification[] getGenericRequires() {
		// TODO Auto-generated method stub
		return null;
	}

	public HostSpecification getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImportPackageSpecification[] getImportPackages() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPlatformFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleSpecification[] getRequiredBundles() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExportPackageDescription[] getResolvedImports() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleDescription[] getResolvedRequires() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExportPackageDescription[] getSelectedExports() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSymbolicName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getUserObject() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasDynamicImports() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRemovalPending() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isResolved() {
		return true;
	}

	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setUserObject(Object userObject) {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleDescription getSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	public Version getVersion() {
		// TODO Auto-generated method stub
		return null;
	}
}