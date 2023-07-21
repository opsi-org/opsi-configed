/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.List;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public class ExternalSource implements TableSource {
	// adapter for external source for table data

	private List<String> columnNames;

	private List<String> classNames;

	private boolean reloadRequested = true;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ExternalSource(List<String> columnNames, List<String> classNames) {
		this.columnNames = columnNames;
		this.classNames = classNames;
	}

	@Override
	public List<String> retrieveColumnNames() {
		return columnNames;
	}

	@Override
	public List<String> retrieveClassNames() {
		return classNames;
	}

	@Override
	public List<List<Object>> retrieveRows() {
		if (reloadRequested) {
			persistenceController.productDataRequestRefresh();
			reloadRequested = false;
		}

		return persistenceController.getProductRows();
	}

	@Override
	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public void setRowCounting(boolean b) {
		/* Not needed */}
}
