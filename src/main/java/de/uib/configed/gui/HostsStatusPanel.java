/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.java_websocket.handshake.ServerHandshake;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.messagebus.MessagebusListener;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class HostsStatusPanel extends JPanel implements MessagebusListener {
	private static final int MIN_WIDTH = 250;
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

	public void updateSelectedClients(int selectedClientsCount, int clientsInTable) {
		Logging.info(this, "updateValues clientsInTable, selectedClientsCount ", clientsInTable, ", ",
				selectedClientsCount);

		labelSelectedClients.setText(Configed.getResourceValue("MainFrame.labelSelectedClients") + "  "
				+ selectedClientsCount + " (" + clientsInTable + ")");
	}

	public void updateValues(int clientsInTable, List<String> selectedClients, HostInfo hostInfo) {
		int selectedClientsCount = selectedClients.size();

		updateSelectedClients(configedMain.getSelectedClients().size(), clientsInTable);

		if (selectedClientsCount == 1) {
			String selectedClient = selectedClients.get(0);

			fieldSelectedClientName.setText(selectedClient);
			if (configedMain.isHostConnected(selectedClient)) {
				fieldSelectedClientName.setLeadingIcon(clientConnectedIcon);
			} else {
				fieldSelectedClientName.setLeadingIcon(clientDisconnectedIcon);
			}
		} else {
			fieldSelectedClientName.setText(null);
			fieldSelectedClientName.setLeadingIcon(clientDisconnectedIcon);
		}

		labelOS.setText(hostInfo.getClientOS());
		labelOS.setIcon(Utils.determineIconBasedOnPlatform(hostInfo.getClientOSType(), 20));

		labelDeviceType.setText(ClientInfoPanel.transformDeviceType(hostInfo.getClientDeviceType()));
		labelDeviceType.setIcon(ClientInfoPanel.getDeviceTypeIcon(hostInfo.getClientDeviceType()));

		StringBuilder tooltipText = new StringBuilder();
		if (!hostInfo.getClientDeviceVendor().isBlank()) {
			tooltipText.append(hostInfo.getClientDeviceVendor());
		}

		if (!hostInfo.getClientDeviceModel().isBlank()) {
			if (tooltipText.length() > 0) {
				tooltipText.append("\n");
			}
			tooltipText.append(hostInfo.getClientDeviceModel());
		}

		labelDeviceType.setToolTipText(tooltipText.toString());

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
		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		layoutStatusPane.setHorizontalGroup(layoutStatusPane.createSequentialGroup()
				.addComponent(labelAllClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelSelectedClients, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(fieldSelectedClientName, MIN_WIDTH, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelDeviceType, MIN_WIDTH, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelOS, MIN_WIDTH, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addComponent(labelDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(fieldDepots, MIN_WIDTH, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(serverConnectionStateLabel));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAllClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSelectedClients, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelDeviceType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelOS, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSelectedClientName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(serverConnectionStateLabel))
				.addGap(Globals.MIN_GAP_SIZE));
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
