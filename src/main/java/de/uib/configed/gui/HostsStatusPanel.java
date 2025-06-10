/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.messagebus.MessagebusListener;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class HostsStatusPanel extends JPanel implements MessagebusListener {
	private static final String CONNECTED_TOOLTIP = Configed.getResourceValue("HostsStatusPanel.ConnectedTooltip");
	private static final String DISCONNECTED_TOOLTIP = Configed
			.getResourceValue("HostsStatusPanel.DisconnectedTooltip");

	private JLabel labelAllClientsCount;

	private JTextField fieldSelectedClientsNames;

	private JLabel labelSelectedClientsNames;

	private JLabel labelInvolvedDepots;
	private JTextField fieldInvolvedDepots;

	private JLabel connectionStateLabel;
	private ImageIcon connectedIcon;
	private ImageIcon disconnectedIcon;

	public HostsStatusPanel() {
		super();

		initComponents();
		setupLayout();
	}

	public String getSelectedClientNames() {
		return fieldSelectedClientsNames.getText();
	}

	public String getInvolvedDepots() {
		return fieldInvolvedDepots.getText();
	}

	public void updateValues(Integer clientsCount, List<String> selectedClients, String depot) {
		int selectedClientsCount = selectedClients.size();

		Logging.info(this, "updateValues clientsCount, selectedClientsCount ", clientsCount, ", ",
				selectedClientsCount);

		labelAllClientsCount.setText(Configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount
				+ " (" + selectedClientsCount + ")");

		String selectedClientNames = Utils.getListStringRepresentation(selectedClients);

		fieldSelectedClientsNames.setText(selectedClientNames);

		fieldSelectedClientsNames.setToolTipText(
				"<html><body><p>" + selectedClientNames.replace(";\n", "<br\\ >") + "</p></body></html>");

		fieldInvolvedDepots.setText(depot);
		fieldInvolvedDepots.setToolTipText("<html><body><p>" + depot.replace(";\n", "<br\\ >") + "</p></body></html>");
	}

	private void initComponents() {
		labelAllClientsCount = new JLabel();

		labelSelectedClientsNames = new JLabel(Configed.getResourceValue("MainFrame.labelNames"));

		labelInvolvedDepots = new JLabel(Configed.getResourceValue("MainFrame.labelInDepot"));

		fieldSelectedClientsNames = new JTextField();
		fieldSelectedClientsNames.setEditable(false);
		fieldSelectedClientsNames.setDragEnabled(true);

		fieldInvolvedDepots = new JTextField();
		fieldInvolvedDepots.setEditable(false);

		connectedIcon = Icons.getSelectedIntellijIcon("circle_checkmark", 24);
		disconnectedIcon = Icons.getSelectedIntellijIcon("circle", 24);

		connectionStateLabel = new JLabel();
	}

	private void setupLayout() {
		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		layoutStatusPane.setHorizontalGroup(layoutStatusPane.createSequentialGroup()
				.addComponent(labelAllClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldSelectedClientsNames, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldInvolvedDepots, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(connectionStateLabel));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAllClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(connectionStateLabel))
				.addGap(Globals.MIN_GAP_SIZE));
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		connectionStateLabel.setIcon(connectedIcon);
		connectionStateLabel.setToolTipText(CONNECTED_TOOLTIP);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		connectionStateLabel.setIcon(disconnectedIcon);
		connectionStateLabel.setToolTipText(DISCONNECTED_TOOLTIP);
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
