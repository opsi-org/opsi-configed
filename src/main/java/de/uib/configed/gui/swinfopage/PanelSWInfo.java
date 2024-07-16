/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.ExporterToCSV;
import de.uib.utils.table.ExporterToPDF;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.TableModelFilter;
import de.uib.utils.table.TableModelFilterCondition;
import de.uib.utils.table.gui.ColorTableCellRenderer;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.gui.TableSearchPane;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapRetriever;
import de.uib.utils.table.provider.RetrieverMapSource;

public class PanelSWInfo extends JPanel {
	private static final String FILTER_MS_UPDATES = "withMsUpdates";
	private static final String FILTER_MS_UPDATES2 = "withMsUpdates2";

	private PanelGenEditTable panelTable;
	private ExporterToCSV csvExportTable;

	private JPanel subPanelTitle;

	private JLabel labelWithMSUpdates;
	private JLabel labelWithMSUpdates2;

	private GenTableModel modelSWInfo;

	private JLabel labelSuperTitle;

	private String title = "";
	private String hostId;
	private boolean withPopup;

	private String scanInfo = "";

	private boolean askForOverwrite = true;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public enum KindOfExport {
		PDF, CSV
	}

	private KindOfExport kindOfExport;

	private boolean withMsUpdates;
	private boolean withMsUpdates2 = true;
	private String exportFilename;

	private JCheckBox checkWithMsUpdates;

	private JCheckBox checkWithMsUpdates2;

	public PanelSWInfo(boolean withPopup) {
		this.withPopup = withPopup;

		initTableComponents();
		setupTableLayout();

		buildPanel();
	}

