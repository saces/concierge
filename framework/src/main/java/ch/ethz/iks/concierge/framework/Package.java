/* Copyright (c) 2006 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
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

package ch.ethz.iks.concierge.framework;

import java.util.List;
import java.util.StringTokenizer;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;

/**
 * The package class. Implements ExportedPackage for the PackageAdminService and
 * provides some static helper methods to parse package name and version
 * strings.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 */
final class Package implements ExportedPackage {
	/**
	 * the package name.
	 */
	final String pkg;

	/**
	 * the version. Encoded as {major, minor, patch}
	 */
	private final short[] version;

	/**
	 * the bundle that has exported this package.
	 */
	final BundleClassLoader classloader;

	/**
	 * the importing bundles. Lazily initialized.
	 */
	List importingBundles = null;

	/**
	 * is the removal of this package pending ?
	 */
	boolean removalPending = false;

	/**
	 * is the package resolved ?
	 */
	boolean resolved = false;

	/**
	 * create a new package instance.
	 * 
	 * @param packageString
	 *            the package string (packageName;
	 *            specification-version=version)
	 * @param classloader
	 *            the exporting classloader
	 * @param resolved
	 *            true, if the package is already resolved.
	 */
	Package(final String packageString, final BundleClassLoader classloader,
			final boolean resolved) {
		final String[] tokens = parsePackageString(packageString);
		this.pkg = tokens[0];
		this.version = getVersionNumber(tokens[1]);
		this.classloader = classloader;
		this.resolved = resolved;
	}

	/**
	 * get the exporting bundle.
	 * 
	 * @return the exporting bundle.
	 * @see org.osgi.service.packageadmin.ExportedPackage#getExportingBundle()
	 * @category ExportedPackage
	 */
	public Bundle getExportingBundle() {
		return classloader.bundle;
	}

	/**
	 * get all bundles that import the pacakge.
	 * 
	 * @return the array of importing bundles.
	 * @see org.osgi.service.packageadmin.ExportedPackage#getImportingBundles()
	 * @category ExportedPackage
	 */
	public Bundle[] getImportingBundles() {
		// 		return importingBundles == null ? null : (Bundle[]) importingBundles
		//		.toArray(new Bundle[importingBundles.size()]);
		if (importingBundles == null) {
			return new Bundle[] { classloader.bundle };
		} 
		Bundle[] bundles = new Bundle[importingBundles.size() + 1];
		importingBundles.toArray(bundles);
		bundles[importingBundles.size()] = classloader.bundle;
		return bundles;
		//importingBundles == null ? null : (Bundle[]) importingBundles
				//		.toArray(new Bundle[importingBundles.size()]);
	}

	/**
	 * get the package name.
	 * 
	 * @return the package name.
	 * @see org.osgi.service.packageadmin.ExportedPackage#getName()
	 * @category ExportedPackage
	 */
	public String getName() {
		return pkg;
	}

	/**
	 * get the specification version.
	 * 
	 * @return the specification version.
	 * @see org.osgi.service.packageadmin.ExportedPackage#getSpecificationVersion()
	 * @category ExportedPackage
	 */
	public String getSpecificationVersion() {
		return version == null ? null : version[0] + "." + version[1] + "."
				+ version[2];
	}

	/**
	 * is the removal of the package pending ?
	 * 
	 * @return true if the removal is pending.
	 * @see org.osgi.service.packageadmin.ExportedPackage#isRemovalPending()
	 * @category ExportedPackage
	 */
	public boolean isRemovalPending() {
		return removalPending;
	}

	/**
	 * does this package equal another package ?
	 * 
	 * @param obj
	 *            the other object.
	 * @return true, if the two objects are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @category Object
	 */
	public boolean equals(final Object obj) {
		if (obj instanceof Package) {
			final Package p = (Package) obj;
			return classloader == null ? matches(pkg, version, p.pkg, p.version)
					: obj.hashCode() == hashCode();
		}
		return false;
	}

	/**
	 * Get a string representation of this package.
	 * 
	 * @return a string representation.
	 * @see java.lang.Object#toString()
	 * @category Object
	 */
	public String toString() {
		return version == null ? pkg : pkg + "; specification-version="
				+ getSpecificationVersion() + (resolved ? "" : " (UNRESOLVED)");
	}

