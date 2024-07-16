/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.connectx;

import java.io.File;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public final class SmbConnect {
	public static final List<String> directoryProducts = List.of("var", "lib", "opsi", "depot");
	public static final String PRODUCT_SHARE_RW = "opsi_depot_rw";

	public static final String DIRECTORY_PE = "winpe";
	public static final String DIRECTORY_I368 = "i386";
	public static final String DIRECTORY_INSTALL_FILES = "installfiles";

	private SmbConnect() {
	}

	public static String unixPath(String[] parts) {
		StringBuilder buf = new StringBuilder();
		if (parts != null) {
			for (int i = 0; i < parts.length - 1; i++) {
				buf.append(parts[i]);
				buf.append("/");
			}
			if (parts.length - 1 > 0) {
				buf.append(parts[parts.length - 1]);
			}
		}
		return buf.toString();
	}

	public static String buildSambaTarget(String depotserver, String share) {
		String result = "";
		Map<String, Map<String, Object>> depot2depotMap = PersistenceControllerFactory.getPersistenceController()
				.getHostInfoCollections().getDepots();

		Logging.info("buildSambaTarget for depotserver " + depotserver);

		if (depot2depotMap.get(depotserver) == null) {
			return result;
		}

		String depotRemoteUrl = (String) depot2depotMap.get(depotserver).get("depotRemoteUrl");

		if (depotRemoteUrl == null) {
			Logging.warning("buildSambaTarget, depotRemoteUrl null");
			return result;
		}

		String[] parts = depotRemoteUrl.split("/");
		String netbiosName = "";

		if (parts.length > 2) {
			netbiosName = parts[2];
			Logging.info("buildSambaTarget " + netbiosName);
		} else {
			Logging.warning("buildSambaTarget, no splitting for ", depotRemoteUrl);
		}

		result = File.separator + File.separator + netbiosName + File.separator + share;

		Logging.info("buildSambaTarget " + result);

		return result;
	}
}
