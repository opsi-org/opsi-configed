/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.gui.type.licenses.LicenseContractEntry;

public final class LicenseData {
	private static List<String> licenses = new ArrayList<>();
	private static List<String> activeLicenses = new ArrayList<>();
	private static List<String> expiredLicenses = new ArrayList<>();

	private static OpsiServiceNOMPersistenceController persistenceController;

	private LicenseData() {
	}

	public static void initData(OpsiServiceNOMPersistenceController persistenceController) {
		LicenseData.persistenceController = persistenceController;
	}

	public static List<String> getLicenses() {
		return new ArrayList<>(licenses);
	}

	private static void retrieveLicenses() {
		if (!licenses.isEmpty()) {
			return;
		}

		Map<String, LicenseContractEntry> licenseContracts = persistenceController.getDataServices().license
				.getLicenseContractsPD();

		if (licenseContracts.isEmpty()) {
			return;
		}

		licenses.clear();
		licenses = licenseContracts.values().stream().map(v -> v.get("licenseContractId")).collect(Collectors.toList());
	}

	public static List<String> getActiveLicenses() {
		return new ArrayList<>(activeLicenses);
	}

	private static void retrieveActiveLicenses() {
		if (!activeLicenses.isEmpty()) {
			return;
		}

		activeLicenses = new ArrayList<>(licenses);
		activeLicenses.removeAll(expiredLicenses);
	}

	public static List<String> getExpiredLicenses() {
		return new ArrayList<>(expiredLicenses);
	}

	private static void retrieveExpiredLicenses() {
		if (!expiredLicenses.isEmpty()) {
			return;
		}

		expiredLicenses.clear();

		Map<String, Set<String>> expiredLicenseContracts = persistenceController.getDataServices().license
				.getLicenseContractsToNotifyPD();

		if (expiredLicenseContracts.isEmpty()) {
			return;
		}

		for (Entry<String, Set<String>> entry : expiredLicenseContracts.entrySet()) {
			expiredLicenses.addAll(entry.getValue());
		}
	}

	public static void clear() {
		licenses.clear();
		activeLicenses.clear();
		expiredLicenses.clear();
	}

	public static void retrieveData() {
		retrieveLicenses();
		retrieveExpiredLicenses();
		retrieveActiveLicenses();
	}
}
