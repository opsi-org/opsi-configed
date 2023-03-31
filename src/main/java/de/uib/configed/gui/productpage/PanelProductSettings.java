package de.uib.configed.gui.productpage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/*
 *   class PanelProductSettings 
 *   for editing client specific product settings
 *   part of:
 *
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2019 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foun
 *
 */
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.productstate.ActionProgress;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.ActionSequence;
import de.uib.opsidatamodel.productstate.InstallationInfo;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.utilities.IntComparatorForStrings;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.list.StandardListCellRenderer;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.gui.AdaptingCellEditorValuesByIndex;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.DynamicCellEditor;
import de.uib.utilities.table.gui.StandardTableCellRenderer;

public class PanelProductSettings extends JSplitPane implements RowSorterListener {

	private static final List<RowSorter.SortKey> sortkeysDefault = new ArrayList<>();
	static {
		sortkeysDefault.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
	}

	private static final int HEIGHT_MIN = 200;

	private static final int FRAME_WIDTH_LEFTHANDED = 1100;
	private static final int FRAME_HEIGHT = 490;

	private static final int WIDTH_COLUMN_PRODUCT_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_COMPLETE_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_STATE = 60;

	private static final int WIDTH_COLUMN_PRODUCT_SEQUENCE = 40;
	private static final int WIDTH_COLUMN_VERSION_INFO = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PRODUCT_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PACKAGE_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_INSTALLATION_INFO = WIDTH_COLUMN_PRODUCT_STATE;

	JScrollPane paneProducts;
	public JTable tableProducts;
	protected AbstractExportTable exportTable;
	JPanel topPane;

	// right pane
	ProductInfoPane infoPane;
	protected AbstractPanelEditProperties panelEditProperties;
	DefaultEditMapPanel propertiesPanel;

	ListCellRenderer standardListCellRenderer;

	TableCellRenderer productNameTableCellRenderer;
	TableCellRenderer productCompleteNameTableCellRenderer;

	TableCellRenderer targetConfigurationTableCellRenderer;
	TableCellRenderer installationStatusTableCellRenderer;
	TableCellRenderer actionProgressTableCellRenderer;
	TableCellRenderer lastActionTableCellRenderer;
	TableCellRenderer actionResultTableCellRenderer;
	TableCellRenderer actionRequestTableCellRenderer;
	ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	ColoredTableCellRenderer productsequenceTableCellRenderer;
	ColoredTableCellRenderer productversionTableCellRenderer;
	ColoredTableCellRenderer packageversionTableCellRenderer;

	ColoredTableCellRenderer versionInfoTableCellRenderer;
	ColoredTableCellRenderer installationInfoTableCellRenderer;

	ColoredTableCellRenderer positionTableCellRenderer;
	ColoredTableCellRenderer lastStateChangeTableCellRenderer;

	protected Map<String, Boolean> productDisplayFields;

	protected List<? extends SortKey> currentSortKeys;

	protected ArrayList<String> selectedProducts;

	JPopupMenu popup;
	JMenuItem itemOnDemand;

	JMenuItem itemSaveAndExecute;

	protected String title;

	protected ConfigedMain mainController;

	public PanelProductSettings(String title, ConfigedMain mainController, Map<String, Boolean> productDisplayFields) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.title = title;
		this.mainController = mainController;
		this.productDisplayFields = productDisplayFields;
		init();

