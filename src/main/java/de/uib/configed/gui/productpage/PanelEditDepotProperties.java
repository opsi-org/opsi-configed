package de.uib.configed.gui.productpage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014, 2016 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.configed.guidata.ListMerger;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.list.StandardListCellRenderer;

public class PanelEditDepotProperties extends AbstractPanelEditProperties
		implements ListSelectionListener, ActionListener, MouseListener, KeyListener {

	private JLabel jLabelEditDepotProductProperties;

	private JList<String> listDepots;
	private List<String> listSelectedDepots;
	private JButton buttonSelectWithEqualProperties;
	private JButton buttonSelectAll;
	JPopupMenu popupDepot = new JPopupMenu();

	private JPanel titlePanel;

	protected final Map<String, Object> emptyVisualData = new HashMap<>();

	public PanelEditDepotProperties(ConfigedMain mainController, DefaultEditMapPanel productPropertiesPanel) {
		super(mainController, productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	private void initComponents() {

		JPanel panelDepots = new JPanel();

		listDepots = new JList<>();
		listDepots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listDepots.addListSelectionListener(this);
		listDepots.addMouseListener(this);
		listDepots.addKeyListener(this);

		listDepots.setCellRenderer(new StandardListCellRenderer());

		listSelectedDepots = new ArrayList<>();

		JScrollPane scrollpaneDepots = new JScrollPane();
		scrollpaneDepots.setViewportView(listDepots);

		popupDepot = new JPopupMenu();
		listDepots.setComponentPopupMenu(popupDepot);

		buttonSelectWithEqualProperties = new JButton("", Globals.createImageIcon("images/equalplus.png", ""));

		buttonSelectWithEqualProperties
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSelectAllWithEqualProperties"));
		Globals.formatButtonSmallText(buttonSelectWithEqualProperties);
		buttonSelectWithEqualProperties.addActionListener(this);

		buttonSelectAll = new JButton("", Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectAll.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSelectAll"));
		Globals.formatButtonSmallText(buttonSelectAll);
		buttonSelectAll.addActionListener(this);

		GroupLayout layoutPanelDepots = new GroupLayout(panelDepots);
		panelDepots.setLayout(layoutPanelDepots);

		layoutPanelDepots.setVerticalGroup(layoutPanelDepots.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrollpaneDepots, GroupLayout.Alignment.LEADING, 0, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(buttonSelectWithEqualProperties, GroupLayout.Alignment.TRAILING,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSelectAll, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layoutPanelDepots.setHorizontalGroup(layoutPanelDepots.createSequentialGroup()
				.addComponent(scrollpaneDepots, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSelectWithEqualProperties, 0, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSelectAll, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		if (!ConfigedMain.OPSI_4_3) {
			buttonSelectAll.setForeground(Globals.blue);
			buttonSelectWithEqualProperties.setForeground(Globals.blue);
		}

		// jLabelProductProperties = new JLabel (

		jLabelEditDepotProductProperties = new JLabel(
				Configed.getResourceValue("ProductInfoPane.jLabelEditDepotProductProperties"));
		jLabelEditDepotProductProperties.setFont(Globals.defaultFontBold);

		IconButton buttonSetValuesFromPackage = new IconButton(
				Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromPackage"),
				"images/reset_network_defaults.png", "images/reset_network_defaults_over.png", " ", true);

		buttonSetValuesFromPackage.setPreferredSize(new Dimension(15, 30));

		buttonSetValuesFromPackage.addActionListener(actionEvent -> productPropertiesPanel.resetDefaults());

		JPanel panelTop = new JPanel();
		GroupLayout layoutEditProperties = new GroupLayout(panelTop);
		panelTop.setLayout(layoutEditProperties);

		layoutEditProperties.setHorizontalGroup(layoutEditProperties.createSequentialGroup()
				.addComponent(panelDepots, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromPackage, 40, 40, 40));

		layoutEditProperties.setVerticalGroup(layoutEditProperties.createParallelGroup(Alignment.TRAILING)
				.addComponent(panelDepots, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromPackage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setResizeWeight(0.3);
		splitter.setTopComponent(panelTop);
		splitter.setBottomComponent(productPropertiesPanel);

		GroupLayout layoutAll = new GroupLayout(this);
		setLayout(layoutAll);

		layoutAll.setVerticalGroup(layoutAll.createSequentialGroup().addComponent(splitter, minHSize,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutAll.setHorizontalGroup(layoutAll.createParallelGroup().addComponent(splitter, Globals.MIN_TABLE_V_SIZE,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createParallelGroup().addComponent(jLabelEditDepotProductProperties,
				minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		titleLayout.setVerticalGroup(titleLayout.createSequentialGroup().addComponent(jLabelEditDepotProductProperties,
				GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean actived) {
		if (!ConfigedMain.OPSI_4_3) {
			jLabelEditDepotProductProperties.setForeground(actived ? Globals.lightBlack : Globals.greyed);
		}
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

		Logging.info(this, "setDepotListData for count depots " + depots.size());

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
				mainController.getPersistenceController().getDepot2product2properties(),
				listDepots.getSelectedValuesList(), productEdited);

		// no properties
		if (visualData == null) {
			// produce empty map
			visualData = emptyVisualData;
		}

		if (!listDepots.getSelectedValuesList().isEmpty()) {
			productPropertiesPanel.setEditableMap(

					visualData,

					mainController.getPersistenceController()
							.getProductPropertyOptionsMap(listDepots.getSelectedValuesList().get(0), productEdited));

			// list of all property maps
			List<Map<String, Object>> storableProperties = new ArrayList<>();
			for (String depot : listDepots.getSelectedValuesList()) {
				Map<String, ConfigName2ConfigValue> product2properties = mainController.getPersistenceController()
						.getDepot2product2properties().get(depot);

				if (product2properties == null) {
					Logging.info(this, " product2properties null for depot " + depot);
				} else if (product2properties.get(productEdited) == null) {
					Logging.info(this, " product2properties null for depot, product " + depot + ", " + productEdited);
				} else {
					storableProperties.add(product2properties.get(productEdited));
				}
			}
			productPropertiesPanel.setStoreData(storableProperties);

			// updateCollection (the real updates)
			ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(
					mainController, mainController.getPersistenceController(), listDepots.getSelectedValuesList(),
					productEdited);
			productPropertiesPanel.setUpdateCollection(depotProductpropertiesUpdateCollection);
			mainController.addToGlobalUpdateCollection(depotProductpropertiesUpdateCollection);
		}
	}

	private Map<String, Object> mergeProperties(
			Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties, List<String> depots,
			String productId) {

		Map<String, Object> result = new HashMap<>();

		if (depots == null || depots.isEmpty()) {
			return result;
		}

		Map<String, ConfigName2ConfigValue> propertiesDepot0 = depot2product2properties.get(depots.get(0));

		if (depots.size() == 1) {
			if (propertiesDepot0 == null || propertiesDepot0.get(productId) == null) {
				// ready
			} else {
				result = propertiesDepot0.get(productId);
			}
		} else {
			int n = 0;

			while (n < depots.size() && (depot2product2properties.get(depots.get(n)) == null
					|| depot2product2properties.get(depots.get(n)).get(productId) == null)) {
				n++;
			}

			if (n == depots.size()) {
				// ready
			} else {
				// create start mergers
				ConfigName2ConfigValue properties = depot2product2properties.get(depots.get(n)).get(productId);

				for (Entry<String, Object> entry : properties.entrySet()) {
					List<?> value = (List<?>) entry.getValue();
					result.put(entry.getKey(), new ListMerger(value));
				}

				// merge the other depots
				for (int i = 1; i < depots.size(); i++) {
					properties = depot2product2properties.get(depots.get(i)).get(productId);

					if (properties == null) {
						Logging.info(this, "mergeProperties, product on depot has not properties " + productId + " on "
								+ depots.get(i));
						continue;
					}

					for (Entry<String, Object> entry : properties.entrySet()) {
						List<?> value = (List<?>) entry.getValue();
						if (result.get(entry.getKey()) == null) {
							// we need a new property. it is not common

							ListMerger merger = new ListMerger(value);

							merger.setHavingNoCommonValue();
							result.put(entry.getKey(), merger);
						} else {
							ListMerger merger = (ListMerger) result.get(entry.getKey());
							result.put(entry.getKey(), merger.merge(value));
						}
					}
				}
			}
		}

		return result;

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
			Logging.debug(this, "resetSelectedDepots, n, selection is " + n + ", -- " + Arrays.toString(selection));

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
		Logging.info(this, "mouseClicked " + e);
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

	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		Logging.debug(this, "actionPerformed " + e);

		if (e.getSource() == buttonSelectWithEqualProperties) {
			selectDepotsWithEqualProperties();
			saveSelectedDepots();
		} else if (e.getSource() == buttonSelectAll) {
			listDepots.setSelectionInterval(0, listDepots.getModel().getSize() - 1);
			saveSelectedDepots();
		}

	}

	private void selectDepotsWithEqualProperties() {
		String selectedDepot0 = listDepots.getSelectedValue();

		if (selectedDepot0 == null || selectedDepot0.equals("")) {
			return;
		}

		ConfigName2ConfigValue properties0 = mainController.getPersistenceController()
				.getDefaultProductProperties(selectedDepot0).get(productEdited);

		int startDepotIndex = listDepots.getSelectedIndex();
		listDepots.setSelectionInterval(startDepotIndex, startDepotIndex);

		for (int i = 0; i < listDepots.getModel().getSize(); i++) {
			String compareDepot = listDepots.getModel().getElementAt(i);

			if (compareDepot.equals(selectedDepot0)) {
				continue;
			}

			ConfigName2ConfigValue compareProperties = mainController.getPersistenceController()
					.getDefaultProductProperties(compareDepot).get(productEdited);

			// True if both objects are equal or both null
			if (Objects.equals(properties0, compareProperties)) {
				listDepots.addSelectionInterval(i, i);
			}
		}
	}
}
