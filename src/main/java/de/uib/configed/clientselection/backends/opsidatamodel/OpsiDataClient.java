package de.uib.configed.clientselection.backends.opsidatamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.clientselection.Client;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.utilities.logging.Logging;

public class OpsiDataClient implements Client {
	public static final String HOSTINFO_MAP = "HostMap";
	public static final String SOFTWARE_MAP = "SoftwareMap";
	public static final String SWAUDIT_MAP = "SwAuditMap";

	private String hostId;
	private Map<String, Object> infoMap;

	// The opsi groups for the client
	private Set<String> groupsSet;

	// The opsi groups in which the client is contained (directly or indirectly)
	private Set<String> superGroupsSet;
	private List<Map<String, Object>> hardwareInfo;

	// The products (a list of maps)
	private List<Map<String, String>> productList;

	// Like above, but just the productIDs
	private List<String> productNames;
	private List<SWAuditClientEntry> swauditList;

	// current software to be checked
	private Map<String, String> softwareValue;

	// current swaudit to be checked
	private Map<String, String> swauditValue;
	private Iterator<Map<String, Object>> hardwareIterator = null;

	// current hardware to be checked
	private Map<String, Object> hardwareValue = null;

	public OpsiDataClient(String id) {
		hostId = id;
		groupsSet = new HashSet<>();
		productList = new LinkedList<>();
		productNames = new ArrayList<>();
		swauditList = new LinkedList<>();
		hardwareInfo = new LinkedList<>();
	}

	/** Set the map with the information about hosts */
	public void setInfoMap(Map<String, Object> map) {
		infoMap = map;
	}

	/** Set the existing opsi client groups */
	public void setGroups(Set<String> groups) {
		groupsSet = groups;
	}

	/**
	 * Set the existing opsi client groups with the client being a member of it
	 * or a subgroup of it
	 */
	public void setSuperGroups(Set<String> groups) {
		superGroupsSet = groups;
	}

	/** Set the list of opsi products */
	public void setOpsiProductList(List<Map<String, String>> productList) {
		this.productList = productList;

		for (Map<String, String> element : productList) {
			productNames.add(element.get("productId"));
		}
	}

	/** Set the products found by software audit */
	public void setSwAuditList(List<SWAuditClientEntry> swauditList) {
		this.swauditList = swauditList;
	}

	/** Get a map by name */
	public Map getMap(String map) {
		if (map.equals(HOSTINFO_MAP)) {
			return infoMap;
		} else if (map.equals(SOFTWARE_MAP)) {
			return softwareValue;
		} else if (map.equals(SWAUDIT_MAP)) {
			return swauditValue;
		} else {
			return getHardwareMap(map);
		}
	}

	/** Get the ID of this client */
	@Override
	public String getId() {
		return hostId;
	}

	@Override
	public String toString() {
		return hostId + " ( " + this.getClass() + " )";
	}

	/** Get the list of opsi products */
	public List<Map<String, String>> getSoftwareList() {
		return productList;
	}

	/** Get the list of opsi product IDs */
	public List<String> getProductNames() {
		return productNames;
	}

	/** Get the list of software audit products */
	public List<SWAuditClientEntry> getSwAuditList() {
		return swauditList;
	}

	/** Get the opsi client groups */
	public Set<String> getGroups() {
		return groupsSet;
	}

	/** Get the groups for which or a supergroup of it a client belongs to */
	public Set<String> getSuperGroups() {
		return superGroupsSet;
	}

	/** Set the current opsi software value */
	public void setCurrentSoftwareValue(Map<String, String> value) {
		softwareValue = value;
	}

	/** Set the current software audit value */
	public void setCurrentSwAuditValue(Map<String, String> value) {
		swauditValue = value;
	}

	/** Start the iterator interator. */
	public void startHardwareIterator() {
		hardwareIterator = null;
	}

	/** Go to the next hardware. Return false, if that is not possibe */
	public boolean hardwareIteratorNext() {
		if (hardwareIterator == null || !hardwareIterator.hasNext()) {
			return false;
		}

		hardwareValue = hardwareIterator.next();
		Logging.debug(this, "hardwareIteratorNext: " + hardwareValue.toString());
		return true;
	}

	/** Set the hardware information */
	public void setHardwareInfo(List<Map<String, Object>> hardwareInfo) {
		this.hardwareInfo = hardwareInfo;
	}

	/** Get the hardware Map for this string */
	private Map<String, Object> getHardwareMap(String key) {
		if (hardwareIterator == null) {
			Logging.debug(this, "getHardwareMap key " + key);
			Logging.debug(this, "getHardwareMap hardwareInfo " + hardwareInfo);
			HashSet<Map<String, Object>> values = new HashSet<>();
			for (Map<String, Object> map : hardwareInfo) {
				if (key.equals(map.get("hardwareClass"))) {
					values.add(map);
				}
			}

			Logging.debug(this, values.toString());

			hardwareValue = null;
			hardwareIterator = values.iterator();
			hardwareIteratorNext();
		}
		if (hardwareValue != null) {
			return hardwareValue;
		}
		return new HashMap<>();
	}
}
