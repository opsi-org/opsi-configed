package de.uib.opsidatamodel;

import java.util.ArrayList;
import java.util.List;

import de.uib.utilities.logging.Logging;

public class Object2Product2VersionList extends java.util.HashMap<String, Product2VersionList> {
	public void addPackage(String depot, String productName, String versionInfo) {
		Product2VersionList pVersions = get(depot);
		if (pVersions == null) {
			pVersions = new Product2VersionList();
			put(depot, pVersions);
		}

		List<String> versions = pVersions.computeIfAbsent(productName, arg -> new ArrayList<>());

		if (!versions.contains(versionInfo))
			versions.add(versionInfo);

		if (versions.size() != 1) {
			Logging.warning(this,
					"addPackage " + productName + " on depot " + depot + " has not got one version, but " + versions);
		}
	}
}
