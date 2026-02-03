/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

import java.awt.Component;
import java.awt.Font;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.infopage.AbstractSingleClientInfoPanel;
import de.uib.configed.gui.share.infopage.SingleClientExporter;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.TableModelFilter;
import de.uib.configed.gui.share.table.TableModelFilterCondition;
import de.uib.configed.gui.share.table.gui.BooleanIconTableCellRenderer;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.MapRetriever;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.type.SWAuditClientEntry;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelSWSingleClientInfo extends AbstractSingleClientInfoPanel {
	private static final String FILTER_MS_UPDATES = "withMsUpdates";
	private static final String FILTER_MS_UPDATES2 = "withMsUpdates2";

	private PanelGenEdit panelTable;

	private JPanel subPanelTitle;

	private JLabel labelWithMSUpdates;
	private JLabel labelWithMSUpdates2;

	private GenTableModel modelSWInfo;

	private JLabel labelSuperTitle;

	private String title = "";
	private String hostId;
	private boolean withPopup;

	private String scanInfo = "";

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public enum KindOfExport {
		PDF, CSV
	}

	private boolean withMsUpdates;
	private boolean withMsUpdates2 = true;

	private JCheckBox checkWithMsUpdates;

	private JCheckBox checkWithMsUpdates2;

	private ConfigedMain configedMain;

	public PanelSWSingleClientInfo(ConfigedMain configedMain, boolean withPopup) {
		this.configedMain = configedMain;
		this.withPopup = withPopup;

		initTableComponents();
		setupTableLayout();

		buildPanel();
	}

	protected void updateContent(String clientId) {
		setHost(clientId);
		Logging.debug(this, "setSoftwareAudit for ", hostId);
		setAskForOverwrite(true);
		updateModel();
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

		panelTable = new PanelGenEdit("", false, 0, new int[] {}, true);
		panelTable.getGenEditTable().setColumnSelectionAllowed(false);
		panelTable.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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

		subPanelTitle.setLayout(new MigLayout("insets 0, wrap 3", "[pref!]50[pref!][pref!]", "[]0"));

		subPanelTitle.add(labelSuperTitle, "cell 0 0, aligny center");
		subPanelTitle.add(labelWithMSUpdates, "cell 1 0, aligny center");
		subPanelTitle.add(checkWithMsUpdates, "cell 2 0, aligny center");

		subPanelTitle.add(labelWithMSUpdates2, "cell 1 1, aligny center");
		subPanelTitle.add(checkWithMsUpdates2, "cell 2 1, aligny center");

		panelTable.setTableModel(modelSWInfo);
		panelTable.setSearchColumnsAll();

		if (panelTable.getGenEditTable().getRowSorter() instanceof TableRowSorter) {
			TableRowSorter<? extends TableModel> rowSorter = (TableRowSorter<? extends TableModel>) panelTable
					.getGenEditTable().getRowSorter();
			rowSorter.setComparator(7, new OSComparator());

			List<RowSorter.SortKey> sortKeys = new ArrayList<>(2);
			sortKeys.add(new RowSorter.SortKey(7, SortOrder.ASCENDING));
			sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			rowSorter.setSortKeys(sortKeys);
			rowSorter.sort();
		}

		panelTable.getGenEditTable().setDefaultRenderer(Object.class, new OSTableCellRenderer());
		panelTable.getGenEditTable().getColumnModel().getColumn(0).setPreferredWidth(400);
		panelTable.getGenEditTable().getColumnModel().getColumn(1).setPreferredWidth(200);
		panelTable.getGenEditTable().getColumnModel().getColumn(2).setPreferredWidth(100);
		panelTable.getGenEditTable().getColumnModel().getColumn(7)
				.setCellRenderer(new BooleanIconTableCellRenderer(Icons.getIntellijIcon("checkmark"), null));
	}

	private void buildPanel() {
		this.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + " 0 0 0, wrap 1", "[grow, fill]",
				"[]" + Globals.MIN_GAP_SIZE + "[grow]"));
		this.add(subPanelTitle, "growx");
		this.add(panelTable, "grow, push");

		if (withPopup) {
			new PopupMenuTrait(
					new Integer[] { PopupMenuTrait.POPUP_EXPORT_CSV, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV,
							PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF, PopupMenuTrait.POPUP_FLOATING_COPY },
					new JComponent[] { this, panelTable.getGenEditTable(), panelTable.getTheScrollpane() }) {
				@Override
				public void action(int p) {
					actionOnPopupMenu(p);
				}
			};
		}
	}

	private static class OSComparator implements Comparator<Boolean> {
		@Override
		public int compare(Boolean o1, Boolean o2) {
			boolean b1 = o1;
			boolean b2 = o2;
			if (b1 == b2) {
				return 0;
			}
			return b1 ? -1 : 1;
		}
	}

	private static class OSTableCellRenderer extends ColorTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			boolean isOperatingSystem = (Boolean) table.getValueAt(row, 7);

			if (isOperatingSystem) {
				cellComponent.setFont(cellComponent.getFont().deriveFont(Font.BOLD));
			} else {
				cellComponent.setFont(cellComponent.getFont().deriveFont(Font.PLAIN));
			}

			return cellComponent;
		}
	}

	private Map<String, Map<String, Object>> retrieveSWInfoMap() {
		if (hostId == null) {
			return new HashMap<>();
		}

		Logging.info(this, "retrieving data for ", hostId);
		Map<String, List<SWAuditClientEntry>> swAuditClientEntries = persistenceController.getDataServices().software
				.getSoftwareAuditOnClients(Collections.singletonList(hostId));

		Map<String, Map<String, Object>> tableData = persistenceController.getDataServices().software
				.retrieveSoftwareAuditData(swAuditClientEntries, hostId);

		if (tableData == null || tableData.isEmpty()) {
			scanInfo = Configed.getResourceValue("PanelSWInfo.noScanResult");
			title = scanInfo;
		} else {
			Optional<String> osName = tableData.values().stream()
					.filter(innerMap -> Boolean.TRUE.equals(innerMap.get(SWAuditEntry.IS_OPERATING_SYSTEM)))
					.map(innerMap -> (String) innerMap.get(SWAuditEntry.NAME)).findFirst();
			Logging.debug(this, "retrieved size  ", tableData.size());
			scanInfo = "Scan " + persistenceController.getDataServices().software
					.getLastSoftwareAuditModification(swAuditClientEntries, hostId);
			if (osName.isPresent()) {
				scanInfo += " on " + osName.get();
			}
			title = scanInfo;
		}

		setSuperTitle(scanInfo);

		Logging.debug(this, " got scanInfo ", scanInfo);
		return tableData;
	}

	private void actionOnPopupMenu(int p) {
		switch (p) {
		case PopupMenuTrait.POPUP_RELOAD -> reload();
		case PopupMenuTrait.POPUP_FLOATING_COPY -> floatExternalX();
		case PopupMenuTrait.POPUP_PDF -> getSingleClientExporter().toBuilder().kindOfExport(KindOfExport.PDF).build()
				.export();
		case PopupMenuTrait.POPUP_EXPORT_CSV -> getSingleClientExporter().toBuilder().kindOfExport(KindOfExport.CSV)
				.build().export();
		case PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV -> getSingleClientExporter().toBuilder()
				.kindOfExport(KindOfExport.CSV).onlySelectedRows(true).build().export();
		default -> Logging.warning(this, "no case found for popupmenutrait");
		}
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
		setMSUpdatesFilter(FILTER_MS_UPDATES, !b);
	}

	private void setWithMsUpdatesValue2(boolean b) {
		withMsUpdates2 = b;

		// setting filter true means that the specified values are not included
		setMSUpdatesFilter(FILTER_MS_UPDATES2, !b);
	}

	private void setMSUpdatesFilter(String filterKey, boolean filter) {
		if (panelTable != null && modelSWInfo != null) {
			boolean saveDataChanged = panelTable.isDataChanged();
			modelSWInfo.setUsingFilter(filterKey, filter);
			panelTable.setDataChanged(saveDataChanged);
		}
	}

	@Override
	public SingleClientExporter getSingleClientExporter() {
		return SingleClientExporter.builder().table(panelTable.getGenEditTable()).filename(exportFilename)
				.askForOverwrite(askForOverwrite).kindOfExport(kindOfExport)
				.metaData(Map.of("title", "Client " + hostId, "subtitle", scanInfo, "subject", "Software report",
						"keywords", "software inventory"))
				.build();
	}

	private void setSuperTitle(String s) {
		String supertitle = s;
		Logging.info(this, "setSuperTitle ", s);
		labelSuperTitle.setText(supertitle);
	}

	private void reload() {
		Logging.debug(this, "reload action");
		ConfigedMain.getMainFrame().activateLoadingCursor();
		persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());

		// We can assume that only one client is selected here, because this panel should
		// only be shown then.
		updateContent(configedMain.getSelectedClients().getFirst());
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void floatExternalX() {
		PanelSWSingleClientInfo copyOfMe = new PanelSWSingleClientInfo(configedMain, false);
		copyOfMe.setHost(hostId);
		copyOfMe.updateModel();

		JDialog dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setContentPane(copyOfMe);
		dialog.setSize(getSize());
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	public void updateModel() {
		Logging.debug(this, "update modelSWInfo.getRowCount() ", modelSWInfo.getRowCount());
		modelSWInfo.requestReload();
		modelSWInfo.reset();
		Logging.debug(this, "update modelSWInfo.getRowCount() ", modelSWInfo.getRowCount());
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
}
