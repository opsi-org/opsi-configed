package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import de.uib.configed.type.licences.LicenceContractEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public class LicenseData {
	private static List<String> licenses = new ArrayList<>();
	private static List<String> activeLicenses = new ArrayList<>();
	private static List<String> expiredLicenses = new ArrayList<>();

	private static AbstractPersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	private LicenseData() {
	}

	public static List<String> getLicenses() {
		return new ArrayList<>(licenses);
	}

	private static void retrieveLicenses() {
		if (!licenses.isEmpty()) {
			return;
		}

		Map<String, LicenceContractEntry> licenceContracts = persist.getLicenceContracts();

		if (licenceContracts.isEmpty()) {
			return;
		}

		licenses.clear();
		licenses = licenceContracts.values().stream().map(v -> v.get("licenseContractId")).collect(Collectors.toList());
	}

	public static List<String> getActiveLicenses() {
		return new ArrayList<>(activeLicenses);
	}

	private static void retrieveActiveLicenses() {
		if (!activeLicenses.isEmpty()) {
			return;
		}

		activeLicenses.clear();

		for (String licence : licenses) {
			if (!expiredLicenses.contains(licence)) {
				activeLicenses.add(licence);
			}
		}
	}

	public static List<String> getExpiredLicenses() {
		return new ArrayList<>(expiredLicenses);
	}

	private static void retrieveExpiredLicenses() {
		if (!expiredLicenses.isEmpty()) {
			return;
		}

		expiredLicenses.clear();

		NavigableMap<String, NavigableSet<String>> expiredLicenceContracts = persist.getLicenceContractsExpired();

		if (expiredLicenceContracts.isEmpty()) {
			return;
		}

		for (Map.Entry<String, NavigableSet<String>> entry : expiredLicenceContracts.entrySet()) {
			NavigableSet<String> expiredLicenceContractSet = entry.getValue();

			for (String expiredLicence : expiredLicenceContractSet) {
				expiredLicenses.add(expiredLicence);
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
