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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.FPanel;
import de.uib.utilities.swing.SecondaryFrame;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;

public class ControllerHWinfoMultiClients {
	public PanelGenEditTable panel;
	private GenTableModel model;

	JButton buttonConfigureColumns;
	// CheckedLabel buttonConfigureColumns;
	JButton buttonReload;
	JButton buttonCopySelection;

	Vector<String> columnNames;
	Vector<String> classNames;

	TreeSet theFilterSet;

	String[] hosts;
	ConfigedMain main;
	protected PersistenceController persist;

	static final int keycol = 0;
	static final String FILTER_SELECTED_CLIENTS = "visibleClients";
	static final String DELETE_PREFIX = "HARDWARE_";

	TableModelFilter tableModelFilter;

	SecondaryFrame fTable;

	de.uib.utilities.table.TableModelFilterCondition filterConditionHwForSelectedHosts = new de.uib.utilities.table.TableModelFilterCondition() {
		private TreeSet<Object> filter;

		public void setFilter(TreeSet<Object> filter) {
			this.filter = filter;
		}

		public boolean test(Vector<Object> row) {
			if (filter == null || row == null || keycol >= row.size())
				return true;

			return filter.contains(row.get(keycol));

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
		filterConditionHwForSelectedHosts.setFilter(new TreeSet(main.getSelectedClientsInTable()));
		tableModelFilter = new TableModelFilter(filterConditionHwForSelectedHosts, false, true);

		initPanel();
		initModel();

		buildSurrounding();
	}

	public void setFilter() {
		theFilterSet = new TreeSet(main.getSelectedClientsInTable());
		filterConditionHwForSelectedHosts.setFilter(theFilterSet);
		model.invalidate(); // requestRefilter();
		model.reset();
		// model.setFilterCondition( filterConditionHwForSelectedHosts );
	}

	public void requestResetFilter() {
		logging.info(this, "requestResetFilter");

		setFilter();

	}

	protected void initPanel() {
		panel = new PanelGenEditTable("", // configed.getResourceValue("HardwareList"),
				0, false, 0, false,
				// new int[]{PanelGenEditTable.POPUP_RELOAD, PanelGenEditTable.POPUP_PDF},
				PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF, true) {
			@Override
			public void reload() {
				// persist.configOptionsRequestRefresh();
				persist.client2HwRowsRequestRefresh();
				// columnNames = persist.getClient2HwRowsColumnNames();
				// classNames = persist.getClient2HwRowsJavaclassNames();
				super.reload();
				// getTableModel().fireTableStructureChanged();

			}

			@Override
			protected Object modifyHeaderValue(Object s) {
				if (s != null && s instanceof String && ((String) s).startsWith(DELETE_PREFIX)) {
					String modified = ((String) s).substring(DELETE_PREFIX.length());
					return modified;
				}

				return s;
			}
		};

		panel.setMasterFrame(de.uib.configed.Globals.mainFrame);
		panel.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panel.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panel.setFiltering(true);

	}

	protected void initModel() {

		// updateCollection = new TableUpdateCollection();
		columnNames = persist.getClient2HwRowsColumnNames();
		classNames = persist.getClient2HwRowsJavaclassNames();
		logging.info(this, "initmodel: columns " + columnNames);
		hosts = new String[0];

		// GenericTableUpdateItemFactory updateItemFactory = new
		// GenericTableUpdateItemFactory(0);

		model = new GenTableModel(
				// updateItemFactory,
				null,

				// tableProvider
				// new de.uib.utilities.table.provider.DefaultTableProvider(sqlSource),
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					public Map retrieveMap() {
						logging.info(this, "retrieveMap: getClient2HwRows");

						Map<String, Map<String, Object>> hwInfos = persist.getClient2HwRows(hosts);

						return hwInfos;
					}
				})),

				// keycol
				0,

				// final columns int array
				new int[] { keycol },

				// table model listener
				panel,

