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

	private JLabel labelActivated;

	private JLabel labelAllClientsCount;
	private JTextField fieldGroupActivated;
	private JLabel labelGroupActivated;

	private JLabel labelSelectedClientsCount;
	private JTextField fieldSelectedClientsNames;

	private JLabel labelSelectedClientsNames;
	private JTextField fieldActivatedClientsCount;

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

	public void setGroupName(String s) {
		Logging.info(this, "setGroupName ", s);
		fieldGroupActivated.setText(s);
	}

	public String getSelectedClientNames() {
		return fieldSelectedClientsNames.getText();
	}

	public String getInvolvedDepots() {
		return fieldInvolvedDepots.getText();
	}

	public String getGroupName() {
		return fieldGroupActivated.getText();
	}

	public void updateValues(Integer clientsCount, List<String> selectedClients, String depot) {
		int selectedClientsCount = selectedClients.size();

		Logging.info(this, "updateValues clientsCount, selectedClientsCount ", clientsCount, ", ",
				selectedClientsCount);

		labelAllClientsCount.setText(Configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount);

		setFieldClientsCount(selectedClientsCount);

		String selectedClientNames = Utils.getListStringRepresentation(selectedClients);

		fieldSelectedClientsNames.setText(selectedClientNames);

		fieldSelectedClientsNames.setToolTipText(
				"<html><body><p>" + selectedClientNames.replace(";\n", "<br\\ >") + "</p></body></html>");

		fieldInvolvedDepots.setText(depot);
		fieldInvolvedDepots.setToolTipText("<html><body><p>" + depot.replace(";\n", "<br\\ >") + "</p></body></html>");
	}

	public void setGroupClientsCount(int n) {
		String newS = null;
		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(0, bracketIndex);
			newS = keep + "(" + n + ")";
		} else {
			newS = "(" + n + ")";
		}

		fieldActivatedClientsCount.setText(newS);
	}

	private void setFieldClientsCount(Integer n) {
		String newS = "";
		if (n != null) {
			newS = n + " ";
		}

		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(bracketIndex);
			newS = newS + keep;
		}

		fieldActivatedClientsCount.setText(newS);
	}

	private void initComponents() {
		labelActivated = new JLabel(Configed.getResourceValue("MainFrame.activated"));

		labelGroupActivated = new JLabel(Configed.getResourceValue("MainFrame.groupActivated"));

		fieldGroupActivated = new JTextField();
		fieldGroupActivated.setEditable(false);

		labelAllClientsCount = new JLabel();

		labelSelectedClientsCount = new JLabel(Configed.getResourceValue("MainFrame.labelSelected"));

		labelSelectedClientsNames = new JLabel(Configed.getResourceValue("MainFrame.labelNames"));

		labelInvolvedDepots = new JLabel(Configed.getResourceValue("MainFrame.labelInDepot"));

		fieldActivatedClientsCount = new JTextField();
		fieldActivatedClientsCount.setEditable(false);

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
				.addComponent(labelActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldGroupActivated, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldSelectedClientsNames, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldActivatedClientsCount, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldInvolvedDepots, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(connectionStateLabel));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAllClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldActivatedClientsCount, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
