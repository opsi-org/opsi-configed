/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utils.logging.Logging;

public class Object2Product2VersionList extends HashMap<String, HashMap<String, List<String>>> {
	public void addPackage(String depot, String productName, String versionInfo) {
		Map<String, List<String>> pVersions = computeIfAbsent(depot, s -> new HashMap<>());

		List<String> versions = pVersions.computeIfAbsent(productName, arg -> new ArrayList<>());

		if (!versions.contains(versionInfo)) {
			versions.add(versionInfo);
		}

		if (versions.size() != 1) {
			Logging.warning(this,
					"addPackage " + productName + " on depot " + depot + " has not got one version, but " + versions);
		}
	}
}
