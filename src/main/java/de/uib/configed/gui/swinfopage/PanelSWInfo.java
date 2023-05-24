/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;

public class PanelSWInfo extends JPanel {

	private static final String FILTER_MS_UPDATES = "withMsUpdates";
	private static final String FILTER_MS_UPDATES2 = "withMsUpdates2";

	private PanelGenEditTable panelTable;
	private ExporterToCSV csvExportTable;

	private JPanel subPanelTitle;

	private final SWInfoTableModel voidTableModel = new SWInfoTableModel();
	private GenTableModel modelSWInfo;

	private JLabel labelSuperTitle;

	private String title = "";
	private String hostId = "";
	private boolean withPopup;

	private String scanInfo = "";

	private boolean askForOverwrite = true;

	private int hGap = Globals.HGAP_SIZE / 2;
	private int vGap = Globals.VGAP_SIZE / 2;

	private ConfigedMain mainController;
	private OpsiserviceNOMPersistenceController persist;

	public enum KindOfExport {
		PDF, CSV
	}

	private KindOfExport kindOfExport;

	private boolean withMsUpdates;
	private boolean withMsUpdates2 = true;
	private String exportFilename;

	private JCheckBox checkWithMsUpdates;

	private int indexOfColWindowsSoftwareID;

	private TableModelFilterCondition filterConditionWithMsUpdates = new TableModelFilterCondition() {
		@Override
		public void setFilter(Set<Object> filter) {
			/* Not needed */}

		@Override
		public boolean test(List<Object> row) {
			String entry = (String) row.get(indexOfColWindowsSoftwareID);
			boolean isKb = entry.startsWith("kb");

			return !isKb;
			// on filtering active everything is taken if not isKb
		}
	};

	private JCheckBox checkWithMsUpdates2;

	private final Pattern patternWithKB = Pattern.compile("\\{.*\\}\\p{Punct}kb.*", Pattern.UNICODE_CHARACTER_CLASS);

	private TableModelFilterCondition filterConditionWithMsUpdates2 = new TableModelFilterCondition() {
		@Override
		public void setFilter(Set<Object> filter) {
			/* Not needed */}

		@Override
		public boolean test(List<Object> row) {
			String entry = (String) row.get(indexOfColWindowsSoftwareID);
			boolean isKb = patternWithKB.matcher(entry).matches();

			return !isKb;
			// on filtering active everything is taken if not isKb
		}
	};

	public PanelSWInfo(ConfigedMain mainController) {
		this(true, mainController);
	}

	public PanelSWInfo(boolean withPopup, ConfigedMain mainController) {
		this.withPopup = withPopup;
		this.mainController = mainController;
		persist = mainController.getPersistenceController();

		initTable();

		buildPanel();
	}

	private void initTable() {

		labelSuperTitle = new JLabel();

		if (!Main.FONT) {
			labelSuperTitle.setFont(Globals.defaultFontBold);
		}

		panelTable = new PanelGenEditTable("title", 0, false, 0, true, new int[] {

		}, true) {

		};

		panelTable.setTitle("");

		panelTable.setColumnSelectionAllowed(false);

		panelTable.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelTable.setSearchSelectMode(true);
		panelTable.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);

		List<String> columnNames;
		List<String> classNames;