	private static TableModelFilter createTableModelFilter1(int indexOfColWindowsSoftwareID) {
		TableModelFilterCondition filterConditionWithMsUpdates = new TableModelFilterCondition() {
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

		return new TableModelFilter(filterConditionWithMsUpdates);
	}

	private static TableModelFilter createTableModelFilter2(int indexOfColWindowsSoftwareID) {
		final Pattern patternWithKB = Pattern.compile("\\{.*\\}\\p{Punct}kb.*", Pattern.UNICODE_CHARACTER_CLASS);

		TableModelFilterCondition filterConditionWithMsUpdates2 = new TableModelFilterCondition() {
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

		return new TableModelFilter(filterConditionWithMsUpdates2);
	}

	private void initTableComponents() {
		labelSuperTitle = new JLabel();

		panelTable = new PanelGenEditTable("", false, 0, new int[] {}, true);
		panelTable.setColumnSelectionAllowed(false);
		panelTable.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelTable.setSearchMode(TableSearchPane.SearchMode.FULL_TEXT_SEARCH);

		List<String> columnNames = new ArrayList<>(SWAuditClientEntry.KEYS);

		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			finalColumns[i] = i;
		}

		modelSWInfo = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						// Nothing to reload.
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return retrieveSWInfoMap();
					}
				})), -1, finalColumns, null, null);

		int indexOfColWindowsSoftwareID = columnNames.indexOf(SWAuditEntry.WINDOWS_SOFTWARE_ID);
		modelSWInfo.chainFilter(FILTER_MS_UPDATES, createTableModelFilter1(indexOfColWindowsSoftwareID));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates = new JCheckBox("", withMsUpdates);

		checkWithMsUpdates.addItemListener(itemEvent -> setWithMsUpdatesValue(checkWithMsUpdates.isSelected()));
		setWithMsUpdatesValue(withMsUpdates);

		modelSWInfo.chainFilter(FILTER_MS_UPDATES2, createTableModelFilter2(indexOfColWindowsSoftwareID));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);

		checkWithMsUpdates2.addItemListener(itemEvent -> setWithMsUpdatesValue2(checkWithMsUpdates2.isSelected()));
		setWithMsUpdatesValue2(withMsUpdates2);

		labelWithMSUpdates = new JLabel(Configed.getResourceValue("PanelSWInfo.withMsUpdates"));
		labelWithMSUpdates2 = new JLabel(Configed.getResourceValue("PanelSWInfo.withMsUpdates2"));
	}

	private void setupTableLayout() {
		subPanelTitle = new JPanel();

		GroupLayout layoutSubPanelTitle = new GroupLayout(subPanelTitle);
		subPanelTitle.setLayout(layoutSubPanelTitle);

		layoutSubPanelTitle.setHorizontalGroup(layoutSubPanelTitle.createSequentialGroup()
				.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(0, 50, 50)
				.addGroup(layoutSubPanelTitle.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(layoutSubPanelTitle.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		layoutSubPanelTitle.setVerticalGroup(layoutSubPanelTitle.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)

				.addGroup(layoutSubPanelTitle.createParallelGroup()
						.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.MIN_GAP_SIZE)

				.addGroup(layoutSubPanelTitle.createParallelGroup()
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		panelTable.setTableModel(modelSWInfo);
		panelTable.setSearchColumnsAll();

		panelTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		panelTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		panelTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		csvExportTable = new ExporterToCSV(panelTable.getTheTable());
	}

	private void buildPanel() {
		JTable jTable = new JTable(new SWInfoTableModel());

		jTable.setAutoCreateRowSorter(true);
		TableRowSorter<? extends TableModel> tableSorter = (TableRowSorter<? extends TableModel>) jTable.getRowSorter();
		List<RowSorter.SortKey> list = new ArrayList<>(1);
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		tableSorter.setSortKeys(list);
		tableSorter.sort();

		jTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		jTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		jTable.setColumnSelectionAllowed(true);
		jTable.setRowSelectionAllowed(true);
		jTable.setDragEnabled(true);
		JScrollPane scrollPaneSWInfo = new JScrollPane(jTable);

		GroupLayout layoutEmbed = new GroupLayout(this);
		setLayout(layoutEmbed);

		layoutEmbed.setHorizontalGroup(layoutEmbed.createParallelGroup()
				.addComponent(subPanelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(panelTable, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutEmbed.setVerticalGroup(layoutEmbed.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(subPanelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(panelTable, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		if (withPopup) {
			PopupMenuTrait popupTrait = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_EXPORT_CSV,
					PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV, PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,
					PopupMenuTrait.POPUP_FLOATING_COPY }) {
				@Override
				public void action(int p) {
					actionOnPopupMenu(p);
				}
			};

			popupTrait.addPopupListenersTo(new JComponent[] { this, panelTable.getTheTable(),
					panelTable.getTheScrollpane(), jTable, scrollPaneSWInfo, scrollPaneSWInfo.getViewport() });
		}
	}

	private Map<String, Map<String, Object>> retrieveSWInfoMap() {
		if (hostId == null) {
			return new HashMap<>();
		}

		Logging.info(this, "retrieving data for ", hostId);
		Map<String, List<SWAuditClientEntry>> swAuditClientEntries = persistenceController.getSoftwareDataService()
				.getSoftwareAuditOnClients(Collections.singletonList(hostId));

		Map<String, Map<String, Object>> tableData = persistenceController.getSoftwareDataService()
				.retrieveSoftwareAuditData(swAuditClientEntries, hostId);

		if (tableData == null || tableData.isEmpty()) {
			scanInfo = Configed.getResourceValue("PanelSWInfo.noScanResult");
			title = scanInfo;
		} else {
			Logging.debug(this, "retrieved size  " + tableData.size());
			scanInfo = "Scan " + persistenceController.getSoftwareDataService()
					.getLastSoftwareAuditModification(swAuditClientEntries, hostId);
			title = scanInfo;
		}

		setSuperTitle(scanInfo);

		Logging.debug(this, " got scanInfo " + scanInfo);
		return tableData;
	}

	private void actionOnPopupMenu(int p) {
		switch (p) {
		case PopupMenuTrait.POPUP_RELOAD:
			reload();
			break;

		case PopupMenuTrait.POPUP_FLOATING_COPY:
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

	public void export() {
		csvExportTable.setAskForOverwrite(askForOverwrite);
		String exportPath = exportFilename;
		if (kindOfExport == KindOfExport.CSV) {
			Logging.info(this, "export to ", exportPath);
			csvExportTable.execute(exportPath, false);
		} else if (kindOfExport == KindOfExport.PDF) {
			sendToPDF();
		} else {
			Logging.warning(this, "unexpected kindOfExport ", kindOfExport);
		}
	}

	private void sendToCSV() {
		csvExportTable.execute(null, false);
	}

	private void sendToCSVonlySelected() {
		csvExportTable.execute(null, true);
	}

	private void sendToPDF() {
		Logging.info(this, "create report swaudit for ", hostId, " check");

		Map<String, String> metaData = new HashMap<>();

		metaData.put("title", "Client " + hostId);
		metaData.put("subtitle", scanInfo);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "software inventory");

		ExporterToPDF pdfExportTable = new ExporterToPDF(panelTable.getTheTable());
		pdfExportTable.setClient(hostId);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(exportFilename, false);
	}

	private void setSuperTitle(String s) {
		String supertitle = s;
		Logging.info(this, "setSuperTitle ", s);
		labelSuperTitle.setText(supertitle);
	}

	/** overwrite in subclasses */
	protected void reload() {
		Logging.debug(this, "reload action");
	}

	private void floatExternalX() {
		PanelSWInfo copyOfMe;
		GeneralFrame externalView;

		copyOfMe = new PanelSWInfo(false);
		copyOfMe.setHost(hostId);
		copyOfMe.updateModel();

		externalView = new GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
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
		Logging.info(this, "setSoftwareNullInfo,  ", hostId);

		this.hostId = hostId;
		title = this.hostId;

		String timeS = "" + new Timestamp(System.currentTimeMillis());
		String[] parts = timeS.split(":");
		if (parts.length > 2) {
			timeS = parts[0] + ":" + parts[1];
		}

		scanInfo = " (no software audit data, checked at time:  " + timeS + ")";
	}

	public void setHost(String hostId) {
		Logging.info(this, "setHost", hostId, " -- ");

		this.hostId = hostId;
	}

	private static class SWInfoTableModel extends AbstractTableModel {
		public SWInfoTableModel() {
			super();
		}

		@Override
		public int getRowCount() {
			return 0;
		}

		@Override
		public int getColumnCount() {
			return SWAuditClientEntry.KEYS.size();
		}

		@Override
		public String getColumnName(int column) {
			return SWAuditClientEntry.KEYS.get(column);
		}

		@Override
		public Object getValueAt(int row, int col) {
			return null;
		}
	}
}
