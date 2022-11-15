package de.uib.configed.dashboard.collector;

import java.util.*;

import de.uib.opsidatamodel.*;

public class DepotData
{
	private static Map<String, Map<String, Object>> depots = new HashMap<>();

	private static PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	public static Map<String, Map<String, Object>> getDepots()
	{
		return new HashMap<>(depots);
	}

	private static void retrieveDepots()
	{
		if (!depots.isEmpty())
		{
			return;
		}

		persist.getHostInfoCollections().opsiHostsRequestRefresh();
		depots = persist.getHostInfoCollections().getDepots();
	}

	public static void clear()
	{
		depots.clear();
	}

	public static void retrieveData()
	{
		retrieveDepots();
	}
}
