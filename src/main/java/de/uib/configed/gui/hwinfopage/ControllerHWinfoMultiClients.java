/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FPanel;
import de.uib.utils.swing.SecondaryFrame;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.TableModelFilter;
import de.uib.utils.table.TableModelFilterCondition;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapRetriever;
import de.uib.utils.table.provider.RetrieverMapSource;

public class ControllerHWinfoMultiClients {
	private static final int KEY_COL = 0;
	private static final String FILTER_SELECTED_CLIENTS = "visibleClients";
	private static final String DELETE_PREFIX = "HARDWARE_";

	private PanelGenEditTable panel;
	private GenTableModel model;

	private ConfigedMain configedMain;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private TableModelFilter tableModelFilter;

	private SecondaryFrame fTable;

	private TableModelFilterCondition filterConditionHwForSelectedHosts = new FilterConditionHwForSelectedHosts();

	public ControllerHWinfoMultiClients(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		start();
	}

	private void start() {
		filterConditionHwForSelectedHosts.setFilter(new TreeSet<>(configedMain.getClientTable().getSelectedValues()));
		tableModelFilter = new TableModelFilter(filterConditionHwForSelectedHosts, false, true);

		initPanel();
		initModel();

		buildSurrounding();
	}

	public void setFilter() {
		Set<Object> theFilterSet = new TreeSet<>(configedMain.getClientTable().getSelectedValues());
		filterConditionHwForSelectedHosts.setFilter(theFilterSet);
		model.invalidate();
		model.reset();
	}

	public void requestResetFilter() {
		Logging.info(this, "requestResetFilter");
		setFilter();
	}

	private void initPanel() {
		panel = new PanelGenEditTable("", false, 0, new int[] { PanelGenEditTable.POPUP_RELOAD,
				PanelGenEditTable.POPUP_PDF, PanelGenEditTable.POPUP_SORT_AGAIN }, true) {
			@Override
			public void reload() {
				persistenceController.reloadData(ReloadEvent.CLIENT_HARDWARE_RELOAD.toString());
				super.reload();
			}
		};

		// We want to remove the prefix from the header values
		panel.getTheTable().getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof String && ((String) value).startsWith(DELETE_PREFIX)) {
					value = ((String) value).substring(DELETE_PREFIX.length());
				}

				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});

		panel.setMasterFrame(ConfigedMain.getMainFrame());
		panel.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panel.showFilterIcon(true);
		panel.setFiltering(true);
	}

	private void initModel() {
		List<String> columnNames = persistenceController.getHardwareDataService().getClient2HwRowsColumnNamesPD();

		Logging.info(this, "initmodel: columns " + columnNames);
		String[] hosts = new String[0];

		model = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						// Nothing to reload.
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						Logging.info(this, "retrieveMap: getClient2HwRows");
						return persistenceController.getHardwareDataService().getClient2HwRows(hosts);
					}
				})), 0, new int[] { KEY_COL }, panel, null);

		panel.setTableModel(model);
		model.chainFilter(FILTER_SELECTED_CLIENTS, tableModelFilter);
	}

	public void rebuildModel() {
		if (model == null) {
			return;
		}
		// the window exists
		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(ReloadEvent.CLIENT_HARDWARE_RELOAD.toString());
		initModel();

		// we apply filter
		panel.reset();
		translateHostColumns();
	}

	private void buildSurrounding() {
		JButton buttonConfigureColumns = new JButton(Utils.createImageIcon("images/configure16.png", ""));
		buttonConfigureColumns.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.configure"));
		buttonConfigureColumns.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);

		JButton buttonReload = new JButton(Utils.createImageIcon("images/reload16.png", ""));
		buttonReload.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.loadNewConfiguration"));

		buttonReload.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);

		buttonReload.addActionListener((ActionEvent actionEvent) -> {
			Logging.info(this, "action performed " + actionEvent);
			rebuildModel();
		});

		buttonConfigureColumns.addActionListener(this::configureColumns);

		JButton buttonCopySelection = new JButton(Utils.createImageIcon("images/memorize_selection.png", ""));
		buttonCopySelection.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);
		buttonCopySelection.setEnabled(false);

		buttonCopySelection.setToolTipText(Configed.getResourceValue("PanelHWInfo.overview.getSelection"));

		buttonCopySelection.addActionListener(actionEvent -> configedMain.setSelectedClients(panel.getSelectedKeys()));

		panel.setTitlePane(
				new JComponent[] { buttonReload, buttonCopySelection, new JLabel("       "), buttonConfigureColumns },
				20);

		panel.addListSelectionListener(listSelectionEvent -> buttonCopySelection
				.setEnabled(!((ListSelectionModel) listSelectionEvent.getSource()).isSelectionEmpty()));

		translateHostColumns();
	}

	private void translateHostColumns() {
		panel.getColumnModel().getColumn(0)
				.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName"));
		panel.getColumnModel().getColumn(1)
				.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
		panel.getColumnModel().getColumn(2)
				.setHeaderValue(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
		panel.getColumnModel().getColumn(3).setHeaderValue(Configed.getResourceValue("PanelHWInfo.lastScanTime"));
	}

	private void configureColumns(ActionEvent actionEvent) {
		Logging.info(this, "action performed " + actionEvent);
		ControllerHWinfoColumnConfiguration controllerHWinfoColumnConfiguration = new ControllerHWinfoColumnConfiguration();
		if (fTable == null || ((FPanel) fTable).isLeft()) {
			fTable = new FPanel("hardware classes / database columns", controllerHWinfoColumnConfiguration.getPanel(),
					true);

			fTable.setSize(new Dimension(ConfigedMain.getMainFrame().getSize().width - 50,
					ConfigedMain.getMainFrame().getSize().height / 2));
		}
		fTable.centerOnParent();
		fTable.setVisible(true);
	}

	private static final class FilterConditionHwForSelectedHosts implements TableModelFilterCondition {
		private Set<Object> filter;

		private FilterConditionHwForSelectedHosts() {
		}

		@Override
		public void setFilter(Set<Object> filter) {
			this.filter = filter;
		}

		@Override
		public boolean test(List<Object> row) {
			if (filter == null || row == null || KEY_COL >= row.size()) {
				return true;
			}
			return filter.contains(row.get(KEY_COL));
		}

		@Override
		public String toString() {
			String result = "TableModelFilterCondition: filterConditionHwForSelectedHosts, filter == null "
					+ (filter == null);
			if (filter != null) {
				result = result + " size " + filter.size();
			}
			return result;
		}
	}

	public PanelGenEditTable getPanel() {
		return panel;
	}
}
