/*
 * ProductInfoPanes.java
 *
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2022 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 * 
 * This class encloses the two models for the RequirementsTable and the DependenciesTree
 * since they belong together one panel.
 * It also adds more properties to the whole model for the whole panel,
 * You can add a listener to listen to changes to the model (new depot/product)
 * 
 * @author Nils Otto
 
 */

package de.uib.configed.guidata;

import java.util.HashSet;
import java.util.Set;

import de.uib.opsidatamodel.PersistenceController;

public class DependenciesModel {

	private Set<DependenciesModelListener> listeners;

	private RequirementsTableModel requirementsTableModel;
	private DependenciesTreeModel dependenciesTreeModel;

	private PersistenceController persistenceController;

	public DependenciesModel(PersistenceController persistenceController) {
		this.persistenceController = persistenceController;

		requirementsTableModel = new RequirementsTableModel(persistenceController);
		dependenciesTreeModel = new DependenciesTreeModel(persistenceController);

		listeners = new HashSet<>();
	}

	public RequirementsTableModel getRequirementsModel() {
		return requirementsTableModel;
	}

	public DependenciesTreeModel getDependenciesTreeModel() {
		return dependenciesTreeModel;
	}

	public void setActualProduct(String productId) {
		String depotId = persistenceController.getDepot();

		setActualProduct(depotId, productId);
	}

	public void setActualProduct(String depotId, String productId) {
		requirementsTableModel.setActualProduct(depotId, productId);

		dependenciesTreeModel.setActualProduct(depotId, productId);

		fireUpdateDepot(depotId);
		fireUpdateProduct(productId);
	}

	public void fireUpdateProduct(String productId) {
		for (DependenciesModelListener listener : listeners)
			listener.updateProduct(productId);
	}

	public void fireUpdateDepot(String depotId) {
		for (DependenciesModelListener listener : listeners)
			listener.updateDepot(depotId);
	}

	public void addListener(DependenciesModelListener listener) {
		listeners.add(listener);
	}

	public interface DependenciesModelListener {
		public void updateProduct(String productId);

		public void updateDepot(String depotId);
	}
}