				// TableUpdateCollection updates
				// updateCollection
				null);

		// updateItemFactory.setSource(model);

		// model.reset();
		// we got metadata:

		// columnNames = model.getColumnNames();
		// classNames = model.getClassNames();

		// updateItemFactory.setColumnNames(columnNames);
		// updateItemFactory.setClassNames(classNames);

		panel.setTableModel(model);
		// panel.setEmphasizedColumns(new int[]{2});

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

		// panel.setTitle( "" );//configed.getResourceValue("PanelHWInfo.overview") );

		// Icon iconConfigure =
		// de.uib.configed.Globals.createImageIcon("images/config_pro.png", "");
		// buttonConfigureColumns = new JButton("...");
		buttonConfigureColumns = new JButton("", de.uib.configed.Globals.createImageIcon("images/configure16.png", ""));
		buttonConfigureColumns.setToolTipText(configed.getResourceValue("PanelHWInfo.overview.configure"));
		buttonConfigureColumns.setPreferredSize(de.uib.configed.Globals.smallButtonDimension);

		buttonReload = new JButton("", de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
		buttonReload.setToolTipText(configed.getResourceValue("PanelHWInfo.overview.loadNewConfiguration"));

		buttonReload.setPreferredSize(de.uib.configed.Globals.smallButtonDimension);

		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "action performed " + e);
				rebuildModel();
				/*
				 * persist.configOptionsRequestRefresh();
				 * persist.client2HwRowsRequestRefresh();
				 * initModel();
				 * panel.reset(); //we apply filter
				 * //panel.getTableModel().fireTableStructureChanged();
				 * //panel.reload();
				 * //logging.info(this, "after reload: columns " +
				 * panel.getTableModel().getColumnNames() );
				 */
			}
		});

		// JPanel testpanel = new JPanel();
		// testpanel.add( new JLabel ("hallo welt") );

		buttonConfigureColumns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "action performed " + e);

				ControllerHWinfoColumnConfiguration controllerHWinfoColumnConfiguration = new ControllerHWinfoColumnConfiguration(
						main, persist);
				if (fTable == null || ((FPanel) fTable).isLeft()) {
					fTable = new FPanel("hardware classes / database columns",
							controllerHWinfoColumnConfiguration.panel,
							// testpanel,
							true);

					fTable.setSize(new java.awt.Dimension(de.uib.utilities.Globals.masterFrame.getSize().width - 50,
							de.uib.utilities.Globals.masterFrame.getSize().height / 2));
				}

				fTable.centerOnParent();

				fTable.setVisible(true);

			}
		});

		buttonCopySelection = new JButton("",
				de.uib.configed.Globals.createImageIcon("images/memorize_selection.png", ""));
		buttonCopySelection.setPreferredSize(de.uib.configed.Globals.smallButtonDimension);
		buttonCopySelection.setEnabled(false);

		buttonCopySelection.setToolTipText(configed.getResourceValue("PanelHWInfo.overview.getSelection"));

		buttonCopySelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// logging.info(this, "action performed " + e);
				// logging.info(this, "selection empty " + panel.isSelectionEmpty());
				// panel.getSelectedKeys();
				main.setSelectedClientsCollectionOnPanel(panel.getSelectedKeys(), true);
			}
		});

		panel.setTitlePane(new JComponent[] {
				// buttonConfigureColumns, buttonReload }, 20 );
				buttonReload, buttonCopySelection, new JLabel("       "), buttonConfigureColumns }, 20);
		panel.setTitlePaneBackground(de.uib.utilities.Globals.backLightBlue);

		panel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// logging.info(this, "val changed " + e);
				buttonCopySelection.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
			}
		});

		javax.swing.table.TableColumn col;
		col = panel.getColumnModel().getColumn(0);
		col.setHeaderValue(configed.getResourceValue("ConfigedMain.pclistTableModel.clientName"));
		col = panel.getColumnModel().getColumn(1);
		col.setHeaderValue(configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
		col = panel.getColumnModel().getColumn(2);
		col.setHeaderValue(configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
		col = panel.getColumnModel().getColumn(3);
		col.setHeaderValue(configed.getResourceValue("PanelHWInfo.lastScanTime"));

	}

}
