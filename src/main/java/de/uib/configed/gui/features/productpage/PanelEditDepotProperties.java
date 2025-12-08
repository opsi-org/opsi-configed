/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.UpdateCollectionManager;
import de.uib.configed.gui.share.datapanel.DefaultEditMapPanel;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class PanelEditDepotProperties extends AbstractPanelEditProperties
		implements ListSelectionListener, MouseListener, KeyListener {
	private JLabel jLabelEditDepotProductProperties;

	private JList<String> listDepots;
	private List<String> listSelectedDepots;

	private JPanel titlePanel;

	private final Map<String, Object> emptyVisualData = new HashMap<>();

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelEditDepotProperties(ConfigedMain configedMain, DefaultEditMapPanel productPropertiesPanel) {
		super(productPropertiesPanel);
		initComponents(configedMain);
		initTitlePanel();
	}

	private void initComponents(ConfigedMain configedMain) {
		listDepots = new JList<>();
		listDepots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listDepots.addListSelectionListener(this);
		listDepots.addMouseListener(this);
		listDepots.addKeyListener(this);

		DepotListCellRenderer myListCellRenderer = new DepotListCellRenderer(configedMain);
		myListCellRenderer.setInfo(persistenceController.getHostInfoCollections().getDepots());
		listDepots.setCellRenderer(myListCellRenderer);

		JScrollPane scrollpaneDepots = new JScrollPane();
		scrollpaneDepots.setViewportView(listDepots);

		JPopupMenu jPopupMenu = new JPopupMenu();
		JMenuItem selectAll = new JMenuItem(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
		selectAll.addActionListener(event -> selectAllDepots());

		JMenuItem selectWithEqualProperties = new JMenuItem(
				Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		selectWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());

		jPopupMenu.add(selectAll);
		jPopupMenu.add(selectWithEqualProperties);
		listDepots.setComponentPopupMenu(jPopupMenu);

		jLabelEditDepotProductProperties = new JLabel(
				Configed.getResourceValue("ProductInfoPane.jLabelEditDepotProductProperties"));

		JButton buttonSetValuesFromPackage = new JButton(Icons.getIntellijIcon("remove"));
		buttonSetValuesFromPackage
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromPackage"));
		buttonSetValuesFromPackage.addActionListener(actionEvent -> productPropertiesPanel.resetDefaults());

		JPanel panelTop = new JPanel();
		GroupLayout layoutEditProperties = new GroupLayout(panelTop);
		panelTop.setLayout(layoutEditProperties);

		layoutEditProperties.setHorizontalGroup(layoutEditProperties.createSequentialGroup()
				.addComponent(scrollpaneDepots, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromPackage, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layoutEditProperties.setVerticalGroup(layoutEditProperties.createParallelGroup(Alignment.TRAILING)
				.addComponent(scrollpaneDepots, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromPackage, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setResizeWeight(0.3);
		splitter.setTopComponent(panelTop);
		splitter.setBottomComponent(productPropertiesPanel);

		GroupLayout layoutAll = new GroupLayout(this);
		setLayout(layoutAll);

		layoutAll.setVerticalGroup(layoutAll.createSequentialGroup().addComponent(splitter, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutAll.setHorizontalGroup(layoutAll.createParallelGroup().addComponent(splitter, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createParallelGroup().addComponent(jLabelEditDepotProductProperties,
				0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		titleLayout.setVerticalGroup(titleLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelEditDepotProductProperties, 0, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE));
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

		resetSelectedDepots(depots);
	}

	// Interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		Map<String, Object> visualData = mergeProperties(
				persistenceController.getProductDataService().getDepot2product2propertiesPD(),
				listDepots.getSelectedValuesList(), productEdited);

		// no properties
		if (visualData == null) {
			// produce empty map
			visualData = emptyVisualData;
		}

		if (!listDepots.getSelectedValuesList().isEmpty()) {
			productPropertiesPanel.setEditableMap(visualData, persistenceController.getProductDataService()
					.getProductPropertyOptionsMap(listDepots.getSelectedValuesList().get(0), productEdited));

			// list of all property maps
			List<Map<String, Object>> storableProperties = new ArrayList<>();
			for (String depot : listDepots.getSelectedValuesList()) {
				Map<String, ConfigName2ConfigValue> product2properties = persistenceController.getProductDataService()
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
			productPropertiesPanel.updateData(depotProductpropertiesUpdateCollection, storableProperties);
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

	private void saveSelectedDepots() {
		Logging.debug(this, "saveSelectedDepots");
		listSelectedDepots = listDepots.getSelectedValuesList();
	}

	private void resetSelectedDepots(List<String> baseList) {
		Logging.debug(this, "resetSelectedDepots");

		listDepots.setValueIsAdjusting(true);

		if (listSelectedDepots == null || listSelectedDepots.isEmpty()) {
			// mark all
			listDepots.setSelectionInterval(0, listDepots.getModel().getSize() - 1);
		} else {
			int[] selection = new int[listSelectedDepots.size()];
			int n = 0;
			for (int j = 0; j < baseList.size(); j++) {
				String depot = baseList.get(j);
				if (listSelectedDepots.indexOf(depot) > -1) {
					selection[n] = j;
					n++;
				}
			}
			Logging.debug(this, "resetSelectedDepots, n, selection is ", n, ", -- ", Arrays.toString(selection));

			for (int i = 0; i < n; i++) {
				listDepots.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
			}
		}
		listDepots.setValueIsAdjusting(false);
	}

	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == listDepots) {
			saveSelectedDepots();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Do nothing because KeyListener demands implementation
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Do nothing because KeyListener demands implementation
	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		Logging.info(this, "mouseClicked ", e);
		if (e.getSource() == listDepots) {
			saveSelectedDepots();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Do nothing because MouseListener demands implementation
	}

	private void selectDepotsWithEqualProperties() {
		String selectedDepot0 = listDepots.getSelectedValue();

		if (selectedDepot0 == null || selectedDepot0.isEmpty()) {
			return;
		}

		ConfigName2ConfigValue properties0 = persistenceController.getProductDataService()
				.getDefaultProductPropertiesPD(selectedDepot0).get(productEdited);

		int startDepotIndex = listDepots.getSelectedIndex();
		listDepots.setSelectionInterval(startDepotIndex, startDepotIndex);

		for (int i = 0; i < listDepots.getModel().getSize(); i++) {
			String compareDepot = listDepots.getModel().getElementAt(i);

			if (compareDepot.equals(selectedDepot0)) {
				continue;
			}

			ConfigName2ConfigValue compareProperties = persistenceController.getProductDataService()
					.getDefaultProductPropertiesPD(compareDepot).get(productEdited);

			// True if both objects are equal or both null
			if (Objects.equals(properties0, compareProperties)) {
				listDepots.addSelectionInterval(i, i);
			}
		}

		saveSelectedDepots();
	}

	private void selectAllDepots() {
		listDepots.setSelectionInterval(0, listDepots.getModel().getSize() - 1);
		saveSelectedDepots();
	}
}
