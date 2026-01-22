/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.uib.configed.core.domain.SavedSearches;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.clientselection.SelectionManager;
import de.uib.configed.gui.share.swing.SearchQueryExecutor;
import de.uib.configed.gui.type.DateExtendedByVars;
import de.uib.configed.share.logging.Logging;

public class ClientSearch {
	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientSearch() {
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

			item.addActionListener(actionEvent -> selectClientsByFailedAtSomeTimeAgo(
					searchedTimeSpans.get(entry.getKey()), entry.getValue()));

			jMenu.add(item);
		}
	}

	public void groupByNotCurrentProductVersion() {
		String products = getLocalbootProductsFromSelection();
		selectClientsNotCurrentProductInstalled(products, false,
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion"));
	}

	public void groupByNotCurrentProductVersionOrBrokenInstallation() {
		String products = getLocalbootProductsFromSelection();
		selectClientsNotCurrentProductInstalled(products, true, Configed
				.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersionOrUnknownState"));
	}

	public void groupByFailedProduct() {
		String products = getLocalbootProductsFromSelection();
		selectClientsWithFailedProduct(products);
	}

	private String getLocalbootProductsFromSelection() {
		ListSelectionDialog listSelectionDialog = new ListSelectionDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.productSelection"));
		listSelectionDialog.setListData(
				new ArrayList<>(persistenceController.getDataServices().product.getAllLocalbootProductNames()));
		listSelectionDialog.show();
		return listSelectionDialog.wasAccepted() ? listSelectionDialog.getSelectedValue() : "";
	}

	private void selectClientsWithFailedProduct(String selectedProduct) {
		Logging.debug(this, "selectClientsWithFailedProduct, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			SelectionManager manager = new SelectionManager();
			String test = String.format(SavedSearches.SEARCH_FAILED_PRODUCT, selectedProduct);
			manager.setSearch(test);
			return manager.selectClients();
		}, Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct").replace("...",
				" " + selectedProduct));
		executor.execute();
		selectProducts(Set.of(selectedProduct));
	}

	public void selectClientsNotCurrentProductInstalled(String selectedProduct,
			boolean includeClientsWithBrokenInstallation, String searchQueryName) {
		Logging.debug(this, "selectClientsNotCurrentProductInstalled, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			String productVersion = persistenceController.getDataServices().product.getProductVersion(selectedProduct);
			String packageVersion = persistenceController.getDataServices().product
					.getProductPackageVersion(selectedProduct);

			Logging.debug(this, "selectClientsNotCurrentProductInstalled product ", selectedProduct, ", ",
					productVersion, ", ", packageVersion);

			return persistenceController.getDataServices().host.getClientsWithOtherProductVersion(selectedProduct,
					productVersion, packageVersion, includeClientsWithBrokenInstallation);
		}, searchQueryName.replace("...", " " + selectedProduct));
		executor.execute();
		selectProducts(Set.of(selectedProduct));
	}

	private static void selectProducts(Set<String> products) {
		ConfigedMain.getMainFrame().getClientConfiguration().getPanelLocalbootProductSettings().enableFilterMode(false);
		ConfigedMain.getMainFrame().getClientConfiguration().getPanelLocalbootProductSettings().getProductTable()
				.setPendingSelection(products);
		ConfigedMain.getMainFrame().getClientConfiguration().getPanelLocalbootProductSettings().enableFilterMode(true);
	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg, String searchQueryName) {
		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			SelectionManager manager = new SelectionManager();
			manager.setSearch(getSearchQueryFromArg(arg));
			return manager.selectClients();
		}, Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan")
				+ searchQueryName);
		executor.execute();
	}

	private String getSearchQueryFromArg(String arg) {
		String searchQuery = SavedSearches.SEARCH_FAILED_AT_ANY_TIME;

		if (arg != null && !arg.isEmpty()) {
			String timeAgo = DateExtendedByVars.interpretVar(arg);
			searchQuery = String.format(SavedSearches.SEARCH_FAILED_BY_TIMES, timeAgo);
			Logging.info(this, "getSearchQueryFromArg  searchQuery ", searchQuery);
		}

		return searchQuery;
	}
}
