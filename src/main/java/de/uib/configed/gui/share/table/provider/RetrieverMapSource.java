/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;

public class RetrieverMapSource extends MapSource {
	// the map is not given via a parameter but by a pointer to a function

	private Object reloadEvent;
	private Supplier<Map<String, ? extends Map<String, ? extends Object>>> mapSupplier;

	public RetrieverMapSource(List<String> columnNames, Object reloadEvent,
			Supplier<Map<String, ? extends Map<String, ? extends Object>>> mapSupplier) {
		super(columnNames, null);
		this.reloadEvent = reloadEvent;
		this.mapSupplier = mapSupplier;
		rows = new ArrayList<>();
	}

	@Override
	protected void fetchData() {
		if (reloadRequested && reloadEvent != null) {
			PersistenceControllerFactory.getPersistenceController().reloadData(reloadEvent.toString());
		}

		reloadRequested = false;

		table = mapSupplier.get();

		super.fetchData();
	}
}