	/**
	 * get the hash code.
	 * 
	 * @return the hash code.
	 * @see java.lang.Object#hashCode()
	 * @category Object
	 */
	public int hashCode() {
		return pkg.hashCode();
	}

	/**
	 * parse a package string.
	 * 
	 * @param packageString
	 *            a string of the form (package;specification-version=version).
	 * @return a string array with a first element which is the package and a
	 *         second element representing the version.
	 */
	static String[] parsePackageString(final String packageString) {
		final int pos = packageString.indexOf(";");
		if (pos > -1) {
			return new String[] { packageString.substring(0, pos).trim(),
					packageString.substring(pos + 1).trim() };
		} else {
			return new String[] { packageString.trim(), "" };
		}
	}

	/**
	 * does this package match a package string ?
	 * 
	 * @param packageString
	 *            the package string (packageName;
	 *            specification-version=version)
	 * @return true, if the two objects match.
	 */
	boolean matches(final String packageString) {
		final String[] tokens = parsePackageString(packageString);
		return matches(pkg, version, tokens[0], getVersionNumber(tokens[1]));
	}

	/**
	 * check, if a pair of package names and version number arrays are matching.
	 * 
	 * @param package1
	 *            the first package string.
	 * @param version1
	 *            the first version number array.
	 * @param package2
	 *            the second pacakge string.
	 * @param version2
	 *            the second version number array.
	 * @return true, if they match.
	 */
	private static boolean matches(final String package1,
			final short[] version1, final String package2,
			final short[] version2) {
		int pos;
		if ((pos = package2.indexOf('*')) > -1) {
			if (pos == 0) {
				return true;
			}
			final String p2 = package2.substring(0, pos);
			if (!p2.endsWith(".")) {
				return false;
			} else {
				if (!package1.startsWith(p2.substring(0, p2.length() - 1))) {
					return false;
				}
			}
		} else if (!package1.equals(package2)) {
			return false;
		}

		if (version1 == null || version2 == null)
			return true;
		for (int i = 0; i < 3; i++) {
			if (version1[i] > version2[i])
				return false;
		}

		return true;
	}

	/**
	 * check, if this packages is an updated version of another package. This is
	 * the case, if the version is equal or higher and the package name is the
	 * same.
	 * 
	 * @param otherPackage
	 *            the other package.
	 * @return true, if the <i>updates</i> relationship holds.
	 */
	boolean updates(final Package otherPackage) {
		if (version == null || otherPackage.version == null) {
			return true;
		}

		for (int i = 0; i < 3; i++) {
			if (version[i] < otherPackage.version[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * check, if two package strings are matching.
	 * 
	 * @param packageString1
	 *            the first package string.
	 * @param packageString2
	 *            the second package string.
	 * @return true, if they match.
	 */
	static boolean matches(final String packageString1,
			final String packageString2) {
		final String[] one = parsePackageString(packageString1);
		final String[] two = parsePackageString(packageString2);
		return matches(one[0], getVersionNumber(one[1]), two[0],
				getVersionNumber(two[1]));
	}

	/**
	 * get the version number array from a version string.
	 * 
	 * @param version
	 *            the version part of the package string.
	 * @return the version number array.
	 * TODO: optimize!!!
	 */
	private static short[] getVersionNumber(final String version) {
		if (version.startsWith("specification-version=")) {
			String versionString = version.substring(22).trim();
			if (versionString.startsWith("\"")) {
				versionString = versionString.substring(1);
			}
			if (versionString.endsWith("\"")) {
				versionString = versionString.substring(0, versionString
						.length() - 1);
			}
			StringTokenizer tokenizer = new StringTokenizer(versionString, ".");
			short[] versionNumber = { 0, 0, 0 };
			for (int i = 0; tokenizer.hasMoreTokens() && i <= 2; i++) {
				versionNumber[i] = Short.parseShort(tokenizer.nextToken());
			}

			return versionNumber;
		} else {
			return null;
		}
	}

}
