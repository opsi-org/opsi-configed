/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.HashSet;
import java.util.Set;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class DependenciesModel {
	private Set<DependenciesModelListener> listeners;

	private RequirementsTableModel requirementsTableModel;
	private DependenciesTreeModel dependenciesTreeModel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DependenciesModel() {
		requirementsTableModel = new RequirementsTableModel();
		dependenciesTreeModel = new DependenciesTreeModel();

		listeners = new HashSet<>();
	}

	public RequirementsTableModel getRequirementsModel() {
		return requirementsTableModel;
	}

	public DependenciesTreeModel getDependenciesTreeModel() {
		return dependenciesTreeModel;
	}

	public void setActualProduct(String productId) {
		String depotId = persistenceController.getDepotDataService().getDepot();

		setActualProduct(depotId, productId);
	}

	public void setActualProduct(String depotId, String productId) {
		requirementsTableModel.setActualProduct(depotId, productId);

		dependenciesTreeModel.setActualProduct(depotId, productId);

		fireUpdateDepot(depotId);
		fireUpdateProduct(productId);
	}

	private void fireUpdateProduct(String productId) {
		for (DependenciesModelListener listener : listeners) {
			listener.updateProduct(productId);
		}
	}

	private void fireUpdateDepot(String depotId) {
		for (DependenciesModelListener listener : listeners) {
			listener.updateDepot(depotId);
		}
	}

	public void addListener(DependenciesModelListener listener) {
		listeners.add(listener);
	}

	public interface DependenciesModelListener {
		void updateProduct(String productId);

		void updateDepot(String depotId);
	}
}
