package com.buglabs.osgi.concierge.core.builder;

import java.io.IOException;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.osgi.framework.Version;

/**
 * Implementation of {@link ExportPackageDescription}
 * @author akravets
 *
 */
public class ExportPackageDescriptionImpl implements ExportPackageDescription{

	private Manifest manifest;
	private String name;
	private String version;

	public ExportPackageDescriptionImpl(String name, String version){
		this.name = name;
		this.version = version;
	}
	
	public ExportPackageDescriptionImpl(IClasspathEntry classpath) throws ZipException, IOException{
		JarFile jarFile = new JarFile(classpath.getPath().toFile());
		manifest = jarFile.getManifest();

	}
	
	public Map getAttributes() {
		if(manifest != null)
			return manifest.getMainAttributes();
		return null;
	}

	public Object getDirective(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getDirectives() {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleDescription getExporter() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		if(this.name != null){
			return this.name;
		}
		return manifest.getMainAttributes().getValue("Bundle-Name");
	}

	public BundleDescription getSupplier() {
		return new BundleDescriptionImpl();
	}

	public Version getVersion() {
		if(this.version != null){
			return new Version(version);
		}
		return new Version(manifest.getMainAttributes().getValue("Manifest-Version"));
	}

}