		columnNames = new ArrayList<>(SWAuditClientEntry.KEYS);
		columnNames.remove(0);
		classNames = new ArrayList<>();
		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
			finalColumns[i] = i;
		}

		modelSWInfo = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						Logging.info(this, "retrieving data for " + hostId);
						Map<String, Map<String, Object>> tableData = persist.retrieveSoftwareAuditData(hostId);

						if (tableData == null || tableData.keySet().isEmpty()) {
							scanInfo = Configed.getResourceValue("PanelSWInfo.noScanResult");
							title = scanInfo;
						} else {
							Logging.debug(this, "retrieved size  " + tableData.keySet().size());
							scanInfo = "Scan " + persist.getLastSoftwareAuditModification(hostId);
							title = scanInfo;

						}

						setSuperTitle(scanInfo);

						Logging.debug(this, " got scanInfo " + scanInfo);

						return tableData;
					}
				})), -1, finalColumns, null, null);

		indexOfColWindowsSoftwareID = columnNames.indexOf(SWAuditEntry.WINDOWS_SOFTWARE_ID);
		modelSWInfo.chainFilter(FILTER_MS_UPDATES, new TableModelFilter(filterConditionWithMsUpdates));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		if (!Main.THEMES) {
			checkWithMsUpdates.setForeground(Globals.blue);
		}
		checkWithMsUpdates.addItemListener(itemEvent -> setWithMsUpdatesValue(checkWithMsUpdates.isSelected()));
		setWithMsUpdatesValue(withMsUpdates);

		modelSWInfo.chainFilter(FILTER_MS_UPDATES2, new TableModelFilter(filterConditionWithMsUpdates2));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		if (!Main.THEMES) {
			checkWithMsUpdates2.setForeground(Globals.blue);
		}
		checkWithMsUpdates2.addItemListener(itemEvent -> setWithMsUpdatesValue2(checkWithMsUpdates2.isSelected()));
		setWithMsUpdatesValue2(withMsUpdates2);

		subPanelTitle = new JPanel();

		JLabel labelWithMSUpdates = new JLabel(Configed.getResourceValue("PanelSWInfo.withMsUpdates"));
		JLabel labelWithMSUpdates2 = new JLabel(Configed.getResourceValue("PanelSWInfo.withMsUpdates2"));

		if (!Main.THEMES) {
			subPanelTitle.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout layoutSubPanelTitle = new GroupLayout(subPanelTitle);
		subPanelTitle.setLayout(layoutSubPanelTitle);

		layoutSubPanelTitle.setHorizontalGroup(layoutSubPanelTitle.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addGroup(layoutSubPanelTitle.createParallelGroup()
						.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layoutSubPanelTitle.createSequentialGroup()
								.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(hGap, hGap, hGap).addGap(hGap, hGap, hGap)
								.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutSubPanelTitle.createSequentialGroup()
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(hGap, hGap, hGap).addGap(hGap, hGap, hGap).addComponent(checkWithMsUpdates2,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(hGap, hGap, hGap));
		layoutSubPanelTitle.setVerticalGroup(layoutSubPanelTitle.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(vGap, vGap, vGap)

				.addGroup(layoutSubPanelTitle.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(vGap, vGap, vGap).addGap(vGap, vGap, vGap));

		panelTable.setTableModel(modelSWInfo);
		panelTable.setSearchColumnsAll();

		panelTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		panelTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		panelTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		csvExportTable = new ExporterToCSV(panelTable.getTheTable());

	}

	private void buildPanel() {

		labelSuperTitle.setOpaque(true);
		if (!Main.THEMES) {
			labelSuperTitle.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		JTable jTable = new JTable(voidTableModel, null);

		jTable.setAutoCreateRowSorter(true);
		TableRowSorter<? extends TableModel> tableSorter = (TableRowSorter<? extends TableModel>) jTable.getRowSorter();
		ArrayList<RowSorter.SortKey> list = new ArrayList<>(1);
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		tableSorter.setSortKeys(list);
		tableSorter.sort();

		jTable.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		jTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		jTable.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setColumnSelectionAllowed(true);
		jTable.setRowSelectionAllowed(true);
		jTable.setDragEnabled(true);
		JScrollPane scrollPaneSWInfo = new JScrollPane(jTable);
		if (!Main.THEMES) {
			scrollPaneSWInfo.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout layoutEmbed = new GroupLayout(this);
		setLayout(layoutEmbed);

		layoutEmbed.setHorizontalGroup(layoutEmbed.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addGroup(layoutEmbed.createParallelGroup()

						.addComponent(subPanelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addComponent(panelTable, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(hGap, hGap, hGap));

		layoutEmbed.setVerticalGroup(layoutEmbed.createSequentialGroup()

				.addGap(vGap, vGap, vGap)
				.addComponent(subPanelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(panelTable, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap));

		if (withPopup) {
			PopupMenuTrait popupTrait = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_EXPORT_CSV,
					PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV, PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,
					PopupMenuTrait.POPUP_FLOATINGCOPY }) {

				@Override
				public void action(int p) {

					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD:
						reload();
						break;

					case PopupMenuTrait.POPUP_FLOATINGCOPY:
						floatExternalX();
						break;

					case PopupMenuTrait.POPUP_PDF:
						sendToPDF();
						break;

					case PopupMenuTrait.POPUP_EXPORT_CSV:
						sendToCSV();
						break;

					case PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV:
						sendToCSVonlySelected();
						break;

					default:
						Logging.warning(this, "no case found for popupmenutrait");
						break;
					}
				}

			};

			popupTrait.addPopupListenersTo(new JComponent[] { this, panelTable.getTheTable(),
					panelTable.getTheScrollpane(), jTable, scrollPaneSWInfo, scrollPaneSWInfo.getViewport() });

		}

	}

	public void setWriteToFile(String path) {
		exportFilename = path;
	}

	public void setAskForOverwrite(boolean b) {
		askForOverwrite = b;
	}

	public void setKindOfExport(KindOfExport k) {
		kindOfExport = k;
	}

	public void setWithMsUpdates(boolean b) {
		checkWithMsUpdates.setSelected(b);
		setWithMsUpdatesValue(b);
	}

	public void setWithMsUpdates2(boolean b) {
		checkWithMsUpdates2.setSelected(b);
		setWithMsUpdatesValue2(b);
	}

	private void setWithMsUpdatesValue(boolean b) {
		withMsUpdates = b;

		// setting filter true means that the specified values are not included

		if (panelTable != null && modelSWInfo != null) {
			boolean saveDataChanged = panelTable.isDataChanged();
			modelSWInfo.setUsingFilter(FILTER_MS_UPDATES, !b);
			panelTable.setDataChanged(saveDataChanged);
		}
	}

	private void setWithMsUpdatesValue2(boolean b) {
		withMsUpdates2 = b;

		// setting filter true means that the specified values are not included

		if (panelTable != null && modelSWInfo != null) {
			boolean saveDataChanged = panelTable.isDataChanged();
			modelSWInfo.setUsingFilter(FILTER_MS_UPDATES2, !b);
			panelTable.setDataChanged(saveDataChanged);
		}
	}

	public void sendToTerminal() {

		SWterminalExporter exporter = new SWterminalExporter(PersistenceControllerFactory.getPersistenceController());

		exporter.setHost(hostId);

		if (panelTable.getSelectedRowCount() > 0) {
			exporter.setOnlySelectedRows();
		}

		exporter.setPanelTableForExportTable(panelTable);

		exporter.export();

	}

	public void export() {
		csvExportTable.setAskForOverwrite(askForOverwrite);
		String exportPath = exportFilename;
		if (kindOfExport == KindOfExport.CSV) {
			Logging.info(this, "export to " + exportPath);
			csvExportTable.execute(exportPath, false);
		} else if (kindOfExport == KindOfExport.PDF) {
			sendToPDF();
		}
	}

	public void sendToCSV() {
		csvExportTable.execute(null, false);
	}

	public void sendToCSVonlySelected() {
		csvExportTable.execute(null, true);
	}

	public void sendToPDF() {
		Logging.info(this, "create report swaudit for " + hostId + " check");

		HashMap<String, String> metaData = new HashMap<>();

		metaData.put("title", "Client " + hostId);
		metaData.put("subtitle", scanInfo);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "software inventory");

		ExporterToPDF pdfExportTable = new ExporterToPDF(panelTable);
		pdfExportTable.setClient(hostId);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(exportFilename, false);

	}

	private void setSuperTitle(String s) {
		String supertitle = s;
		Logging.info(this, "setSuperTitle " + s);
		labelSuperTitle.setText(supertitle);

	}

	/** overwrite in subclasses */
	protected void reload() {
		Logging.debug(this, "reload action");
	}

	private void floatExternalX() {

		PanelSWInfo copyOfMe;
		GeneralFrame externalView;

		copyOfMe = new PanelSWInfo(false, mainController);
		copyOfMe.setHost(hostId);
		copyOfMe.updateModel();

		externalView = new GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(ConfigedMain.getMainFrame());

		externalView.setVisible(true);
	}

	public void updateModel() {
		Logging.debug(this, "update modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());
		modelSWInfo.requestReload();
		modelSWInfo.reset();
		Logging.debug(this, "update modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());
	}

	public void setSoftwareNullInfo(String hostId) {
		Logging.info(this, "setSoftwareNullInfo,  " + hostId);

		this.hostId = "" + hostId;
		title = this.hostId;

		String timeS = "" + Globals.getToday();
		String[] parts = timeS.split(":");
		if (parts.length > 2) {
			timeS = parts[0] + ":" + parts[1];
		}

		scanInfo = " (no software audit data, checked at time:  " + timeS + ")";
	}

	public void setHost(String hostId) {
		Logging.info(this, "setHost" + hostId + " -- ");

		this.hostId = "" + hostId;
	}

	private static class SWInfoTableModel extends AbstractTableModel {
		private List<String[]> data;

		public SWInfoTableModel() {
			super();
			data = new ArrayList<>();
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return SWAuditClientEntry.getDisplayKeys().size();
		}

		@Override
		public String getColumnName(int column) {
			return SWAuditClientEntry.getDisplayKey(column + 1);
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data.get(row)[col + 1];
		}
	}
}
