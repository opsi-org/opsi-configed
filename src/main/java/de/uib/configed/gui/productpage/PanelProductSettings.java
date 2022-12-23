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
import java.util.Vector;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
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
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.productstate.ActionProgress;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.InstallationInfo;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.datapanel.AbstractEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.list.StandardListCellRenderer;
import de.uib.utilities.table.ExportTable;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.gui.AdaptingCellEditorValuesByIndex;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.DynamicCellEditor;
import de.uib.utilities.table.gui.StandardTableCellRenderer;

public class PanelProductSettings extends JSplitPane implements RowSorterListener {

	private static final List<RowSorter.SortKey> sortkeysDefault = new ArrayList<>();
	static {
		sortkeysDefault.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
	}

	JScrollPane paneProducts;
	public JTable tableProducts;
	protected ExportTable exportTable;
	JPanel topPane;

	ProductInfoPane infoPane; // right pane
	protected DefaultPanelEditProperties panelEditProperties;
	AbstractEditMapPanel propertiesPanel;

	protected int hMin = 200;

	final int fwidth_lefthanded = 800;
	final int splitterLeftRight = 15;
	final int fheight = 450;

	final int fwidth_column_productname = 170;
	final int fwidth_column_productcompletename = 170;
	final int fwidth_column_productstate = 60;
	final int fwidth_column_productposition = 40;
	// final int fwidth_column_productinstallationstatus = 100;
	// final int fwidth_column_productaction = 100;
	final int fwidth_column_productsequence = fwidth_column_productposition;
	final int fwidth_column_versionInfo = fwidth_column_productstate;
	final int fwidth_column_productversion = fwidth_column_productstate;
	final int fwidth_column_packageversion = fwidth_column_productstate;
	final int fwidth_column_installationInfo = fwidth_column_productstate;

	
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

	TableCellRenderer propertiesTableCellRenderer;

	protected Map<String, Boolean> productDisplayFields;

	protected List<? extends RowSorter.SortKey> currentSortKeys;

	protected ArrayList<String> selectedProducts;

	JPopupMenu popup;
	JMenu subOpsiclientdEvent;
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

		setDividerLocation(fwidth_lefthanded);
		setResizeWeight(0.5);

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
		paneProducts.setPreferredSize(new Dimension(fwidth_lefthanded, fheight + 40));
		paneProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		tableProducts.setBackground(Globals.backgroundWhite);
		tableProducts.setShowHorizontalLines(true);
		tableProducts.setGridColor(Color.WHITE);
		tableProducts.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		// final PanelProductSettings THIS = this;

		tableProducts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				// valueChanged");

				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				
				clearEditing();
				

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					logging.debug(this, "no rows selected");

				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					if (selectedRow != lsm.getMaxSelectionIndex()) {
						// multiselection

					}

