/*
 * ControllerHWinfoMultiClients
 *
 * Copyright (c) 2018 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 * author: Rupert Röder
 */

package de.uib.configed.gui.hwinfopage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FPanel;
import de.uib.utilities.swing.SecondaryFrame;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;

public class ControllerHWinfoMultiClients {
	public PanelGenEditTable panel;
	private GenTableModel model;

	JButton buttonConfigureColumns;

	JButton buttonReload;
	JButton buttonCopySelection;

	List<String> columnNames;
	List<String> classNames;

	TreeSet<Object> theFilterSet;

	String[] hosts;
	ConfigedMain main;
	protected PersistenceController persist;

	private static final int KEY_COL = 0;
	private static final String FILTER_SELECTED_CLIENTS = "visibleClients";
	private static final String DELETE_PREFIX = "HARDWARE_";

	TableModelFilter tableModelFilter;

	SecondaryFrame fTable;

	de.uib.utilities.table.TableModelFilterCondition filterConditionHwForSelectedHosts = new de.uib.utilities.table.TableModelFilterCondition() {
		private Set<Object> filter;

		@Override
		public void setFilter(Set<Object> filter) {
			this.filter = filter;
		}

		@Override
		public boolean test(List<Object> row) {
			if (filter == null || row == null || KEY_COL >= row.size())
				return true;

			return filter.contains(row.get(KEY_COL));

		}

		@Override
		public String toString() {
			String result = "TableModelFilterCondition: filterConditionHwForSelectedHosts, filter == null "
					+ (filter == null);
			if (filter != null)
				result = result + " size " + filter.size();

			return result;
		}

	};

	public ControllerHWinfoMultiClients(ConfigedMain main, PersistenceController persist) {
		this.main = main;
		this.persist = persist;
		start();
	}

	private void start() {
		filterConditionHwForSelectedHosts.setFilter(new TreeSet<>(main.getSelectedClientsInTable()));
		tableModelFilter = new TableModelFilter(filterConditionHwForSelectedHosts, false, true);

		initPanel();
		initModel();

		buildSurrounding();
	}

	public void setFilter() {
		theFilterSet = new TreeSet<>(main.getSelectedClientsInTable());
		filterConditionHwForSelectedHosts.setFilter(theFilterSet);
		model.invalidate();
		model.reset();

	}

	public void requestResetFilter() {
		Logging.info(this, "requestResetFilter");

		setFilter();

	}

	protected void initPanel() {
		panel = new PanelGenEditTable("", 0, false, 0, false,

				PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF, true) {
			@Override
			public void reload() {

				persist.client2HwRowsRequestRefresh();

				super.reload();

			}

			@Override
			protected Object modifyHeaderValue(Object s) {
				if (s instanceof String && ((String) s).startsWith(DELETE_PREFIX)) {
					return ((String) s).substring(DELETE_PREFIX.length());

				}

				return s;
			}
		};

		panel.setMasterFrame(ConfigedMain.getMainFrame());
		panel.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panel.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panel.setFiltering(true);

	}

	protected void initModel() {

		columnNames = persist.getClient2HwRowsColumnNames();
		classNames = persist.getClient2HwRowsJavaclassNames();
		Logging.info(this, "initmodel: columns " + columnNames);
		hosts = new String[0];

		// GenericTableUpdateItemFactory updateItemFactory = new

		model = new GenTableModel(
				// updateItemFactory,
				null,

				// tableProvider

				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
					Logging.info(this, "retrieveMap: getClient2HwRows");

					return (Map) persist.getClient2HwRows(hosts);
				})),

				// keycol
				0,

				// final columns int array
				new int[] { KEY_COL },

				// table model listener
				panel,

				// TableUpdateCollection updates
				// updateCollection
				null);

		// we got metadata:

		panel.setTableModel(model);

		model.chainFilter(FILTER_SELECTED_CLIENTS, tableModelFilter);

	}

	public void rebuildModel() {
		if (model == null)
			return;
		// the window exists
		persist.configOptionsRequestRefresh();
		persist.client2HwRowsRequestRefresh();
		initModel();
		panel.reset(); // we apply filter
	}

	protected void buildSurrounding() {

		// Icon iconConfigure =

		buttonConfigureColumns = new JButton("", Globals.createImageIcon("images/configure16.png", ""));
		buttonConfigureColumns.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.configure"));
		buttonConfigureColumns.setPreferredSize(Globals.smallButtonDimension);

		buttonReload = new JButton("", Globals.createImageIcon("images/reload16.png", ""));
		buttonReload.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.loadNewConfiguration"));

		buttonReload.setPreferredSize(Globals.smallButtonDimension);

		buttonReload.addActionListener(actionEvent -> {
			Logging.info(this, "action performed " + actionEvent);
			rebuildModel();
		});

		buttonConfigureColumns.addActionListener(actionEvent -> {
			Logging.info(this, "action performed " + actionEvent);

			ControllerHWinfoColumnConfiguration controllerHWinfoColumnConfiguration = new ControllerHWinfoColumnConfiguration(
					main, persist);
			if (fTable == null || ((FPanel) fTable).isLeft()) {
				fTable = new FPanel("hardware classes / database columns", controllerHWinfoColumnConfiguration.panel,
						// testpanel,
						true);

				fTable.setSize(new java.awt.Dimension(ConfigedMain.getMainFrame().getSize().width - 50,
						ConfigedMain.getMainFrame().getSize().height / 2));
			}

			fTable.centerOnParent();

			fTable.setVisible(true);
		});

		buttonCopySelection = new JButton("", Globals.createImageIcon("images/memorize_selection.png", ""));
		buttonCopySelection.setPreferredSize(Globals.smallButtonDimension);
		buttonCopySelection.setEnabled(false);

		buttonCopySelection.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.getSelection"));

		buttonCopySelection.addActionListener(
				actionEvent -> main.setSelectedClientsCollectionOnPanel(panel.getSelectedKeys(), true));

		panel.setTitlePane(new JComponent[] {

				buttonReload, buttonCopySelection, new JLabel("       "), buttonConfigureColumns }, 20);
		panel.setTitlePaneBackground(Globals.BACKGROUND_COLOR_7);

		panel.addListSelectionListener(listSelectionEvent -> buttonCopySelection
				.setEnabled(!((ListSelectionModel) listSelectionEvent.getSource()).isSelectionEmpty()));

		javax.swing.table.TableColumn col;
		col = panel.getColumnModel().getColumn(0);
		col.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName"));
		col = panel.getColumnModel().getColumn(1);
		col.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
		col = panel.getColumnModel().getColumn(2);
		col.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
		col = panel.getColumnModel().getColumn(3);
		col.setHeaderValue(Configed.getResourceValue("PanelHWInfo.lastScanTime"));

	}

}
