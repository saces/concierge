/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer, IBM Research - initial API and implementation
 *******************************************************************************/
package org.eclipse.concierge.compat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.concierge.BundleImpl.Revision;
import org.eclipse.concierge.Concierge;
import org.eclipse.concierge.ConciergeCollections.ParseResult;
import org.eclipse.concierge.ConciergeCollections.Tuple;
import org.eclipse.concierge.Resources.BundleCapabilityImpl;
import org.eclipse.concierge.Resources.BundleRequirementImpl;
import org.eclipse.concierge.Utils;
import org.eclipse.concierge.compat.LegacyBundleProcessing;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.resource.Namespace;

public class BundleManifestOne implements LegacyBundleProcessing {

	private static final String SPLIT_AT_COMMA = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	private static final String SPLIT_AT_SEMICOLON = ";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	private static final Map<String, Object> NO_ATTRS = new HashMap<String, Object>();

	public Tuple<List<BundleCapability>, List<BundleRequirement>> processManifest(
			final Revision revision, final Manifest manifest)
			throws BundleException {
		final Attributes mfAttrs = manifest.getMainAttributes();

		final List<BundleCapability> caps = new ArrayList<BundleCapability>();
		final List<BundleRequirement> reqs = new ArrayList<BundleRequirement>();

		// package namespace
		{
			// package imports
			final String importStr = mfAttrs.getValue(Constants.IMPORT_PACKAGE);
			if (importStr != null) {
				final String[] imports = importStr.split(SPLIT_AT_COMMA);
				for (int i = 0; i < imports.length; i++) {
					final String[] literals = imports[i]
							.split(SPLIT_AT_SEMICOLON);

					final ParseResult parseResult = Utils.parseLiterals(
							literals, 1);
					final HashMap<String, String> dirs = parseResult
							.getDirectives();
					dirs.put(Namespace.REQUIREMENT_FILTER_DIRECTIVE, Utils
							.createFilter(PackageNamespace.PACKAGE_NAMESPACE,
									literals[0], parseResult.getAttributes()));

					dirs.put(Concierge.DIR_INTERNAL, literals[0].trim());

					reqs.add(new BundleRequirementImpl(revision,
							PackageNamespace.PACKAGE_NAMESPACE, dirs, null,
							Constants.IMPORT_PACKAGE + ' ' + imports[i]));
				}
			}

			// add implicit import for org.osgi.framework

			final HashMap<String, String> dirs = new HashMap<String, String>();
			dirs.put(Namespace.REQUIREMENT_FILTER_DIRECTIVE, Utils
					.createFilter(PackageNamespace.PACKAGE_NAMESPACE,
							"org.osgi.framework", NO_ATTRS));

			reqs.add(new BundleRequirementImpl(revision,
					PackageNamespace.PACKAGE_NAMESPACE, dirs, null,
					Constants.IMPORT_PACKAGE + ' ' + "org.osgi.framework"));

		}

		{
			// dynamic imports
			final String dynImportStr = mfAttrs
					.getValue(Constants.DYNAMICIMPORT_PACKAGE);
			if (dynImportStr != null) {
				final String[] dynImports = dynImportStr.split(SPLIT_AT_COMMA);
				for (int i = 0; i < dynImports.length; i++) {
					final String[] literals = dynImports[i]
							.split(SPLIT_AT_SEMICOLON);

					final ParseResult parseResult = Utils.parseLiterals(
							literals, 1);
					final HashMap<String, String> dirs = parseResult
							.getDirectives();
					final HashMap<String, Object> attrs = parseResult
							.getAttributes();

					dirs.put(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE,
							PackageNamespace.RESOLUTION_DYNAMIC);

					dirs.put(Concierge.DIR_INTERNAL, literals[0].trim());

					if (literals[0].contains("*")) {
						dirs.put(
								PackageNamespace.REQUIREMENT_CARDINALITY_DIRECTIVE,
								PackageNamespace.CARDINALITY_MULTIPLE);
					}

					reqs.add(new BundleRequirementImpl(revision,
							PackageNamespace.PACKAGE_NAMESPACE, dirs, attrs,
							Constants.DYNAMICIMPORT_PACKAGE + ' '
									+ dynImports[i]));
				}
			}
		}

		{
			// package exports
			final String exportStr = mfAttrs.getValue(Constants.EXPORT_PACKAGE);
			if (exportStr != null) {
				final String[] exports = exportStr.split(SPLIT_AT_COMMA);
				for (int i = 0; i < exports.length; i++) {
					final String[] literals = exports[i]
							.split(SPLIT_AT_SEMICOLON);

					final ParseResult parseResult = Utils.parseLiterals(
							literals, 1);
					final HashMap<String, Object> attrs = parseResult
							.getAttributes();

					attrs.put(PackageNamespace.PACKAGE_NAMESPACE, literals[0].trim());

					caps.add(new BundleCapabilityImpl(revision,
							PackageNamespace.PACKAGE_NAMESPACE, parseResult
									.getDirectives(), attrs,
							Constants.EXPORT_PACKAGE + ' ' + exports[i]));
				}
			}
		}

		return new Tuple<List<BundleCapability>, List<BundleRequirement>>(caps,
				reqs);
	}

	public List<BundleCapability> translateToCapability(
			final Concierge concierge, final String attributeName,
			final String valueStr) {
		throw new UnsupportedOperationException();
	}

}
