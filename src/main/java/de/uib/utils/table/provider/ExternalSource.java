/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.provider;

import java.util.List;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class ExternalSource implements TableSource {
	// adapter for external source for table data

	private Iterable<String> depotIds;
	private List<String> columnNames;
	private boolean reloadRequested;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ExternalSource(List<String> columnNames, Iterable<String> depotIds) {
		this.columnNames = columnNames;
		this.depotIds = depotIds;
	}

	@Override
	public List<String> retrieveColumnNames() {
		return columnNames;
	}

	@Override
	public List<List<Object>> retrieveRows() {
		if (reloadRequested) {
			reloadRequested = false;
		}

		return persistenceController.getProductDataService().getProductRowsForDepots(depotIds);
	}

	@Override
	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public void cancelRequestReload() {
		reloadRequested = false;
	}

	@Override
	public void setRowCounting(boolean b) {
		/* Not needed */}
}
