/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.java_websocket.handshake.ServerHandshake;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class HostsStatusPanel extends JPanel implements MessagebusListener {
	private static final String CONNECTED_TOOLTIP = Configed.getResourceValue("HostsStatusPanel.ConnectedTooltip");
	private static final String DISCONNECTED_TOOLTIP = Configed
			.getResourceValue("HostsStatusPanel.DisconnectedTooltip");

	private JLabel labelAllClientsCount;

	private JLabel labelSelectedClients;

	private FlatTextField fieldSelectedClientName;

	private JLabel labelOS;
	private JLabel labelDeviceType;

	private JLabel labelDepots;
	private JTextField fieldDepots;

	private JLabel serverConnectionStateLabel;
	private ImageIcon serverConnectedIcon;
	private ImageIcon serverDisconnectedIcon;
	private ImageIcon clientConnectedIcon;
	private ImageIcon clientDisconnectedIcon;

	private ConfigedMain configedMain;

	public HostsStatusPanel(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initComponents();
		setupLayout();
	}

	public String getSelectedClientNames() {
		return fieldSelectedClientName.getText();
	}

	public void updateAllClientsCount(int clientsCount) {
		Logging.info(this, "updateTotalClients clientsCount ", clientsCount);
		labelAllClientsCount.setText(Configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount);
	}

	public void updateValues(int clientsInTable, List<String> selectedClients, HostInfo hostInfo) {
		updateSelectedClientsCount(configedMain.getSelectedClients().size(), clientsInTable);

		// We must update the text before updating the connection status
		// because the connection status icon is set based on the text in fieldSelectedClientName
		updateSelectedClientText(selectedClients);

		updateClientConnectionStatus();

		updateSelectedClientInfo(hostInfo);

		updateDepotsOfSelectedClients();
	}

	public void updateSelectedClientsCount(int selectedClientsCount, int clientsInTable) {
		Logging.info(this, "updateValues clientsInTable, selectedClientsCount ", clientsInTable, ", ",
				selectedClientsCount);

		labelSelectedClients.setText(Configed.getResourceValue("MainFrame.labelSelectedClients") + "  "
				+ selectedClientsCount + " (" + clientsInTable + ")");
	}

	private void updateSelectedClientText(List<String> selectedClients) {
		if (selectedClients.size() == 1) {
			String selectedClient = selectedClients.get(0);
			fieldSelectedClientName.setText(selectedClient);
		} else {
			fieldSelectedClientName.setText(null);
		}
	}

	public void updateClientConnectionStatus() {
		String clientName = fieldSelectedClientName.getText();
		Logging.info(this, "updateClientConnectionStatus clientName ", clientName);
		if (clientName.isEmpty() || !configedMain.isHostConnected(clientName)) {
			fieldSelectedClientName.setLeadingIcon(clientDisconnectedIcon);
		} else {
			fieldSelectedClientName.setLeadingIcon(clientConnectedIcon);
		}
	}

	private void updateSelectedClientInfo(HostInfo hostInfo) {
		if (hostInfo == null) {
			return;
		}

		labelOS.setText(hostInfo.getString(HostInfo.CLIENT_OS_KEY));
		labelOS.setIcon(Utils.determineIconBasedOnPlatform(hostInfo.getString(HostInfo.CLIENT_OS_KEY), 20));

		labelDeviceType
				.setText(ClientInfoPanel.transformDeviceType(hostInfo.getString(HostInfo.CLIENT_DEVICE_TYPE_KEY)));
		labelDeviceType
				.setIcon(Utils.determineIconBasedOnDeviceType(hostInfo.getString(HostInfo.CLIENT_DEVICE_TYPE_KEY), 20));
		StringBuilder tooltipText = new StringBuilder();
		if (hostInfo.getString(HostInfo.CLIENT_DEVICE_VENDOR_KEY) != null
				&& !hostInfo.getString(HostInfo.CLIENT_DEVICE_VENDOR_KEY).isBlank()) {
			tooltipText.append(hostInfo.getString(HostInfo.CLIENT_DEVICE_VENDOR_KEY));
		}

		if (hostInfo.getString(HostInfo.CLIENT_DEVICE_MODEL_KEY) != null
				&& !hostInfo.getString(HostInfo.CLIENT_DEVICE_MODEL_KEY).isBlank()) {
			if (tooltipText.length() > 0) {
				tooltipText.append("\n");
			}
			tooltipText.append(hostInfo.getString(HostInfo.CLIENT_DEVICE_MODEL_KEY));
		}

		labelDeviceType.setToolTipText(tooltipText.toString());
	}

	private void updateDepotsOfSelectedClients() {
		String depotsOfClients = configedMain.getDepotsOfSelectedClients().toString();
		depotsOfClients = depotsOfClients.substring(1, depotsOfClients.length() - 1);
		fieldDepots.setText(depotsOfClients);
		fieldDepots.setToolTipText(depotsOfClients.replace(", ", "\n"));
	}

	private void initComponents() {
		labelAllClientsCount = new JLabel();

		labelSelectedClients = new JLabel();

		labelOS = new JLabel();
		labelDeviceType = new JLabel();

		labelDepots = new JLabel(Configed.getResourceValue("MainFrame.labelDepots"));
		fieldDepots = new JTextField();
		fieldDepots.setEditable(false);
		fieldDepots.setDragEnabled(true);

		fieldSelectedClientName = new FlatTextField();
		fieldSelectedClientName.setEditable(false);
		fieldSelectedClientName.setDragEnabled(true);

		serverConnectedIcon = Icons.getSelectedIntellijIcon("circle_checkmark", 24);
		serverDisconnectedIcon = Icons.getSelectedIntellijIcon("circle", 24);
		clientConnectedIcon = Icons.getIntellijIcon("checkmark", Globals.OPSI_OK);

		// Create a transparent icon for disconnected clients
		// This is a workaround have an empty space
		clientDisconnectedIcon = new ImageIcon(
				new BufferedImage(clientConnectedIcon.getIconWidth(), 1, BufferedImage.TYPE_INT_ARGB));

		serverConnectionStateLabel = new JLabel();
	}

	private void setupLayout() {
		setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", gapx " + Globals.GAP_SIZE + ", flowx", "", ""));

		add(labelAllClientsCount);
		add(labelSelectedClients);
		add(fieldSelectedClientName, "growx, pushx");
		add(labelDeviceType, "growx, pushx");
		add(labelOS, "growx, pushx");
		add(labelDepots);
		add(fieldDepots, "growx, pushx");
		add(serverConnectionStateLabel);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		serverConnectionStateLabel.setIcon(serverConnectedIcon);
		serverConnectionStateLabel.setToolTipText(CONNECTED_TOOLTIP);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		serverConnectionStateLabel.setIcon(serverDisconnectedIcon);
		serverConnectionStateLabel.setToolTipText(DISCONNECTED_TOOLTIP);
	}

	@Override
	public void onError(Exception ex) {
		//Not Needed
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		// Not Needed
	}
}
