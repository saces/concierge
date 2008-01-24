package com.buglabs.osgi.concierge.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.buglabs.osgi.concierge.core.utils.ManifestUtils;

/**
 * 
 * @author Angel Roman - roman@mdesystems.com
 *
 */
public class TestManifestParsing extends TestCase {
	
	public void testManifestExists() throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("TestInput/MANIFEST.MF"), null);
		URL fileURL = FileLocator.toFileURL(url);
		File f = new File(fileURL.getFile());
		assertTrue(f.exists());
	}
	
	public void testGetBundleClasspath() throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("TestInput/MANIFEST.MF"), null);
		URL fileURL = FileLocator.toFileURL(url);
		File f = new File(fileURL.getFile());
		assertTrue(f.exists());
		
		List cpElements = ManifestUtils.getBundleClassPath(new FileInputStream(f));
		
		assertEquals(3, cpElements.size());
		
		assertTrue(cpElements.contains("swt.jar"));
		assertTrue(cpElements.contains("draw2d.jar"));
		assertTrue(cpElements.contains("."));
	}
	
	public void testGetImportPackages() throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("TestInput/MANIFEST.MF"), null);
		URL fileURL = FileLocator.toFileURL(url);
		File f = new File(fileURL.getFile());
		assertTrue(f.exists());
		
		List packages = ManifestUtils.getImportedPackages(new FileInputStream(f));
		
		assertEquals(4, packages.size());
		
		assertTrue(packages.contains("com.buglabs.osgi.concierge.jdt"));
		assertTrue(packages.contains("com.buglabs.osgi.concierge.runtime"));
		assertTrue(packages.contains("javax.servlet;version=\"2.3.0\""));
		assertTrue(packages.contains("com.ibm.icu.text"));
	}
	
	public void testGetExportedPackages() throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("TestInput/MANIFEST.MF"), null);
		URL fileURL = FileLocator.toFileURL(url);
		File f = new File(fileURL.getFile());
		assertTrue(f.exists());
		
		List packages = ManifestUtils.getExportedPackages(new FileInputStream(f));
		
		assertEquals(2, packages.size());
		
		assertTrue(packages.contains("proja"));
		assertTrue(packages.contains("proja.pkg2"));
	}
}