		super.setResizeWeight(1);
	}

	protected void initTopPane() {
		topPane = new JPanel();
		topPane.setVisible(true);
	}

	protected void init() {
		tableProducts = new JTable() {
			@Override
			public void setValueAt(Object value, int row, int column) {
				List<String> saveSelectedProducts = getSelectedProducts();
				// only in case of setting ActionRequest needed, since we there call
				// fireTableDataChanged
				super.setValueAt(value, row, column);
				setSelection(new HashSet<>(saveSelectedProducts));
			}
		};
		tableProducts.setDragEnabled(true);

		exportTable = new ExporterToCSV(tableProducts);

		initTopPane();

		selectedProducts = new ArrayList<>();

		paneProducts = new JScrollPane();

		paneProducts.getViewport().add(tableProducts);
		paneProducts.setPreferredSize(new Dimension(FRAME_WIDTH_LEFTHANDED, FRAME_HEIGHT));
		paneProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		if (!ConfigedMain.THEMES) {
			tableProducts.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		tableProducts.setShowHorizontalLines(true);
		tableProducts.setGridColor(Globals.PANEL_PRODUCT_SETTINGS_TABLE_GRID_COLOR);
		tableProducts.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		tableProducts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {

				// Ignore extra messages.
				if (e.getValueIsAdjusting()) {
					return;
				}

				clearEditing();

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					Logging.debug(this, "no rows selected");

				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					if (selectedRow != lsm.getMaxSelectionIndex()) {
						// multiselection

					} else {
						Logging.debug(this, "selected " + selectedRow);
						Logging.debug(this, "selected modelIndex " + convertRowIndexToModel(selectedRow));
						Logging.debug(this, "selected  value at "
								+ tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
						mainController.setProductEdited(
								(String) tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
					}

				}

			}
		});

		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		standardListCellRenderer = new StandardListCellRenderer();

		productNameTableCellRenderer = new StandardTableCellRenderer("") {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Will be done if c==null is true since instanceof
				// returns false if null
				if (!(c instanceof JComponent)) {
					return c;
				}

				JComponent jc = (JComponent) c;

				String stateChange = ((IFInstallationStateTableModel) (table.getModel()))
						.getLastStateChange(convertRowIndexToModel(row));

				if (stateChange == null) {
					stateChange = "";
				}

				stateChange = table.getValueAt(row, column).toString() + ", "
						+ Configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange;

				jc.setToolTipText(stateChange);

				return jc;
			}
		};

		productCompleteNameTableCellRenderer = new StandardTableCellRenderer("");

		String iconsDir = null;

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/targetconfiguration";
		}

		targetConfigurationTableCellRenderer = new ColoredTableCellRendererByIndex(
				TargetConfiguration.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_TARGET_CONFIGURATION) + ": ");

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/installationstatus";
		}

		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex(
				InstallationStatus.getLabel2TextColor(), InstallationStatus.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_STATUS) + ": ");

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/actionprogress";
		}

		actionProgressTableCellRenderer = new ActionProgressTableCellRenderer(ActionProgress.getLabel2DisplayLabel(),
				iconsDir, false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_PROGRESS) + ": ");

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/actionresult";
		}

		actionResultTableCellRenderer = new ColoredTableCellRendererByIndex(ActionResult.getLabel2DisplayLabel(),
				iconsDir, false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_RESULT) + ": ");

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/lastaction";
		}

		lastActionTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2DisplayLabel(),
				iconsDir, false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_ACTION) + ": ");

		if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
			iconsDir = "images/productstate/actionrequest";
		}

		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor(),
				ActionRequest.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_REQUEST) + ": ");

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex(ActionSequence.getLabel2DisplayLabel(),
				null, false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_SEQUENCE) + ": "

		);

		lastStateChangeTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE));

		productsequenceTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_POSITION));

		productversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_VERSION));

		packageversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PACKAGE_VERSION));

		versionInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_VERSION_INFO)) {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe since instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;

					if (val.equals("")) {
						return c;
					}

					if (val.equals(InstallationStateTableModel.CONFLICT_STRING)
							|| val.equals(InstallationStateTableModel.UNEQUAL_ADD_STRING
									+ InstallationStateTableModel.CONFLICT_STRING)) {
						if (!ConfigedMain.THEMES) {
							c.setBackground(Globals.CONFLICT_STATE_CELL_COLOR);
							c.setForeground(Globals.CONFLICT_STATE_CELL_COLOR);
						} else {
							c.setBackground(Color.PINK);
							c.setForeground(Color.PINK);
						}

					} else {

						String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
						IFInstallationStateTableModel istm = (IFInstallationStateTableModel) (table.getModel());

						String serverProductVersion = "";

						if (istm.getGlobalProductInfos().get(productId) == null) {
							Logging.warning(this,
									" istm.getGlobalProductInfos()).get(productId) == null for productId " + productId);
						} else {
							serverProductVersion = serverProductVersion
									+ ((istm.getGlobalProductInfos()).get(productId))
											.get(ProductState.KEY_VERSION_INFO);
						}

						if (!val.equals(serverProductVersion)) {
							c.setForeground(Globals.FAILED_COLOR);
						}
					}
				}

				return c;
			}
		};

		installationInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_INFO)) {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;
					if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.FAILED)))) {
						c.setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.SUCCESSFUL)))) {
						c.setForeground(Globals.OK_COLOR);
					}
				}

				return c;
			}
		};

		JPanel leftPane = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(topPane, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setLeftComponent(leftPane);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, true, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		((EditMapPanelX) propertiesPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName()));
		propertiesPanel.registerDataChangedObserver(mainController.getGeneralDataChangedKeeper());
		propertiesPanel.setActor(new DefaultEditMapPanel.Actor() {
			@Override
			protected void reloadData() {
				super.reloadData();
				Logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			protected void saveData() {
				super.saveData();
				Logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			protected void deleteData() {
				super.deleteData();
				Logging.info(this, "we are in PanelProductSettings");
			}
		});

		panelEditProperties = new PanelEditClientProperties(mainController, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		propertiesPanel.registerDataChangedObserver(infoPane);

		infoPane.getPanelProductDependencies().setDependenciesModel(mainController.getDependenciesModel());

		setRightComponent(infoPane);

		producePopupMenu(productDisplayFields);

		paneProducts.addMouseListener(new utils.PopupMouseListener(popup));
		tableProducts.addMouseListener(new utils.PopupMouseListener(popup));

	}

	private static class ActionProgressTableCellRenderer extends ColoredTableCellRendererByIndex {
		ActionProgressTableCellRenderer(Map<String, String> mapOfStringValues, String imagesBase, boolean showOnlyIcon,
				String tooltipPrefix) {
			super(mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
		}

		// overwrite the renderer in order to get the behaviour:
		// - if the cell value is not empty or null, display the installing gif
		// - write the cell value text as tooltip
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component result = null;
			if (value != null && !value.equals("") && !value.toString().equals("null")
					&& !value.toString().equalsIgnoreCase("none")
					&& !value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING)) {
				result = super.getTableCellRendererComponent(table, "installing", isSelected, hasFocus, row, column);

				((JLabel) result)
						.setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));

			} else if (value != null && value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING)) {
				result = super.getTableCellRendererComponent(table, Globals.CONFLICT_STATE_STRING, isSelected, hasFocus,
						row, column);

				((JLabel) result).setToolTipText(Globals
						.fillStringToLength(tooltipPrefix + " " + Globals.CONFLICT_STATE_STRING + " ", FILL_LENGTH));
			} else {
				result = super.getTableCellRendererComponent(table, "none", isSelected, hasFocus, row, column);

				((JLabel) result).setToolTipText(Globals.fillStringToLength(
						tooltipPrefix + " " + ActionProgress.getDisplayLabel(ActionProgress.NONE) + " ", FILL_LENGTH));
			}

			return result;
		}
	}

	public void initAllProperties() {
		propertiesPanel.init();
		infoPane.setProductInfo("");
		infoPane.setProductAdvice("");
	}

	protected void producePopupMenu(final Map<String, Boolean> checkColumns) {
		popup = new JPopupMenu("");

		JMenuItem save = new JMenuItemFormatted();
		save.setText(Configed.getResourceValue("ConfigedMain.saveConfiguration"));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		save.setFont(Globals.defaultFont);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionevent on save-menue");
				mainController.checkSaveAll(false);
				mainController.requestReloadStatesAndActions();
			}
		});
		popup.add(save);

		itemOnDemand = new JMenuItemFormatted();
		itemOnDemand.setText(Configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"));
		itemOnDemand.setFont(Globals.defaultFont);
		itemOnDemand.addActionListener((ActionEvent e) -> mainController
				.fireOpsiclientdEventOnSelectedClients(AbstractPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND));

		popup.add(itemOnDemand);

		itemSaveAndExecute = new JMenuItemFormatted();
		itemSaveAndExecute.setText(Configed.getResourceValue("ConfigedMain.savePOCAndExecute"));

		// dies bit get its intended context
		itemSaveAndExecute.setIcon(Globals.createImageIcon("images/executing_command_blue_16.png", ""));
		itemSaveAndExecute.setFont(Globals.defaultFont);
		itemSaveAndExecute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionevent on save and execute menu item");
				saveAndExecuteAction();
			}
		});
		popup.add(itemSaveAndExecute);
		popup.addSeparator();

		showPopupOpsiclientdEvent(true);

		JMenuItem reload = new JMenuItemFormatted();

		// find itscontext
		reload.setText(Configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Globals.createImageIcon("images/reload16.png", ""));
		reload.setFont(Globals.defaultFont);
		reload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "------------- reload action");
				reloadAction();
			}
		});
		popup.add(reload);

		JMenuItem createReport = new JMenuItemFormatted();
		createReport.setText(Configed.getResourceValue("PanelProductSettings.pdf"));
		createReport.setIcon(Globals.createImageIcon("images/acrobat_reader16.png", ""));
		createReport.setFont(Globals.defaultFont);
		createReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "------------- create report");
				HashMap<String, String> metaData = new HashMap<>();

				// TODO: getFilter
				// display, if filter is active,
				// display selected productgroup
				// depot server, selected clients out of statusPane

				metaData.put("header", title);
				metaData.put("subject", title);
				title = "";
				if (mainController.getHostsStatusInfo().getInvolvedDepots().length() != 0) {
					title = title + "Depot : " + mainController.getHostsStatusInfo().getInvolvedDepots();
				}
				if (mainController.getHostsStatusInfo().getSelectedClientNames().length() != 0) {
					title = title + "; Clients: " + mainController.getHostsStatusInfo().getSelectedClientNames();
				}
				metaData.put("title", title);
				metaData.put("keywords", "product settings");

				// only relevent rows
				ExporterToPDF pdfExportTable = new ExporterToPDF(strippTable(tableProducts));

				pdfExportTable.setMetaData(metaData);
				pdfExportTable.setPageSizeA4Landscape();

				// create pdf 
				pdfExportTable.execute(null, false);

			}
		});
		popup.add(createReport);

		exportTable.addMenuItemsTo(popup);

		JMenu sub = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		sub.setFont(Globals.defaultFont);
		popup.addSeparator();
		popup.add(sub);

		Iterator<String> iter = checkColumns.keySet().iterator();

		while (iter.hasNext()) {
			final String columnName = iter.next();

			if (columnName.equals("productId")) {
				// fixed column
				continue;
			}

			JMenuItem item = new JCheckBoxMenuItem();
			item.setText(InstallationStateTableModel.getColumnTitle(columnName));
			item.setFont(Globals.defaultFont);
			((JCheckBoxMenuItem) item).setState(checkColumns.get(columnName));
			item.addItemListener((ItemEvent e) -> {
				boolean oldstate = checkColumns.get(columnName);
				checkColumns.put(columnName, !oldstate);
				mainController.requestReloadStatesAndActions();
				mainController.resetView(mainController.getViewIndex());
			});

			sub.add(item);
		}
	}

	protected JTable strippTable(JTable jTable) {
		boolean dontStrippIt;
		List<String[]> data = new ArrayList<>();
		String[] headers = new String[jTable.getColumnCount()];
		for (int i = 0; i < jTable.getColumnCount(); i++) {
			headers[i] = jTable.getColumnName(i);
		}

		for (int j = 0; j < jTable.getRowCount(); j++) {
			dontStrippIt = false;
			String[] actCol = new String[jTable.getColumnCount()];
			for (int i = 0; i < jTable.getColumnCount(); i++) {

				String s;
				Object cellValue = jTable.getValueAt(j, i);

				if (cellValue == null) {
					s = "";
				} else {
					s = cellValue.toString();
				}

				actCol[i] = s;

				switch (jTable.getColumnName(i)) {
				case "Stand":
					if (!s.equals(InstallationStatus.KEY_NOT_INSTALLED)) {
						dontStrippIt = true;
					}
					break;
				case "Report":
					if (!s.equals("")) {
						dontStrippIt = true;
					}
					break;
				case "Angefordert":
					if (!s.equals("none")) {
						dontStrippIt = true;
					}
					break;
				default:
					Logging.warning(this, "no case found for columnName in jTable");
					break;
				}
			}
			if (dontStrippIt) {
				data.add(actCol);
			}

		}
		// create jTable with selected rows
		int rows = data.size();
		int cols = jTable.getColumnCount();
		String[][] strippedData = new String[rows][cols];
		for (int i = 0; i < data.size(); i++) {
			strippedData[i] = data.get(i);
		}
		return new JTable(strippedData, headers);
	}

	protected void reloadAction() {

		mainController.requestReloadStatesAndActions();
		mainController.resetView(mainController.getViewIndex());
		mainController.setDataChanged(false);
	}

	protected void saveAndExecuteAction() {
		Logging.info(this, "saveAndExecuteAction");
		mainController.checkSaveAll(false);
		mainController.requestReloadStatesAndActions();

		mainController
				.fireOpsiclientdEventOnSelectedClients(AbstractPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);

	}

	private String infoSortKeys(List<? extends RowSorter.SortKey> sortKeys) {
		if (sortKeys == null) {
			return "null";
		}

		StringBuilder result = new StringBuilder("[");
		int i = 0;
		for (RowSorter.SortKey key : sortKeys) {
			i++;
			result.append(key.getColumn() + ".." + key);
		}
		result.append("]");
		Logging.info(this, "infoSortkeys " + result);
		return " (number " + i + ") ";

	}

	public List<? extends SortKey> getSortKeys() {
		Logging.info(this, "getSortKeys : " + infoSortKeys(currentSortKeys));
		return currentSortKeys;
	}

	public void setSortKeys(List<? extends SortKey> currentSortKeys) {
		Logging.info(this, "setSortKeys: " + infoSortKeys(currentSortKeys));
		if (currentSortKeys != null) {
			tableProducts.getRowSorter().setSortKeys(currentSortKeys);
		}
	}

	public void showPopupOpsiclientdEvent(boolean visible) {

		itemOnDemand.setVisible(visible);
		itemSaveAndExecute.setVisible(visible);
	}

	public void clearSelection() {
		tableProducts.clearSelection();
	}

	public void setSelection(Set<String> selectedIDs) {
		clearSelection();
		if (selectedIDs != null) {
			if (selectedIDs.isEmpty() && tableProducts.getRowCount() > 0) {
				tableProducts.addRowSelectionInterval(0, 0);
				// show first product if no product given
				Logging.info(this, "setSelection 0");
			} else {
				for (int row = 0; row < tableProducts.getRowCount(); row++) {
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId)) {
						tableProducts.addRowSelectionInterval(row, row);
					}
				}
			}
		}
	}

	public Set<String> getSelectedIDs() {
		HashSet<String> result = new HashSet<>();

		int[] selection = tableProducts.getSelectedRows();

		for (int i = 0; i < selection.length; i++) {
			result.add((String) tableProducts.getValueAt(selection[i], 0));
		}

		return result;
	}

	public int convertRowIndexToModel(int row) {
		return tableProducts.convertRowIndexToModel(row);
	}

	public List<Integer> getSelectedRowsInModelTerms() {
		int[] selection = tableProducts.getSelectedRows();
		ArrayList<Integer> selectionInModelTerms = new ArrayList<>(selection.length);
		for (int i = 0; i < selection.length; i++) {
			selectionInModelTerms.add(convertRowIndexToModel(selection[i]));
		}

		return selectionInModelTerms;
	}

	private static class StringComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {

			return o1.compareTo(o2);
		}
	}

	public void setTableModel(IFInstallationStateTableModel istm) {

		// delete old row sorter before setting new model
		tableProducts.setRowSorter(null);

		tableProducts.setModel(istm);

		// try bugfix:

		final StringComparator myComparator = new StringComparator();

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableProducts.getModel()) {

			@Override
			protected boolean useToString(int column) {
				try {
					return super.useToString(column);
				} catch (Exception ex) {
					Logging.info(this, "------------------- no way to string");
					return false;
				}
			}

			@Override
			public Comparator<?> getComparator(int column) {
				try {
					if (column == 0) {
						return myComparator;
					} else {
						return super.getComparator(column);
					}
				} catch (Exception ex) {
					Logging.info(this, "------------------- not getting comparator ");
					return null;
				}

			}
		}

		;

		tableProducts.setRowSorter(sorter);
		sorter.addRowSorterListener(this);

		tableProducts.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(tableProducts.getTableHeader().getDefaultRenderer()));

		// ---

		Logging.debug(this, " tableProducts columns  count " + tableProducts.getColumnCount());
		Enumeration<TableColumn> enumer = tableProducts.getColumnModel().getColumns();

		while (enumer.hasMoreElements()) {
			Logging.debug(this, " tableProducts column  " + enumer.nextElement().getHeaderValue());
		}

		int colIndex = -1;

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_ID)) > -1) {
			TableColumn nameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_NAME);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_NAME)) > -1) {
			TableColumn completeNameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_COMPLETE_NAME);
			completeNameColumn.setCellRenderer(productCompleteNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_TARGET_CONFIGURATION)) > -1) {
			TableColumn targetColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
				iconsDir = "images/productstate/targetconfiguration";
			}

			JComboBox<String> targetCombo = new JComboBox<>();
			targetCombo.setRenderer(standardListCellRenderer);

			targetColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(targetCombo, istm,
					TargetConfiguration.getLabel2DisplayLabel(), iconsDir));
			targetColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			targetColumn.setCellRenderer(targetConfigurationTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_STATUS)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
				iconsDir = "images/productstate/installationstatus";
			}

			JComboBox<String> statesCombo = new JComboBox<>();
			statesCombo.setRenderer(standardListCellRenderer);

			statusColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(statesCombo, istm,
					InstallationStatus.getLabel2DisplayLabel(), iconsDir));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_PROGRESS)) > -1) {
			TableColumn progressColumn = tableProducts.getColumnModel().getColumn(colIndex);

			progressColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			progressColumn.setCellRenderer(actionProgressTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_ACTION)) > -1) {
			TableColumn lastactionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastactionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			lastactionColumn.setCellRenderer(lastActionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_RESULT)) > -1) {
			TableColumn actionresultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionresultColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionresultColumn.setCellRenderer(actionResultTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_REQUEST)) > -1) {

			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.SHOW_ICONS_IN_PRODUCT_TABLE) {
				iconsDir = "images/productstate/actionrequest";
			}

			JComboBox<String> actionsCombo = new JComboBox<>();
			actionsCombo.setRenderer(standardListCellRenderer);
			actionColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(actionsCombo, istm,
					ActionRequest.getLabel2DisplayLabel(), iconsDir));
			actionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionColumn.setCellRenderer(actionRequestTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_STATE_CHANGE)) > -1) {
			TableColumn laststatechangeColumn = tableProducts.getColumnModel().getColumn(colIndex);
			laststatechangeColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			laststatechangeColumn.setCellRenderer(lastStateChangeTableCellRenderer);

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_SEQUENCE)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			if (sorter instanceof DefaultRowSorter) {
				((DefaultRowSorter<?, ?>) sorter).setComparator(colIndex, new IntComparatorForStrings());
			}

			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_PRIORITY)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			((DefaultRowSorter<?, ?>) sorter).setComparator(colIndex, new IntComparatorForStrings());

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_POSITION)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			// we already have Integer

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_VERSION)) > -1) {
			TableColumn productversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productversionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_VERSION);
			productversionColumn.setCellRenderer(productversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PACKAGE_VERSION)) > -1) {

			TableColumn packageversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			packageversionColumn.setPreferredWidth(WIDTH_COLUMN_PACKAGE_VERSION);
			packageversionColumn.setCellRenderer(packageversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_VERSION_INFO)) > -1) {
			TableColumn versionInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(WIDTH_COLUMN_VERSION_INFO);
			versionInfoColumn.setCellRenderer(versionInfoTableCellRenderer);

			((DefaultRowSorter<?, ?>) sorter).setComparator(colIndex, new StringComparator());

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_INFO)) > -1) {
			TableColumn installationInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			installationInfoColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			installationInfoColumn.setCellRenderer(installationInfoTableCellRenderer);

			JComboBox<String> installationInfoCombo = new JComboBox<>();

			installationInfoCombo.setRenderer(standardListCellRenderer);

			DynamicCellEditor cellEditor = new DynamicCellEditor(installationInfoCombo, istm,
					InstallationInfo.defaultDisplayValues);

			installationInfoColumn.setCellEditor(cellEditor);
		}

		sorter.setSortKeys(sortkeysDefault);

	}

	public void initEditing(String productID, String productTitle, String productInfo, String productHint,
			String productVersion, Collection<Map<String, Object>> storableProductProperties,
			Map editableProductProperties,
			// editmappanelx
			Map<String, ListCellOptions> productpropertyOptionsMap,
			ProductpropertiesUpdateCollection updateCollection) {
		infoPane.setGrey(false);
		infoPane.setProductId(productID);
		infoPane.setProductName(productTitle);
		infoPane.setProductInfo(productInfo);
		infoPane.setProductVersion(productVersion);

		infoPane.setProductAdvice(productHint);

		Globals.checkCollection(this, "editableProductProperties ", editableProductProperties);
		Globals.checkCollection(this, "productpropertyOptionsMap", productpropertyOptionsMap);

		propertiesPanel.setEditableMap(
				// visualMap (merged for different clients)
				editableProductProperties, productpropertyOptionsMap);

		propertiesPanel.setStoreData(storableProductProperties);
		propertiesPanel.setUpdateCollection(updateCollection);

	}

	public void clearListEditors() {
		if (propertiesPanel instanceof EditMapPanelX) {
			((EditMapPanelX) propertiesPanel).cancelOldCellEditing();
		}
	}

	public void clearEditing() {

		initEditing("", "", "", "", "", null, null, null, null);
		infoPane.setGrey(true);
	}

	// RowSorterListener for table row sorter
	@Override
	public void sorterChanged(RowSorterEvent e) {
		Logging.info(this, "RowSorterEvent " + e);
		currentSortKeys = tableProducts.getRowSorter().getSortKeys();
		Logging.info(this, "sorterChanged, sortKeys: " + infoSortKeys(currentSortKeys));

	}

	public List<String> getSelectedProducts() {
		// in model terms

		List<Integer> selectedRows = getSelectedRowsInModelTerms();

		List<String> selectedProductsList = new ArrayList<>();

		for (int row : selectedRows) {
			selectedProductsList.add((String) tableProducts.getModel().getValueAt(row, 0));
		}

		Logging.info(this, "selectedProducts " + selectedProductsList);

		return selectedProductsList;
	}
}
