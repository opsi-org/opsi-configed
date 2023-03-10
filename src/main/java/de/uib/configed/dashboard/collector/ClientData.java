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
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public final class ClientData {
	private static Map<String, List<Client>> clients = new HashMap<>();
	private static Map<String, List<String>> activeClients = new HashMap<>();
	private static Map<String, List<String>> inactiveClients = new HashMap<>();
	private static Map<String, Map<String, Integer>> clientLastSeen = new HashMap<>();

	private static String selectedDepot;

	private static AbstractPersistenceController persist = PersistenceControllerFactory.getPersistenceController();

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

		Map<String, HostInfo> mapOfAllPCInfoMaps = persist.getHostInfoCollections().getMapOfAllPCInfoMaps();

		List<String> depots = new ArrayList<>(persist.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			List<Client> clientsList = new ArrayList<>();

			for (Map.Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
				HostInfo hostInfo = entry.getValue();

				if (hostInfo.getInDepot().equals(depot)) {
					Client client = new Client();
					client.setHostname(hostInfo.getName());
					client.setLastSeen(hostInfo.getLastSeen());
					client.setReachable(activeClients.get(depot).contains(hostInfo.getName()));
					clientsList.add(client);
				}
			}

			clients.put(depot, clientsList);
		}

		List<Client> allClients = Helper.combineListsFromMap(clients);
		clients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allClients);
	}

	public static List<String> getActiveClients() {
		if (activeClients.isEmpty() || !activeClients.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(activeClients.get(selectedDepot));
	}

	public static List<String> getInactiveClients() {
		if (inactiveClients.isEmpty() || !inactiveClients.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(inactiveClients.get(selectedDepot));
	}

	private static void retrieveClientActivityState() {
		if (!activeClients.isEmpty() && !inactiveClients.isEmpty()) {
			return;
		}

		activeClients.clear();
		inactiveClients.clear();

		Map<String, Object> reachableInfo = persist.reachableInfo(null);
		List<String> depots = new ArrayList<>(persist.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			List<String> clients = persist.getHostInfoCollections().getMapOfAllPCInfoMaps().values().stream()
					.filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName).collect(Collectors.toList());

			List<String> activeClientsList = new ArrayList<>();
			List<String> inactiveClientsList = new ArrayList<>();

			if (!clients.isEmpty()) {
				for (String client : clients) {
					if (reachableInfo.containsKey(client)) {
						if (Boolean.TRUE.equals(reachableInfo.get(client))) {
							activeClientsList.add(client);
						} else {
							inactiveClientsList.add(client);
						}
					}
				}
			}

			activeClients.put(depot, activeClientsList);
			inactiveClients.put(depot, inactiveClientsList);
		}

		List<String> allActiveClients = Helper.combineListsFromMap(activeClients);
		activeClients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allActiveClients);

		List<String> allInactiveClients = Helper.combineListsFromMap(inactiveClients);
		inactiveClients.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allInactiveClients);
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

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDate current = LocalDate.now();

		Map<String, HostInfo> mapOfAllPCInfoMaps = persist.getHostInfoCollections().getMapOfAllPCInfoMaps();
		List<String> depots = new ArrayList<>(persist.getHostInfoCollections().getAllDepots().keySet());

		for (String depot : depots) {
			final Map<String, Integer> lastSeenData = new HashMap<>();
			int fourteenOrLowerDays = 0;
			int betweenFifteenAndThirtyDays = 0;
			int moreThanThirtyDays = 0;

			for (Map.Entry<String, HostInfo> entry : mapOfAllPCInfoMaps.entrySet()) {
				if (entry.getValue().getInDepot().equals(depot)) {
					if (entry.getValue().getLastSeen().trim().isEmpty()) {
						continue;
					}

					String date = entry.getValue().getLastSeen().substring(0, 10);

					try {
						final LocalDate lastSeenDate = LocalDate.parse(date, dtf);
						final long days = ChronoUnit.DAYS.between(lastSeenDate, current);

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

				lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"),
						fourteenOrLowerDays);
				lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"),
						betweenFifteenAndThirtyDays);
				lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"),
						moreThanThirtyDays);
			}

			clientLastSeen.put(depot, lastSeenData);
		}

		Map<String, Integer> allClientLastSeen = Helper.combineMapsFromMap(clientLastSeen);
		clientLastSeen.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allClientLastSeen);
	}

	public static void clear() {
		clients.clear();
		activeClients.clear();
		inactiveClients.clear();
		clientLastSeen.clear();
	}

	public static void retrieveData(String depot) {
		selectedDepot = depot;

		retrieveClientActivityState();
		retrieveClients();
		retrieveLastSeenData();
	}
}
