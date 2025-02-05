/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.type.DateExtendedByVars;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class ClientSearch {
	private ConfigedMain configedMain;

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientSearch(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initMenuData();
	}

	private void initMenuData() {
		searchedTimeSpans = new LinkedHashMap<>();

		final String TODAY = "today";
		final String SINCE_YESTERDAY = "since yesterday";
		final String LAST_3_DAYS = "last 3 days";
		final String LAST_7_DAYS = "last 7 days";
		final String LAST_MONTH = "last month";
		final String ANY_TIME = "at any time";

		searchedTimeSpans.put(TODAY, "%minus0%");
		searchedTimeSpans.put(SINCE_YESTERDAY, "%minus1%");
		searchedTimeSpans.put(LAST_3_DAYS, "%minus2%");
		searchedTimeSpans.put(LAST_7_DAYS, "%minus7%");
		searchedTimeSpans.put(LAST_MONTH, "%minus31%");
		searchedTimeSpans.put(ANY_TIME, "");

		searchedTimeSpansText = new LinkedHashMap<>();

		searchedTimeSpansText.put(TODAY, Configed.getResourceValue("MainFrame.TODAY"));
		searchedTimeSpansText.put(SINCE_YESTERDAY, Configed.getResourceValue("MainFrame.SINCE_YESTERDAY"));
		searchedTimeSpansText.put(LAST_3_DAYS, Configed.getResourceValue("MainFrame.LAST_3_DAYS"));
		searchedTimeSpansText.put(LAST_7_DAYS, Configed.getResourceValue("MainFrame.LAST_7_DAYS"));
		searchedTimeSpansText.put(LAST_MONTH, Configed.getResourceValue("MainFrame.LAST_MONTH"));
		searchedTimeSpansText.put(ANY_TIME, Configed.getResourceValue("MainFrame.ANY_TIME"));
	}

	public void addSearchSpansToJMenu(JMenu jMenu) {
		for (Entry<String, String> entry : searchedTimeSpansText.entrySet()) {
			JMenuItem item = new JMenuItem(entry.getValue());

			item.addActionListener(
					actionEvent -> selectClientsByFailedAtSomeTimeAgo(searchedTimeSpans.get(entry.getKey())));

			jMenu.add(item);
		}
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
		ListSelectionDialog listSelectionDialog = new ListSelectionDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.productSelection"));
		listSelectionDialog.setListData(new ArrayList<>(
				new TreeSet<>(persistenceController.getProductDataService().getAllLocalbootProductNames())));
		listSelectionDialog.setMultiSelection();
		listSelectionDialog.show();
		return listSelectionDialog.wasAccepted() ? listSelectionDialog.getSelectedValue() : "";
	}

	private void selectClientsWithFailedProduct(String selectedProduct) {
		Logging.debug(this, "selectClientsWithFailedProduct, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SelectionManager manager = new SelectionManager();

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
		SelectionManager manager = new SelectionManager();

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
