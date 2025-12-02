/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
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
	String groups = "";
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
	List<String> domains = new ArrayList<>();
	@Builder.Default
	List<String> depots = new ArrayList<>();
	@Builder.Default
	List<String> netbootProducts = new ArrayList<>();
	@Builder.Default
	List<String> hostnames = new ArrayList<>();

	// UI flags
	@Builder.Default
	boolean withDialog = true;
	boolean initialized;

	// Pending state for validation/confirmations
	@Builder.Default
	List<Object> pendingSingleRow = new ArrayList<>();
	@Builder.Default
	List<List<Object>> acceptedRows = new ArrayList<>();
	@Builder.Default
	List<List<Object>> rowsToImport = new ArrayList<>();
}
