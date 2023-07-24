/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public final class ClientData {
	private static Map<String, List<Client>> clients = new HashMap<>();
	private static Map<String, List<String>> reachableClients = new HashMap<>();
	private static Map<String, List<String>> unreachableClients = new HashMap<>();
	private static Map<String, Map<String, Integer>> clientLastSeen = new HashMap<>();

	private static String selectedDepot;

	private static OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ClientData() {
	}

	public static List<Client> getClients() {
		if (clients.isEmpty() || !clients.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(clients.get(selectedDepot));
	}

	private static void retrieveClients() {
		if (!clients.isEmpty()) {
			return;
		}

		clients.clear();

		Map<String, HostInfo> mapOfAllPCInfoMaps = persistenceController.getHostInfoCollections()
				.getMapOfAllPCInfoMaps();

		List<String> depots = new ArrayList<>(persistenceController.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			List<Client> clientsList = new ArrayList<>();

			for (Map.Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
				HostInfo hostInfo = entry.getValue();

				if (hostInfo.getInDepot().equals(depot)) {
					Client client = new Client();
					client.setHostname(hostInfo.getName());
					client.setLastSeen(hostInfo.getLastSeen());
					client.setReachable(reachableClients.get(depot).contains(hostInfo.getName()));
					clientsList.add(client);
				}
			}

			clients.put(depot, clientsList);
		}

		List<Client> allClients = Helper.combineListsFromMap(clients);
		clients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allClients);
	}

	public static List<String> getReachableClients() {
		if (reachableClients.isEmpty() || !reachableClients.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(reachableClients.get(selectedDepot));
	}

	public static List<String> getUnreachableClients() {
		if (unreachableClients.isEmpty() || !unreachableClients.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(unreachableClients.get(selectedDepot));
	}

	private static void retrieveClientActivityState() {
		if (!reachableClients.isEmpty() && !unreachableClients.isEmpty()) {
			return;
		}

		reachableClients.clear();
		unreachableClients.clear();

		Map<String, Object> reachableInfo = persistenceController.reachableInfo(null);
		List<String> depots = new ArrayList<>(persistenceController.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			List<String> clients = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().values()
					.stream().filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName)
					.collect(Collectors.toList());

			List<String> reachableClientsList = new ArrayList<>();
			List<String> unreachableClientsList = new ArrayList<>();

			if (!clients.isEmpty()) {
				addClientsToReachableLists(clients, reachableClientsList, unreachableClientsList, reachableInfo);
			}

			reachableClients.put(depot, reachableClientsList);
			unreachableClients.put(depot, unreachableClientsList);
		}

		List<String> allActiveClients = Helper.combineListsFromMap(reachableClients);
		reachableClients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allActiveClients);

		List<String> allInactiveClients = Helper.combineListsFromMap(unreachableClients);
		unreachableClients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allInactiveClients);
	}

	private static void addClientsToReachableLists(List<String> clients, List<String> reachableClientsList,
			List<String> unreachableClientsList, Map<String, Object> reachableInfo) {
		for (String client : clients) {
			if (reachableInfo.containsKey(client)) {
				if (Boolean.TRUE.equals(reachableInfo.get(client))) {
					reachableClientsList.add(client);
				} else {
					unreachableClientsList.add(client);
				}
			}
		}
	}

	public static Map<String, Integer> getLastSeenData() {
		if (clientLastSeen.isEmpty() || !clientLastSeen.containsKey(selectedDepot)) {
			return new HashMap<>();
		}

		return new HashMap<>(clientLastSeen.get(selectedDepot));
	}

	private static void retrieveLastSeenData() {
		if (!Helper.mapsInMapAreEmpty(clientLastSeen)) {
			return;
		}

		clientLastSeen.clear();

		Map<String, HostInfo> mapOfAllPCInfoMaps = persistenceController.getHostInfoCollections()
				.getMapOfAllPCInfoMaps();
		List<String> depots = new ArrayList<>(persistenceController.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			clientLastSeen.put(depot, produceLastSeenData(mapOfAllPCInfoMaps, depot));
		}

		Map<String, Integer> allClientLastSeen = Helper.combineMapsFromMap(clientLastSeen);
		clientLastSeen.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allClientLastSeen);
	}

	private static Map<String, Integer> produceLastSeenData(Map<String, HostInfo> mapOfAllPCInfoMaps, String depot) {
		final Map<String, Integer> lastSeenData = new HashMap<>();
		int fourteenOrLowerDays = 0;
		int betweenFifteenAndThirtyDays = 0;
		int moreThanThirtyDays = 0;

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDate currentDate = LocalDate.now();

		for (Map.Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
			if (!entry.getValue().getInDepot().equals(depot) || entry.getValue().getLastSeen().trim().isEmpty()) {
				continue;
			}

			String date = entry.getValue().getLastSeen().substring(0, 10);

			try {
				final LocalDate lastSeenDate = LocalDate.parse(date, dtf);
				final long days = ChronoUnit.DAYS.between(lastSeenDate, currentDate);

				if (days <= 14) {
					fourteenOrLowerDays++;
				} else if (days <= 30) {
					betweenFifteenAndThirtyDays++;
				} else {
					moreThanThirtyDays++;
				}
			} catch (DateTimeParseException ex) {
				Logging.info("Date couldn't be parsed: " + date);
			}
		}

		lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"), fourteenOrLowerDays);
		lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"),
				betweenFifteenAndThirtyDays);
		lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"), moreThanThirtyDays);
		return lastSeenData;
	}

	public static void clear() {
		clients.clear();
		reachableClients.clear();
		unreachableClients.clear();
		clientLastSeen.clear();
	}

	public static void retrieveData(String depot) {
		selectedDepot = depot;

		retrieveClientActivityState();
		retrieveClients();
		retrieveLastSeenData();
	}
}
