/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewComponent.TableSideEffectStrategy;
import de.uib.configed.gui.features.table.GenericTableViewEffect;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.features.table.TableConfig;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.IconTableCellRenderer;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromTable;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ClientTablePanel extends JPanel {
	private JScrollPane scrollpane;

	private TableSearchPane searchPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// we put a JTable on a standard JScrollPane
	private GenericTableViewComponent clientTableViewComponent;
	private JComponent component;

	private ConfigedMain configedMain;

	private Set<String> iconColumnKeys = Set.of(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL,
			HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL,
			HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL, HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL,
			HostInfo.CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL);

	public ClientTablePanel(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;

		initComponents();
	}

	private void initComponents() {
		List<TableColumnConfig> columns = new ArrayList<>();
		for (Entry<String, Boolean> entry : persistenceController.getDataServices().host.getHostDisplayFields()
				.entrySet()) {
			TableColumnConfig column = TableColumnConfig.builder().key(entry.getKey())
					.header(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey())).build();

			if (iconColumnKeys.contains(entry.getKey())) {
				column = column.toBuilder().renderer(getTableCellRendererBasedOnIconType(entry.getKey())).maxWidth(100)
						.build();
			}

			if (HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL.equals(entry.getKey())) {
				column = column.withComparator(this::compareStringIgnoringNull);
			}

			columns.add(column.withVisible(entry.getValue()));
		}

		TableSideEffectStrategy clientSideEffectStrategy = (GenericTableViewEffect effect) -> {
			if (effect instanceof GenericTableViewEffect.Selection) {
				return this::actOnListSelection;
			}
			return null;
		};

		TableConfig config = TableConfig.builder().defauTableCellRenderer(new ColorTableCellRenderer())
				.fillViewportHeight(true).showTableHeader(true).dragEnabled(true).autoCreateRowSorter(true)
				.selectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION).reorderingAllowed(false)
				.enableHeaderContextMenu(true).columnSelectionAllowed(false)
				.sortColumnKey(Configed
						.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL))
				.sortOrder(SortOrder.ASCENDING).build();

		clientTableViewComponent = new GenericTableViewComponent(GenericTableViewModel.builder().tableConfig(config)
				.columns(columns).originalSnapshot(new ArrayList<>()).rows(new ArrayList<>()).showSearchPane(true)
				// .searchTargetModelFromTable(new SearchTargetModelFromClientTable(configedMain))
				.filterKey(FilterKey.CLIENT_TABLE).build(), clientSideEffectStrategy, () -> {
					if (ConfigedMain.getMainFrame() != null) {
						return ConfigedMain.getMainFrame().getClientPopupMenu();
					}
					return null;
				});
		component = clientTableViewComponent.initUI();

		// Ask to be notified of selection changes.
		// selectionModel = (DefaultListSelectionModel) clientTable.getSelectionModel();
		// the default implementation in JTable yields this type

		// activateListSelectionListener();

		searchPane = clientTableViewComponent.getSearchPane();
		// searchPane = new TableSearchPane(new SearchTargetModelFromClientTable(configedMain, clientTable));
		// searchPane.setFilterKey(FilterKey.CLIENT_TABLE);
		// searchPane.setFiltering();

		// clientTable.addKeyListener(searchPane);

		setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fill, wrap 1", "[grow, fill]", "[grow, fill]"));

		// add(searchPane);
		add(component, "grow, push");
	}

	/**
	 * Returns a comparator that sorts string values while always placing null
	 * or empty strings at the bottom regardless of sort direction.
	 *
	 * @param ascending if true, sorts in natural order; if false, in reverse
	 *                  order.
	 */
	private int compareStringIgnoringNull(Object o1, Object o2) {
		boolean isO1Invalid = (o1 == null || o1.toString().isBlank());
		boolean isO2Invalid = (o2 == null || o2.toString().isBlank());

		if (isO1Invalid && isO2Invalid) {
			return 0;
		} else if (isO1Invalid || isO2Invalid) {
			return compareInvalids(isO1Invalid);
		} else {
			return ((Comparable<Object>) o1).compareTo(o2);
		}
	}

	private int compareInvalids(boolean isO1Invalid) {
		boolean isAscending = clientTableViewComponent.model.getTableConfig().getSortOrder() == SortOrder.ASCENDING;

		if (isO1Invalid) {
			return isAscending ? 1 : -1;
		} else {
			return isAscending ? -1 : 1;
		}
	}

	public GenericTableViewComponent getTableComponent() {
		return clientTableViewComponent;
	}

	private static TableCellRenderer getTableCellRendererBasedOnIconType(String iconType) {
		TableCellRenderer cellRenderer = new IconTableCellRenderer<>(
				IconTableCellRenderer.booleanMap(Icons.getIntellijIcon("checkmark", null)), false);
		if (HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL.equals(iconType)) {
			cellRenderer = new IconTableCellRenderer<>(
					IconTableCellRenderer.booleanMap(Icons.getIntellijIcon("checkmark", Globals.OPSI_OK)), false);
		} else if (HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL.equals(iconType)) {
			cellRenderer = new IconTableCellRenderer<>(IconTableCellRenderer.PLATFORM_ICONS);
		} else if (HostInfo.CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL.equals(iconType)) {
			cellRenderer = new IconTableCellRenderer<>(IconTableCellRenderer.DEVICE_ICONS);
		} else {
			// Do nothing.
		}
		return cellRenderer;
	}

	public void updateTable() {
		if (getComponent(0) == component) {
			// Do nothing if we already set the table as view
			return;
		}

		if (persistenceController.getDataServices().hostInfoCollections.getCountClients() == 0) {
			setMissingDataPanel();
		} else {
			if (getComponent(0) != null) {
				remove(0);
			}
			add(component);
		}
	}

	public boolean isFilteredMode() {
		return searchPane.isFilteredMode();
	}

	private void actOnListSelection() {
		if (ChangedDataManager.checkSaveAll(true)) {
			configedMain.actOnListSelection();
		}
	}

	public void setMissingDataPanel() {
		JLabel missingData0 = new JLabel(Icons.createImageIcon(Globals.ICON_CONFIGED, ""));
		JLabel missingData1 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));
		JLabel missingData2 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));
		JPanel mdPanel = new JPanel();

		mdPanel.setLayout(new MigLayout("fill"));

		JPanel panel = new JPanel(new MigLayout("wrap 1, aligny center, alignx center, gap 0", "[center]", "[]0"));
		panel.add(missingData0);
		panel.add(missingData1, "gapy " + Globals.GAP_SIZE);
		panel.add(missingData2, "gapy " + Globals.GAP_SIZE);

		mdPanel.add(panel, "grow, center");

		if (getComponent(0) != null) {
			remove(0);
		}
		add(mdPanel);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		scrollpane.addMouseListener(l);
		// clientTable.addMouseListener(l);
		component.addMouseListener(l);
	}

	public void setFilterMark(boolean selected) {
		searchPane.setFilterMark(selected);
	}

	public final void initColumnNames() {
		// New code
		searchPane.setSearchFieldsAll();
	}

	public void restoreFilter() {
		searchPane.restoreFilter();
	}

	public void setSelectedValues(Collection<String> clientsToSelect) {
		String valuesListS = null;
		if (clientsToSelect != null) {
			valuesListS = "" + clientsToSelect.size();
		}

		Logging.info(this, "setSelectedValues ", valuesListS);

		if (clientsToSelect == null) {
			// Clear selection when empty
			clientTableViewComponent.dispatch(new GenericTableViewMsg.ChangeSelection(new HashSet<>()));
		} else if (clientsToSelect.isEmpty()) {
			// Also act on list selection when there is no client to select.
			// For example when the last client is unselected in the client list,
			// this method is not called automatically by the selection listener,
			// so we do it manually
			actOnListSelection();
		} else {
			Set<String> rowsToSelect = new HashSet<>();
			for (int i = 0; i < clientTableViewComponent.model.getRows().size(); i++) {
				RowData data = clientTableViewComponent.model.getRows().get(i);
				String clientName = data.getValue(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, String.class);
				if (clientsToSelect.contains(clientName)) {
					rowsToSelect.add(data.getId());
				}
			}
			clientTableViewComponent.dispatch(new GenericTableViewMsg.ChangeSelection(rowsToSelect));

			Logging.info(this, "setSelectedValues  produced ", rowsToSelect.size());
		}
	}

	public int findModelRowFromClientName(String clientName) {
		Set<String> selectedIds = clientTableViewComponent.model.getSelectedRows();

		for (String id : selectedIds) {
			RowData data = clientTableViewComponent.getRowById(id);

			if (data != null) {
				String name = data.getValue(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, String.class);
				if (clientName != null && clientName.equals(name)) {
					return clientTableViewComponent.model.getRows().indexOf(data);
				}
			}
		}

		return -1;
	}

	public int findColumnIndex(String columnKey) {
		int columnIndex = -1;
		for (int i = 0; i < clientTableViewComponent.model.getColumns().size(); i++) {
			TableColumnConfig columnConfig = clientTableViewComponent.model.getColumns().get(i);
			if (columnKey.equals(columnConfig.getKey())) {
				columnIndex = i;
			}
		}
		return columnIndex;
	}

	private class SearchTargetModelFromClientTable extends SearchTargetModelFromTable {
		private ConfigedMain configedMain;

		public SearchTargetModelFromClientTable(ConfigedMain configedMain) {
			JTable table = clientTableViewComponent.getTable();
			super(table);

			this.configedMain = configedMain;

			Logging.info(this, "table null? ", table == null);
		}

		@Override
		public void setCursorRow(int row) {
			Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
		}

		@Override
		public void setFiltered(boolean b) {
			configedMain.setRebuiltClientListTableModel(true, false);
		}
	}
}
