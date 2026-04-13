/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.dashboard.collector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.dashboard.Helper;
import de.uib.configed.gui.type.HostInfo;

public final class ClientData {
	private static Map<String, List<Client>> clients = new HashMap<>();
	private static Map<String, List<String>> connectedClientsByMessagebus = new HashMap<>();
	private static Map<String, List<String>> notConnectedClientsByMessagebus = new HashMap<>();
	private static Map<String, Map<String, Integer>> clientLastSeen = new HashMap<>();

	private static String selectedDepot;

	private static OpsiServiceNOMPersistenceController persistenceController;

	private ClientData() {
	}

	public static void initData(OpsiServiceNOMPersistenceController persistenceController) {
		ClientData.persistenceController = persistenceController;
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

		Map<String, HostInfo> mapOfAllPCInfoMaps = persistenceController.getDataServices().hostInfoCollections
				.getMapOfAllPCInfoMaps();

		List<String> depots = new ArrayList<>(
				persistenceController.getDataServices().hostInfoCollections.getAllDepots().keySet());

		for (String depot : depots) {
			List<Client> clientsList = new ArrayList<>();

			for (Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
				HostInfo hostInfo = entry.getValue();

				if (hostInfo.getString(HostInfo.DEPOT_OF_CLIENT_KEY).equals(depot)) {
					Client client = new Client();
					client.setHostname(hostInfo.getString(HostInfo.HOSTNAME_KEY));
					client.setLastSeen(hostInfo.getString(HostInfo.LAST_SEEN_KEY));
					client.setConnectedWithMessagebus(connectedClientsByMessagebus.get(depot)
							.contains(hostInfo.getString(HostInfo.HOSTNAME_KEY)));
					clientsList.add(client);
				}
			}

			Helper.fillMapOfListsForDepots(clients, clientsList, depot);
		}
	}

	public static List<String> getConnectedClientsByMessagebus() {
		if (connectedClientsByMessagebus.isEmpty() || !connectedClientsByMessagebus.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(connectedClientsByMessagebus.get(selectedDepot));
	}

	public static List<String> getNotConnectedClientsByMessagebus() {
		if (notConnectedClientsByMessagebus.isEmpty() || !notConnectedClientsByMessagebus.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(notConnectedClientsByMessagebus.get(selectedDepot));
	}

	private static void retrieveClientsWithMessagebusConnection() {
		if (!connectedClientsByMessagebus.isEmpty() && !notConnectedClientsByMessagebus.isEmpty()) {
			return;
		}

		connectedClientsByMessagebus.clear();
		notConnectedClientsByMessagebus.clear();

		List<String> allConnectedClientsByMessagebus = new ArrayList<>(
				persistenceController.getDataServices().host.getMessagebusConnectedClients());
		List<String> depots = new ArrayList<>(
				persistenceController.getDataServices().hostInfoCollections.getAllDepots().keySet());
		for (String depot : depots) {
			List<String> notConnectedClientsByMessagebusInDepot = persistenceController
					.getDataServices().hostInfoCollections.getMapOfAllPCInfoMaps().values().stream()
							.filter(v -> depot.equals(v.getString(HostInfo.DEPOT_OF_CLIENT_KEY)))
							.map(hostInfo -> hostInfo.getString(HostInfo.HOSTNAME_KEY)).collect(Collectors.toList());
			List<String> allConnectedClientsByMessagebusInDepot = allConnectedClientsByMessagebus.stream()
					.filter(notConnectedClientsByMessagebusInDepot::contains).toList();
			notConnectedClientsByMessagebusInDepot.removeAll(allConnectedClientsByMessagebus);
			notConnectedClientsByMessagebus.put(depot, notConnectedClientsByMessagebusInDepot);
			connectedClientsByMessagebus.put(depot, allConnectedClientsByMessagebusInDepot);

			Helper.fillMapOfListsForDepots(connectedClientsByMessagebus, allConnectedClientsByMessagebusInDepot, depot);
			Helper.fillMapOfListsForDepots(notConnectedClientsByMessagebus, notConnectedClientsByMessagebusInDepot,
					depot);
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

		Map<String, HostInfo> mapOfAllPCInfoMaps = persistenceController.getDataServices().hostInfoCollections
				.getMapOfAllPCInfoMaps();
		List<String> depots = new ArrayList<>(
				persistenceController.getDataServices().hostInfoCollections.getAllDepots().keySet());

		for (String depot : depots) {
			Helper.fillMapOfMapsForDepots(clientLastSeen, produceLastSeenData(mapOfAllPCInfoMaps, depot), depot);
		}
	}

	private static Map<String, Integer> produceLastSeenData(Map<String, HostInfo> mapOfAllPCInfoMaps, String depot) {
		final Map<String, Integer> lastSeenData = new HashMap<>();
		int fourteenOrLowerDays = 0;
		int betweenFifteenAndThirtyDays = 0;
		int moreThanThirtyDays = 0;

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDate currentDate = LocalDate.now();

		for (Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
			if (!entry.getValue().getString(HostInfo.DEPOT_OF_CLIENT_KEY).equals(depot)
					|| entry.getValue().getString(HostInfo.LAST_SEEN_KEY).isBlank()) {
				continue;
			}

			String date = entry.getValue().getString(HostInfo.LAST_SEEN_KEY).substring(0, 10);
			final LocalDate lastSeenDate = LocalDate.parse(date, dtf);
			final long days = ChronoUnit.DAYS.between(lastSeenDate, currentDate);

			if (days <= 14) {
				fourteenOrLowerDays++;
			} else if (days <= 30) {
				betweenFifteenAndThirtyDays++;
			} else {
				moreThanThirtyDays++;
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
		connectedClientsByMessagebus.clear();
		notConnectedClientsByMessagebus.clear();
		clientLastSeen.clear();
	}

	public static void retrieveData(String depot) {
		selectedDepot = depot;

		retrieveClientsWithMessagebusConnection();
		retrieveClients();
		retrieveLastSeenData();
	}
}
