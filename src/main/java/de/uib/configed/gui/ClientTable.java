/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.features.table.TableConfig;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.IconTableCellRenderer;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;

public class ClientTable implements MessagebusListener {
	private String clientNameTitle;
	private GenericTableViewComponent tableViewComponent;

	private ConfigedMain configedMain;

	private IconTableCellRenderer<Boolean> defaultCheckMarkCellRenderer = new IconTableCellRenderer<>(
			IconTableCellRenderer.booleanMap(Icons.getIntellijIcon("checkmark", null)), false);
	private IconTableCellRenderer<Boolean> opsiCheckMarkCellRenderer = new IconTableCellRenderer<>(
			IconTableCellRenderer.booleanMap(Icons.getIntellijIcon("checkmark", Globals.OPSI_OK)), false);
	private IconTableCellRenderer<String> platformIconTableCellRenderer = new IconTableCellRenderer<>(
			IconTableCellRenderer.PLATFORM_ICONS);
	private IconTableCellRenderer<String> deviceTypeIconTableCellRenderer = new IconTableCellRenderer<>(
			IconTableCellRenderer.DEVICE_ICONS);

	private Set<String> checkMarks = Set.of(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL,
			HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL,
			HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL, HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientTable(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		TableConfig config = TableConfig.builder().defauTableCellRenderer(new ColorTableCellRenderer())
				.selectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION).fillViewportHeight(true)
				.dragEnabled(true).autoCreateRowSorter(true).reorderingAllowed(false).columnSelectionAllowed(false)
				.build();
		GenericTableViewModel model = GenericTableViewModel.builder().tableConfig(config).columns(buildColumns())
				.originalSnapshot(buildOriginalSnapshot()).build();

		this.tableViewComponent = new GenericTableViewComponent(model);

		clientNameTitle = Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName");
	}

	private List<TableColumnConfig> buildColumns() {
		List<TableColumnConfig> columns = new ArrayList<>();

		for (Entry<String, Boolean> entry : persistenceController.getDataServices().host.getHostDisplayFields()
				.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				String key = entry.getKey();
				columns.add(TableColumnConfig.builder().key(key)
						.header(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()))
						.renderer(getRenderer(key)).build());
			}
		}

		return columns;
	}

	private TableCellRenderer getRenderer(String key) {
		TableCellRenderer renderer = null;
		if (checkMarks.contains(key)) {
			if (HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL.equals(key)) {
				renderer = opsiCheckMarkCellRenderer;
			} else {
				renderer = defaultCheckMarkCellRenderer;
			}
		} else if (HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL.equals(key)) {
			renderer = platformIconTableCellRenderer;
		} else if (HostInfo.CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL.equals(key)) {
			renderer = deviceTypeIconTableCellRenderer;
		} else {
			renderer = null;
		}
		return renderer;
	}

	private List<Map<String, Object>> buildOriginalSnapshot() {
		List<Map<String, Object>> originalSnapshot = new ArrayList<>();
		Map<String, HostInfo> pcinfos = persistenceController.getDataServices().hostInfoCollections
				.getMapOfPCInfoMaps();

		for (String clientId : configedMain.getSelectedClients()) {
			HostInfo pcinfo = pcinfos.getOrDefault(clientId, new HostInfo());

			Map<String, Object> rowmap = pcinfo.getDisplayRowMap();
			rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL,
					persistenceController.getDataServices().host.getSessionInfo().getOrDefault(clientId, ""));
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, configedMain.isHostConnected(clientId));
			originalSnapshot.add(rowmap);
		}

		return originalSnapshot;
	}

	public GenericTableViewComponent getTable() {
		return tableViewComponent;
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// Not required to implement.
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		// Sleep for a little because otherwise we cannot get the needed data from the server.
		Utils.threadSleep(this, 5);

		if ((!WebSocketEvent.GENERAL_EVENT.toString().equals(message.get("type")) && !message.containsKey("event"))
				|| ServerActionManager.isLocalChangeInProgress()) {
			return;
		}

		String eventType = (String) message.get("event");
		if (WebSocketEvent.HOST_CREATED.toString().equals(eventType)) {
			addClientToTable();
		} else if (WebSocketEvent.HOST_DELETED.toString().equals(eventType)) {
			removeClientFromTable();
		} else {
			// Other events are handled by other listeners.
		}
	}

	public void addClientToTable() {
		// persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		// SwingUtilities.invokeLater(() -> {
		// 	Set<String> selectedValues = getSelectedSet();
		// 	clearSelection();
		// 	configedMain.refreshClientListKeepingGroup();
		// 	configedMain.setClients(selectedValues);
		// });
	}

	public void removeClientFromTable() {
		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(configedMain::refreshClientListKeepingGroup);
	}
}
