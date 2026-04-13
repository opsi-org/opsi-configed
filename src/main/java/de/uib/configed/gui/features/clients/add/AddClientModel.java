/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
@SuppressWarnings("java:S1820")
public class AddClientModel {
	// Core input fields
	@Builder.Default
	String hostname = "";
	@Builder.Default
	String selectedDomain = "";
	@Builder.Default
	String description = "";
	@Builder.Default
	String inventoryNumber = "";
	@Builder.Default
	String notes = "";
	@Builder.Default
	String systemUUID = "";
	@Builder.Default
	String macAddress = "";
	@Builder.Default
	String ipAddress = "";
	@Builder.Default
	String selectedDepot = "";
	@Builder.Default
	String selectedNetbootProduct = "";

	// Options
	boolean wanEnabled;
	boolean wanSelected;
	boolean shutdownInstallSelected;

	// Lists
	@Builder.Default
	List<String> groups = new ArrayList<>();
	@Builder.Default
	List<String> domains = new ArrayList<>();
	@Builder.Default
	List<String> depots = new ArrayList<>();
	@Builder.Default
	List<String> netbootProducts = new ArrayList<>();
	@Builder.Default
	List<String> hostnames = new ArrayList<>();

	// Pending state for validation/confirmations
	@Builder.Default
	Map<String, Object> pendingSingleRow = new HashMap<>();
	@Builder.Default
	List<Map<String, Object>> acceptedRows = new ArrayList<>();
	@Builder.Default
	List<Map<String, Object>> rowsToImport = new ArrayList<>();
}
