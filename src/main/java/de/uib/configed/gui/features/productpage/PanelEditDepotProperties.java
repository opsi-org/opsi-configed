/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.core.domain.datachanges.ProductpropertiesUpdateCollection;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ConfigedUtilityMethods;
import de.uib.configed.gui.DepotListCellRenderer;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.UpdateCollectionManager;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.datapanel.KeyValueTable;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelEditDepotProperties extends AbstractPanelEditProperties implements ListSelectionListener {
	private JLabel jLabelEditDepotProductProperties;

	private JList<String> listDepots;

	private JPanel titlePanel;

	private final Map<String, Object> emptyVisualData = new HashMap<>();

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private DepotsList depotsList;

	public PanelEditDepotProperties(ConfigedMain configedMain, KeyValueTable productPropertiesPanel,
			DepotsList depotsList) {
		super(productPropertiesPanel);

		this.depotsList = depotsList;

		initComponents(configedMain);
		initTitlePanel();
	}

	private void initComponents(ConfigedMain configedMain) {
		listDepots = new JList<>();
		listDepots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listDepots.addListSelectionListener(this);

		DepotListCellRenderer myListCellRenderer = new DepotListCellRenderer(configedMain);
		myListCellRenderer.setInfo(persistenceController.getDataServices().hostInfoCollections.getDepots());
		listDepots.setCellRenderer(myListCellRenderer);

		JScrollPane scrollpaneDepots = new JScrollPane();
		scrollpaneDepots.setViewportView(listDepots);

		JMenuItem selectAll = new JMenuItem(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
		selectAll.addActionListener(event -> selectAllDepots());

		JMenuItem selectWithEqualProperties = new JMenuItem(
				Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		selectWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());

		JPopupMenu jPopupMenu = new JPopupMenu();
		jPopupMenu.add(selectAll);
		jPopupMenu.add(selectWithEqualProperties);

		listDepots.addMouseListener(new PopupMouseListener(jPopupMenu));

		jLabelEditDepotProductProperties = new JLabel(
				Configed.getResourceValue("ProductInfoPane.jLabelEditDepotProductProperties"));

		JButton buttonSetValuesFromPackage = new JButton(Icons.getIntellijIcon("remove"));
		buttonSetValuesFromPackage
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromPackage"));
		buttonSetValuesFromPackage.addActionListener(actionEvent -> productPropertiesPanel.resetDefaults());

		JPanel panelTop = new JPanel();
		panelTop.setLayout(new MigLayout("insets 0, fill, wrap 2, hidemode 3", "[grow,fill][pref!]", "[]0"));
		panelTop.add(scrollpaneDepots, "grow");
		panelTop.add(buttonSetValuesFromPackage, "aligny bottom");

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTop, productPropertiesPanel);
		splitter.setResizeWeight(0.3);

		SplitPaneStateManager.registerSplitPane(splitter, SplitPaneStateManager.DEPOT_PRODUCT_PROPERTIES_SPLIT);

		this.setLayout(new MigLayout("insets 0, fill", "[grow, fill]", "[grow, fill]"));
		this.add(splitter, "grow, push");
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();
		titlePanel.setLayout(new MigLayout("insets 0, fill", "[grow, fill]", "[]0"));
		titlePanel.add(jLabelEditDepotProductProperties, "growx");
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean actived) {
		// Do nothing
	}

	public void clearDepotListData() {
		setDepotListData(new ArrayList<>(), "");
	}

	public void setDepotListData(List<String> depots, String productEdited) {
		Logging.info(this, "setDepotListData");
		if (depots == null) {
			Logging.warning(this, "depots list is null here");
			return;
		}

		Logging.info(this, "setDepotListData for count depots ", depots.size());

		this.productEdited = productEdited;
		listDepots.setListData(depots.toArray(new String[0]));

		selectMatchingItems(listDepots, depotsList.getSelectedValuesList());
	}

	private static void selectMatchingItems(JList<String> jlist, List<String> matchingList) {
		Set<String> matchSet = new HashSet<>(matchingList);

		List<Integer> indicesToSelect = new ArrayList<>();

		for (int i = 0; i < jlist.getModel().getSize(); i++) {
			String item = jlist.getModel().getElementAt(i);
			if (matchSet.contains(item)) {
				indicesToSelect.add(i);
			}
		}

		int[] selectedIndices = indicesToSelect.stream().mapToInt(Integer::intValue).toArray();

		jlist.setSelectedIndices(selectedIndices);
	}

	// Interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		Map<String, Object> visualData = mergeProperties(
				persistenceController.getDataServices().product.getDepot2product2propertiesPD(),
				listDepots.getSelectedValuesList(), productEdited);

		// no properties
		if (visualData == null) {
			// produce empty map
			visualData = emptyVisualData;
		}

		if (!listDepots.getSelectedValuesList().isEmpty()) {
			productPropertiesPanel.setEditableMap(visualData, persistenceController.getDataServices().product
					.getProductPropertyOptionsMap(listDepots.getSelectedValuesList().get(0), productEdited));

			// list of all property maps
			List<Map<String, Object>> storableProperties = new ArrayList<>();
			for (String depot : listDepots.getSelectedValuesList()) {
				Map<String, ConfigName2ConfigValue> product2properties = persistenceController.getDataServices().product
						.getDepot2product2propertiesPD().get(depot);

				if (product2properties == null) {
					Logging.info(this, " product2properties null for depot ", depot);
				} else if (product2properties.get(productEdited) == null) {
					Logging.info(this, " product2properties null for depot, product ", depot, ", ", productEdited);
				} else {
					storableProperties.add(product2properties.get(productEdited));
				}
			}

			// updateCollection (the real updates)
			ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(
					listDepots.getSelectedValuesList(), productEdited);
			// productPropertiesPanel.updateData(depotProductpropertiesUpdateCollection, storableProperties);
			productPropertiesPanel.setUpdateCollection(depotProductpropertiesUpdateCollection);
			UpdateCollectionManager.addToGlobalUpdateCollection(depotProductpropertiesUpdateCollection);
		}
	}

	private static Map<String, Object> mergeProperties(
			Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties, List<String> depots,
			String productId) {
		List<String> filteredDepots = depots.stream().filter(depot -> depot2product2properties.get(depot) != null
				&& depot2product2properties.get(depot).get(productId) != null).toList();

		if (depots.isEmpty()) {
			// Do nothing
		} else if (depots.size() == 1) {
			Map<String, ConfigName2ConfigValue> propertiesDepot0 = depot2product2properties.get(depots.get(0));
			if (propertiesDepot0 != null && propertiesDepot0.get(productId) != null) {
				return propertiesDepot0.get(productId);
			}
		} else {
			List<Map<String, Object>> propertiesForProductOnDepots = POJOReMapper.remap(depot2product2properties
					.entrySet().stream().filter(entry -> filteredDepots.contains(entry.getKey()))
					.map(entry -> entry.getValue().get(productId)).toList());

			return POJOReMapper.remap(ConfigedUtilityMethods.mergeMaps(propertiesForProductOnDepots));
		}

		return new HashMap<>();
	}

	private void selectDepotsWithEqualProperties() {
		String selectedDepot0 = listDepots.getSelectedValue();

		if (selectedDepot0 == null || selectedDepot0.isEmpty()) {
			return;
		}

		ConfigName2ConfigValue properties0 = persistenceController.getDataServices().product
				.getDefaultProductPropertiesPD(selectedDepot0).get(productEdited);

		int startDepotIndex = listDepots.getSelectedIndex();
		listDepots.setSelectionInterval(startDepotIndex, startDepotIndex);

		for (int i = 0; i < listDepots.getModel().getSize(); i++) {
			String compareDepot = listDepots.getModel().getElementAt(i);

			if (compareDepot.equals(selectedDepot0)) {
				continue;
			}

			ConfigName2ConfigValue compareProperties = persistenceController.getDataServices().product
					.getDefaultProductPropertiesPD(compareDepot).get(productEdited);

			// True if both objects are equal or both null
			if (Objects.equals(properties0, compareProperties)) {
				listDepots.addSelectionInterval(i, i);
			}
		}
	}

	private void selectAllDepots() {
		listDepots.setSelectionInterval(0, listDepots.getModel().getSize() - 1);
	}
}
