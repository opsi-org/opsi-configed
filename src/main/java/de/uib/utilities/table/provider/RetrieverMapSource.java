/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.List;

public class RetrieverMapSource extends MapSource {
	// the map is not given via a parameter but by a pointer to a function

	private MapRetriever retriever;

	public RetrieverMapSource(List<String> columnNames, List<String> classNames, MapRetriever retriever,
			boolean rowCounting) {
		super(columnNames, classNames, null, rowCounting);
		this.retriever = retriever;
		rows = new ArrayList<>();
	}

	public RetrieverMapSource(List<String> columnNames, MapRetriever retriever) {
		this(columnNames, null, retriever, false);
	}

	@Override
	protected void fetchData() {
		if (reloadRequested) {
			retriever.reloadMap();
			reloadRequested = false;
		}

		table = retriever.retrieveMap();

		super.fetchData();
	}
}