					else {
						logging.debug(this, "selected " + selectedRow);
						logging.debug(this, "selected modelIndex " + convertRowIndexToModel(selectedRow));
						logging.debug(this, "selected  value at "
								+ tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
						mainController.setProductEdited(
								(String) tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
					}

					/*
					 * int[] selection = tableProducts.getSelectedRows();
					 * 
					 * for (int i = 0; i < selection.length; i++)
					 * {
					 * selectedProducts.add(
					 * (String) tableProducts.getValueAt( selection[i] , 0 )
					 * );
					 * }
					 */
				}

				

			}
		});

		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		standardListCellRenderer = new StandardListCellRenderer();
		

		productNameTableCellRenderer = new StandardTableCellRenderer("") {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, // value to display
					boolean isSelected, // is the cell selected
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Will be done if c==null is true since instanceof
				// returns false if null
				if (!(c instanceof JComponent))
					return c;

				JComponent jc = (JComponent) c;

				String stateChange = ((IFInstallationStateTableModel) (table.getModel()))
						.getLastStateChange(convertRowIndexToModel(row));

				
				// stateChange " + stateChange);

				if (stateChange == null)
					stateChange = "";

				stateChange = table.getValueAt(row, column).toString() + ", "
						+ configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange;

				jc.setToolTipText(stateChange);

				return jc;
			}
		};

		productCompleteNameTableCellRenderer = new StandardTableCellRenderer("");

		String iconsDir = null;

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/targetconfiguration";
		targetConfigurationTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.TargetConfiguration.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_targetConfiguration) + ": ");

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/installationstatus";
		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2TextColor(),
				de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_installationStatus) + ": ");

		class ActionProgressTableCellRenderer extends ColoredTableCellRendererByIndex {
			ActionProgressTableCellRenderer(Map<String, String> mapOfStringValues, String imagesBase,
					boolean showOnlyIcon, String tooltipPrefix) {
				super(mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
			}

			// overwrite the renderer in order to get the behaviour:
			// - if the cell value is not empty or null, display the installing gif
			// - write the cell value text as tooltip
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, // value to display
					boolean isSelected, // is the cell selected
					boolean hasFocus, int row, int column) {
				
				Component result = null;
				if (value != null && !value.equals("") && !value.toString().equals("null")
						&& !value.toString().equalsIgnoreCase("none")
						&& !value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING)) {
					result = super.getTableCellRendererComponent(table, "installing", isSelected, hasFocus, row,
							column);

					((JLabel) result)
							.setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));

				} else if (value != null && value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING)) {
					result = super.getTableCellRendererComponent(table, Globals.CONFLICT_STATE_STRING, isSelected,
							hasFocus, row, column);

					((JLabel) result).setToolTipText(Globals.fillStringToLength(
							tooltipPrefix + " " + Globals.CONFLICT_STATE_STRING + " ", FILL_LENGTH));
				}

				else {
					result = super.getTableCellRendererComponent(table, "none", isSelected, hasFocus, row, column);

					((JLabel) result).setToolTipText(Globals.fillStringToLength(
							tooltipPrefix + " " + ActionProgress.getDisplayLabel(ActionProgress.NONE) + " ",
							FILL_LENGTH));
				}

				return result;
			}
		}

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionprogress";

		actionProgressTableCellRenderer = new ActionProgressTableCellRenderer(
				de.uib.opsidatamodel.productstate.ActionProgress.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionProgress) + ": ");

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionresult";

		actionResultTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.ActionResult.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionResult) + ": ");

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/lastaction";

		lastActionTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_lastAction) + ": ");

		if (Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionrequest";

		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2TextColor(),
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(), iconsDir, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionRequest) + ": ");

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex(
				de.uib.opsidatamodel.productstate.ActionSequence.getLabel2DisplayLabel(), null, false,
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionSequence) + ": "

		);

		lastStateChangeTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_lastStateChange));

		productsequenceTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_position));

		productversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_productVersion));

		packageversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_packageVersion));

		versionInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_versionInfo)) {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, // value to display
					boolean isSelected, // is the cell selected
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe since instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;
					if (val.equals(""))
						return c;

					if (val.equals(InstallationStateTableModel.CONFLICTstring)
							|| val.equals(InstallationStateTableModel.unequalAddstring
									+ InstallationStateTableModel.CONFLICTstring)) {
						c.setBackground(Globals.CONFLICT_STATE_CELL_COLOR); 
						c.setForeground(Globals.CONFLICT_STATE_CELL_COLOR);
					} else {

						String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
						IFInstallationStateTableModel istm = (IFInstallationStateTableModel) (table.getModel());

						String serverProductVersion = "";

						if (istm.getGlobalProductInfos().get(productId) == null)
							logging.warning(this,
									" istm.getGlobalProductInfos()).get(productId) == null for productId " + productId);
						else
							serverProductVersion = serverProductVersion
									+ ((istm.getGlobalProductInfos()).get(productId))
											.get(de.uib.opsidatamodel.productstate.ProductState.KEY_versionInfo);

						if (!val.equals(serverProductVersion)) {
							c.setForeground(Color.red);
							// if (c instanceof JLabel) ((JLabel) c).setText("≠ " + val) ;//setText(val + "
							// (≠)") ;
						} else {
							// if (c instanceof JLabel) ((JLabel) c).setText("" + val );
						}
					}
				}

				return c;
			}
		};

		installationInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_installationInfo))

		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, // value to display
					boolean isSelected, // is the cell selected
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;
					if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.FAILED))))
						c.setForeground(Color.red);

					else if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.SUCCESSFUL))))

						c.setForeground(Globals.okGreen);

				}

				return c;
			}
		};

		/*
		 * JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT)
		 * {
		 * 
		 * @Override
		 * public int getMinimumDividerLocation()
		 * {
		 * return 10;
		 * }
		 * };
		 * 
		 * leftPane.setTopComponent(topPane);
		 * leftPane.setBottomComponent(paneProducts);
		 * leftPane.setDividerLocation(topPane.getPreferredSize().height);
		 * leftPane.setResizeWeight((double) 0);
		 */

		JPanel leftPane = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(topPane, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setLeftComponent(leftPane);

		// propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false,
		
		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, true, false);
		logging.info(this, " created properties Panel, is  EditMapPanelX instance No. " + EditMapPanelX.objectCounter);
		((EditMapPanelX) propertiesPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName()));
		propertiesPanel.registerDataChangedObserver(mainController.getGeneralDataChangedKeeper());
		propertiesPanel.setActor(new AbstractEditMapPanel.Actor() {
			@Override
			protected void reloadData() {
				super.reloadData();
				logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			protected void saveData() {
				super.saveData();
				logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			protected void deleteData() {
				super.deleteData();
				logging.info(this, "we are in PanelProductSettings");
			}
		});

		panelEditProperties = new PanelEditClientProperties(mainController, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		propertiesPanel.registerDataChangedObserver(infoPane);

		infoPane.getPanelProductDependencies().setDependenciesModel(mainController.getDependenciesModel());

		setRightComponent(infoPane);
		// setDividerLocation(fwidth_lefthanded - splitterLeftRight);

		producePopupMenu(productDisplayFields);

		paneProducts.addMouseListener(new utils.PopupMouseListener(popup));
		tableProducts.addMouseListener(new utils.PopupMouseListener(popup));

		
	}

	public void initAllProperties() {
		propertiesPanel.init();
		infoPane.setProductInfo("");
		infoPane.setProductAdvice("");
	}

	protected void producePopupMenu(final Map<String, Boolean> checkColumns) {
		popup = new JPopupMenu("");

		// LinkedHashMap<String, JMenuItem> menuItems = new LinkedHashMap<String,
		

		JMenuItem save = new JMenuItemFormatted();
		save.setText(configed.getResourceValue("ConfigedMain.saveConfiguration"));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		save.setFont(Globals.defaultFont);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.debug(this, "actionevent on save-menue");
				mainController.checkSaveAll(false);
				mainController.requestReloadStatesAndActions();
			}
		});
		popup.add(save);

		itemOnDemand = new JMenuItemFormatted();
		itemOnDemand.setText(configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"));
		itemOnDemand.setFont(Globals.defaultFont);
		itemOnDemand.addActionListener((ActionEvent e) -> mainController
				.fireOpsiclientdEventOnSelectedClients(PersistenceController.OPSI_CLIENTD_EVENT_on_demand));

		popup.add(itemOnDemand);

		itemSaveAndExecute = new JMenuItemFormatted();
		itemSaveAndExecute.setText(configed.getResourceValue("ConfigedMain.savePOCAndExecute"));
		// itemSaveAndExecute.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X,
		// InputEvent.CTRL_DOWN_MASK) );
		// dies bit get its intended context
		itemSaveAndExecute.setIcon(Globals.createImageIcon("images/executing_command_blue_16.png", ""));
		itemSaveAndExecute.setFont(Globals.defaultFont);
		itemSaveAndExecute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.debug(this, "actionevent on save and execute menu item");
				saveAndExecuteAction();
			}
		});
		popup.add(itemSaveAndExecute);
		popup.addSeparator();

		/*
		 * subOpsiclientdEvent = new JMenu(
		 * configed.getResourceValue("ConfigedMain.OpsiclientdEvent")
		 * );
		 * 
		 * 
		 * subOpsiclientdEvent.setFont(Globals.defaultFont);
		 * 
		 * for (final String event :
		 * mainController.getPersistenceController().getOpsiclientdExtraEvents())
		 * {
		 * JMenuItemFormatted item = new JMenuItemFormatted(event);
		 * item.setFont(Globals.defaultFont);
		 * 
		 * item.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * mainController.fireOpsiclientdEventOnSelectedClients(event);
		 * }
		 * });
		 * 
		 * subOpsiclientdEvent.add(item);
		 * }
		 * 
		 * popup.add(subOpsiclientdEvent);
		 * 
		 */
		showPopupOpsiclientdEvent(true);

		JMenuItem reload = new JMenuItemFormatted();
		// reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does not
		// find itscontext
		reload.setText(configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Globals.createImageIcon("images/reload16.png", ""));
		reload.setFont(Globals.defaultFont);
		reload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "------------- reload action");
				reloadAction();
			}
		});
		popup.add(reload);

		JMenuItem createReport = new JMenuItemFormatted();
		createReport.setText(configed.getResourceValue("PanelProductSettings.pdf"));
		createReport.setIcon(Globals.createImageIcon("images/acrobat_reader16.png", ""));
		createReport.setFont(Globals.defaultFont);
		createReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "------------- create report");
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
				pdfExportTable.setPageSizeA4_Landscape();
				pdfExportTable.execute(null, false); // create pdf // no filename, onlySelectedRows = false

				/**
				 * old PDF exporting tableToPDF = new DocumentToPdf (null,
				 * metaData); // no filename, metadata // set alignment left
				 * ArrayList list = new ArrayList<>(); list.add(0); // column
				 * de.uib.utilities.pdf.DocumentElementToPdf.setAlignmentLeft(list);
				 * // only relevant rows
				 * tableToPDF.createContentElement("table",
				 * strippTable(tableProducts));
				 * tableToPDF.setPageSizeA4_Landscape(); 
				 */
			}
		});
		popup.add(createReport);

		exportTable.addMenuItemsTo(popup);

		/*
		 * popup.addSeparator();
		 * 
		 * JMenuItem findClientsWithOtherProductVersion = new JMenuItemFormatted();
		 * findClientsWithOtherProductVersion.setText(configed.getResourceValue(
		 * "ConfigedMain.findClientsWithOtherProductVersion"));
		 * findClientsWithOtherProductVersion.setFont(Globals.defaultFont);
		 * findClientsWithOtherProductVersion.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * mainController.selectClientsNotCurrentProductInstalled(selectedProducts);
		 * }
		 * }
		 * );
		 * popup.add(findClientsWithOtherProductVersion);
		 * 
		 * // is extremely slow
		 */

		JMenu sub = new JMenu(configed.getResourceValue("ConfigedMain.columnVisibility"));
		sub.setFont(Globals.defaultFont);
		popup.addSeparator();
		popup.add(sub);

		Iterator<String> iter = checkColumns.keySet().iterator();

		while (iter.hasNext()) {
			final String columnName = iter.next();

			if (columnName.equals("productId"))
				// fixed column
				continue;

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
		Vector<String[]> data = new Vector<>();
		String[] headers = new String[jTable.getColumnCount()];
		for (int i = 0; i < jTable.getColumnCount(); i++) {
			headers[i] = jTable.getColumnName(i);
		}

		for (int j = 0; j < jTable.getRowCount(); j++) {
			dontStrippIt = false;
			String[] actCol = new String[jTable.getColumnCount()];
			for (int i = 0; i < jTable.getColumnCount(); i++) {

				String s = "";
				try {
					s = jTable.getValueAt(j, i).toString();
				} catch (Exception ex) { // nullPointerException, cell empty
					s = "";
				}
				actCol[i] = s;
				jTable.getColumnName(i);
				switch (jTable.getColumnName(i)) {
				case "Stand":
					if (!s.equals("not_installed"))
						dontStrippIt = true;
					break;
				case "Report":
					if (!s.equals(""))
						dontStrippIt = true;
					break;
				case "Angefordert":
					if (!s.equals("none"))
						dontStrippIt = true;
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
		JTable strippedTable = new JTable(strippedData, headers);

		return strippedTable;
	}

	protected void reloadAction() {
		// List<? extends RowSorter.SortKey> currentSortKeys
		
		mainController.requestReloadStatesAndActions();
		mainController.resetView(mainController.getViewIndex());
		mainController.setDataChanged(false);
		
	}

	protected void saveAndExecuteAction() {
		logging.info(this, "saveAndExecuteAction");
		mainController.checkSaveAll(false);
		mainController.requestReloadStatesAndActions();

		mainController.fireOpsiclientdEventOnSelectedClients(PersistenceController.OPSI_CLIENTD_EVENT_on_demand);

	}

	private String infoSortKeys(List<? extends RowSorter.SortKey> sortKeys) {
		if (sortKeys == null)
			return "null";

		StringBuilder result = new StringBuilder("[");
		int i = 0;
		for (RowSorter.SortKey key : sortKeys) {
			i++;
			result.append(key.getColumn() + ".." + key);
		}
		result.append("]");
		logging.info(this, "infoSortkeys " + result);
		return " (number " + i + ") ";
		

	}

	public List<? extends RowSorter.SortKey> getSortKeys() {
		logging.info(this, "getSortKeys : " + infoSortKeys(currentSortKeys));
		return currentSortKeys;
	}

	public void setSortKeys(List<? extends RowSorter.SortKey> currentSortKeys) {
		logging.info(this, "setSortKeys: " + infoSortKeys(currentSortKeys));
		if (currentSortKeys != null)
			tableProducts.getRowSorter().setSortKeys(currentSortKeys);
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
				logging.info(this, "setSelection 0");
			} else {
				for (int row = 0; row < tableProducts.getRowCount(); row++) {
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId))
						tableProducts.addRowSelectionInterval(row, row);
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

	private class StringComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			
			return o1.compareTo(o2);
		}
	}

	public void setTableModel(IFInstallationStateTableModel istm) {
		tableProducts.setRowSorter(null); // delete old row sorter before setting new model
		tableProducts.setModel(istm);

		
		// try bugfix:

		final StringComparator myComparator = new StringComparator();

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableProducts.getModel()) {

			@Override
			protected boolean useToString(int column) {
				try {
					return super.useToString(column);
				} catch (Exception ex) {
					logging.info(this, "------------------- no way to string");
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
					logging.info(this, "------------------- not getting comparator ");
					return null;
				}

				// NullPointerException at java.lang.Class.isAssignableFrom
			}
		}

		;
		// sorter.setComparator(0, Globals.getCollator());

		tableProducts.setRowSorter(sorter);
		sorter.addRowSorterListener(this);

		// tableProducts.getTableHeader().setToolTipText(configed.getResourceValue("MainFrame.tableheader_tooltip"));
		tableProducts.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(tableProducts.getTableHeader().getDefaultRenderer()));

		// ---

		logging.debug(this, " tableProducts columns  count " + tableProducts.getColumnCount());
		Enumeration<TableColumn> enumer = tableProducts.getColumnModel().getColumns();

		while (enumer.hasMoreElements())
			logging.debug(this, " tableProducts column  " + ((TableColumn) enumer.nextElement()).getHeaderValue());

		int colIndex = -1;

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_productId)) > -1) {
			TableColumn nameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(fwidth_column_productname);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_productName)) > -1) {
			TableColumn completeNameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(fwidth_column_productcompletename);
			completeNameColumn.setCellRenderer(productCompleteNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_targetConfiguration)) > -1) {
			TableColumn targetColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.showIconsInProductTable)
				iconsDir = "images/productstate/targetconfiguration";

			JComboBox targetCombo = new JComboBox<>();
			targetCombo.setRenderer(standardListCellRenderer);
			
			targetColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(targetCombo, istm,
					de.uib.opsidatamodel.productstate.TargetConfiguration.getLabel2DisplayLabel(), iconsDir));
			targetColumn.setPreferredWidth(fwidth_column_productstate);
			targetColumn.setCellRenderer(targetConfigurationTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_installationStatus)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.showIconsInProductTable)
				iconsDir = "images/productstate/installationstatus";

			JComboBox statesCombo = new JComboBox<>();
			statesCombo.setRenderer(standardListCellRenderer);
			
			statusColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(statesCombo, istm,
					de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2DisplayLabel(), iconsDir));
			statusColumn.setPreferredWidth(fwidth_column_productstate);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_actionProgress)) > -1) {
			TableColumn progressColumn = tableProducts.getColumnModel().getColumn(colIndex);

			progressColumn.setPreferredWidth(fwidth_column_productstate);
			progressColumn.setCellRenderer(actionProgressTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_lastAction)) > -1) {
			TableColumn lastactionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastactionColumn.setPreferredWidth(fwidth_column_productstate);
			lastactionColumn.setCellRenderer(lastActionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_actionResult)) > -1) {
			TableColumn actionresultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionresultColumn.setPreferredWidth(fwidth_column_productstate);
			actionresultColumn.setCellRenderer(actionResultTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_actionRequest)) > -1) {

			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			String iconsDir = null;
			if (Globals.showIconsInProductTable)
				iconsDir = "images/productstate/actionrequest";

			JComboBox actionsCombo = new JComboBox<>();
			actionsCombo.setRenderer(standardListCellRenderer);
			actionColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(actionsCombo, istm,
					de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(), iconsDir));
			actionColumn.setPreferredWidth(fwidth_column_productstate);
			actionColumn.setCellRenderer(actionRequestTableCellRenderer); 
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_lastStateChange)) > -1) {
			TableColumn laststatechangeColumn = tableProducts.getColumnModel().getColumn(colIndex);
			laststatechangeColumn.setPreferredWidth(fwidth_column_productsequence);

			
			laststatechangeColumn.setCellRenderer(lastStateChangeTableCellRenderer);

			/*
			 * if (sorter instanceof DefaultRowSorter)
			 * {
			 * ((DefaultRowSorter) sorter).setComparator(colIndex, new
			 * de.uib.utilities.IntComparatorForStrings());
			 * }
			 */

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_actionSequence)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(fwidth_column_productsequence);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			if (sorter instanceof DefaultRowSorter) {
				((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
			}

			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_productPriority)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(fwidth_column_productsequence);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			// if (sorter instanceof DefaultRowSorter)
			{
				((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
			}
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_position)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(fwidth_column_productsequence);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			// if (sorter instanceof DefaultRowSorter)
			// ((DefaultRowSorter) sorter).setComparator(colIndex, new
			
			// we already have Integer

		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_productVersion)) > -1) {
			TableColumn productversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productversionColumn.setPreferredWidth(fwidth_column_productversion);
			productversionColumn.setCellRenderer(productversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_packageVersion)) > -1) {
			
			TableColumn packageversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			packageversionColumn.setPreferredWidth(fwidth_column_packageversion);
			packageversionColumn.setCellRenderer(packageversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_versionInfo)) > -1) {
			TableColumn versionInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(fwidth_column_versionInfo);
			versionInfoColumn.setCellRenderer(versionInfoTableCellRenderer);

			((DefaultRowSorter) sorter).setComparator(colIndex, new StringComparator());

			// ((DefaultRowSorter) sorter).setComparator(colIndex, new
			
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_installationInfo)) > -1) {
			TableColumn installationInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			installationInfoColumn.setPreferredWidth(fwidth_column_installationInfo);
			installationInfoColumn.setCellRenderer(installationInfoTableCellRenderer);

			JComboBox installationInfoCombo = new JComboBox<>();
			
			/*
			 * try
			 * {
			 * JTextField field = (JTextField) installationInfoCombo.getEditor();
			 * field.getCaret().setBlinkRate(0);
			 * }
			 * catch(Exception ex)
			 * {
			 * logging.debug(this, "installationInfoCombo.getEditor() " + ex);
			 * }
			 */

			installationInfoCombo.setRenderer(standardListCellRenderer);

			DynamicCellEditor cellEditor = new DynamicCellEditor(installationInfoCombo, istm,
					InstallationInfo.defaultDisplayValues);

			installationInfoColumn.setCellEditor(cellEditor);
		}

		sorter.setSortKeys(sortkeysDefault);

	}

	public void initEditing(String productID, String productTitle, String productInfo, String productHint,
			String productVersion,
			// String productPackageversion,
			String productCreationTimestamp,

			Map<String, Boolean> specificPropertiesExisting, Collection storableProductProperties,
			Map editableProductProperties,

			// editmappanelx
			Map<String, de.uib.utilities.table.ListCellOptions> productpropertyOptionsMap,

			/*
			 * //editmappanel
			 * Map productpropertiesValuesMap,
			 * Map productpropertiesDescriptionsMap,
			 * Map productpropertiesDefaultsMap,
			 */

			ProductpropertiesUpdateCollection updateCollection)

	{
		infoPane.setGrey(false);
		infoPane.setProductId(productID);
		infoPane.setProductName(productTitle);
		infoPane.setProductInfo(productInfo);
		infoPane.setProductVersion(productVersion);
		
		infoPane.setProductAdvice(productHint);

		Globals.checkCollection(this, "initEditing", "editableProductProperties ", editableProductProperties);
		Globals.checkCollection(this, "initEditing", "productpropertyOptionsMap", productpropertyOptionsMap);

		propertiesPanel.setEditableMap(

				// visualMap (merged for different clients)
				editableProductProperties, productpropertyOptionsMap

		);

		propertiesPanel.setStoreData(storableProductProperties);
		propertiesPanel.setUpdateCollection(updateCollection);

	}

	public void clearListEditors() {
		if (propertiesPanel instanceof EditMapPanelX)
			((EditMapPanelX) propertiesPanel).cancelOldCellEditing();
	}

	public void clearEditing() {

		initEditing("", // String productTitle,
				"", // String productInfo,
				"", // String productHint,
				"", // String productVersion,
				"", // String productPackageversion,
				"", // String productCreationTimestamp,

				null, // new HashMap<>,
				null, // Collection storableProductProperties,

				null, // Map editableProductProperties,

				// editmappanelx
				null, // Map<String, de.uib.utilities.table.ListCellOptions> productpropertyOptionsMap

				/*
				 * //editmappanel
				 * null, //Map productpropertiesValuesMap,
				 * null, //Map productpropertiesDescriptionsMap,
				 * null, //Map productpropertiesDefaultsMap,
				 */

				null // ProductpropertiesUpdateCollection updateCollection)
		);
		infoPane.setGrey(true);
	}

	// RowSorterListener for table row sorter
	@Override
	public void sorterChanged(RowSorterEvent e) {
		logging.info(this, "RowSorterEvent " + e);
		currentSortKeys = tableProducts.getRowSorter().getSortKeys();
		logging.info(this, "sorterChanged, sortKeys: " + infoSortKeys(currentSortKeys));

	}

	public List<String> getSelectedProducts()
	// in model terms
	{
		List<Integer> selectedRows = getSelectedRowsInModelTerms();

		List<String> selectedProducts = new ArrayList<>();

		for (int row : selectedRows) {
			selectedProducts.add((String) tableProducts.getModel().getValueAt(row, 0));
		}

		logging.info(this, "selectedProducts " + selectedProducts);

		return selectedProducts;
	}

}
