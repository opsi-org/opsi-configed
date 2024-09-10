/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.Icon;

import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.gui.FSelectionList;
import de.uib.configed.type.DateExtendedByVars;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;

public class ClientSearch {
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientSearch(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void groupByNotCurrentProductVersion() {
		String products = getLocalbootProductsFromSelection();
		selectClientsNotCurrentProductInstalled(products, false);
	}

	public void groupByNotCurrentProductVersionOrBrokenInstallation() {
		String products = getLocalbootProductsFromSelection();
		selectClientsNotCurrentProductInstalled(products, true);
	}

	public void groupByFailedProduct() {
		String products = getLocalbootProductsFromSelection();
		selectClientsWithFailedProduct(products);
	}

	private String getLocalbootProductsFromSelection() {
		FSelectionList fProductSelectionList = new FSelectionList(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.productSelection"), true, new String[] { "", "" },
				new Icon[] { Icons.getIntellijIcon("close"), Icons.getIntellijIcon("checkmark") }, 400, 600);
		fProductSelectionList.setListData(new ArrayList<>(
				new TreeSet<>(persistenceController.getProductDataService().getAllLocalbootProductNames())));
		fProductSelectionList.setVisible(true);
		return fProductSelectionList.getResult() == 2 ? fProductSelectionList.getSelectedValue() : "";
	}

	private void selectClientsWithFailedProduct(String selectedProduct) {
		Logging.debug(this, "selectClientsWithFailedProduct, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SelectionManager manager = new SelectionManager(null);

		String test = String.format(SavedSearches.SEARCH_FAILED_PRODUCT, selectedProduct);

		manager.setSearch(test);

		List<String> result = manager.selectClients();

		Logging.info(this, "selected: ", result);
		configedMain.getClientTablePanel().setSelectedValues(result);
	}

	public void selectClientsNotCurrentProductInstalled(String selectedProduct,
			boolean includeClientsWithBrokenInstallation) {
		Logging.debug(this, "selectClientsNotCurrentProductInstalled, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		String productVersion = persistenceController.getProductDataService().getProductVersion(selectedProduct);
		String packageVersion = persistenceController.getProductDataService().getProductPackageVersion(selectedProduct);

		Logging.debug(this, "selectClientsNotCurrentProductInstalled product ", selectedProduct, ", ", productVersion,
				", ", packageVersion);

		List<String> clientsToSelect = persistenceController.getHostDataService().getClientsWithOtherProductVersion(
				selectedProduct, productVersion, packageVersion, includeClientsWithBrokenInstallation);

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally ", clientsToSelect.size());

		configedMain.getClientTablePanel().setSelectedValues(clientsToSelect);
	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg) {
		SelectionManager manager = new SelectionManager(null);

		if (arg == null || arg.isEmpty()) {
			manager.setSearch(SavedSearches.SEARCH_FAILED_AT_ANY_TIME);
		} else {
			String timeAgo = DateExtendedByVars.interpretVar(arg);
			String test = String.format(SavedSearches.SEARCH_FAILED_BY_TIMES, timeAgo);

			Logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test ", test);
			manager.setSearch(test);
		}

		List<String> result = manager.selectClients();

		configedMain.getClientTablePanel().setSelectedValues(result);
	}
}
