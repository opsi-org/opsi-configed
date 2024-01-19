/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import de.uib.configed.type.licenses.LicenseContractEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public final class LicenseData {
	private static List<String> licenses = new ArrayList<>();
	private static List<String> activeLicenses = new ArrayList<>();
	private static List<String> expiredLicenses = new ArrayList<>();

	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private LicenseData() {
	}

	public static List<String> getLicenses() {
		return new ArrayList<>(licenses);
	}

	private static void retrieveLicenses() {
		if (!licenses.isEmpty()) {
			return;
		}

		Map<String, LicenseContractEntry> licenseContracts = persistenceController.getLicenseDataService()
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

		NavigableMap<String, NavigableSet<String>> expiredLicenseContracts = persistenceController
				.getLicenseDataService().getLicenseContractsToNotifyPD();

		if (expiredLicenseContracts.isEmpty()) {
			return;
		}

		for (Entry<String, NavigableSet<String>> entry : expiredLicenseContracts.entrySet()) {
			NavigableSet<String> expiredLicenseContractSet = entry.getValue();

			for (String expiredLicense : expiredLicenseContractSet) {
				expiredLicenses.add(expiredLicense);
			}
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
