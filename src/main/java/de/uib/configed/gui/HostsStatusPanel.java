/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.HostsStatusInfo;
import de.uib.messagebus.MessagebusListener;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.Containership;

public class HostsStatusPanel extends JPanel implements HostsStatusInfo, MessagebusListener {
	public static final int MAX_CLIENT_NAMES_IN_FIELD = 10;

	private static final String CONNECTED_TOOLTIP = Configed.getResourceValue("HostsStatusPanel.ConnectedTooltip");
	private static final String DISCONNECTED_TOOLTIP = Configed
			.getResourceValue("HostsStatusPanel.DisconnectedTooltip");

	private JLabel labelAllClientsCount;
	private JTextField fieldGroupActivated;

	private JTextField fieldSelectedClientsNames;
	private JTextField fieldActivatedClientsCount;
	private JTextField fieldInvolvedDepots;

	private JLabel connectionStateLabel;
	private ImageIcon connectedIcon;
	private ImageIcon disconnectedIcon;

	public HostsStatusPanel() {
		super();
		createGui();
	}

	@Override
	public void setGroupName(String s) {
		Logging.info(this, "setGroupName " + s);
		resetReportedClients();
		fieldGroupActivated.setText(s);
	}

	@Override
	public String getSelectedClientNames() {
		return fieldSelectedClientsNames.getText();
	}

	@Override
	public String getInvolvedDepots() {
		return fieldInvolvedDepots.getText();
	}

	@Override
	public String getGroupName() {
		return fieldGroupActivated.getText();
	}

	private void resetReportedClients() {
		fieldActivatedClientsCount.setText("");
		fieldSelectedClientsNames.setText("");
		fieldSelectedClientsNames.setToolTipText("");

		fieldInvolvedDepots.setText("");
	}

	private void initializeValues() {
		labelAllClientsCount.setText(Configed.getResourceValue("MainFrame.labelClientsTotal") + " ");
		resetReportedClients();
		fieldInvolvedDepots.setText("");
		fieldInvolvedDepots.setToolTipText("");
	}

	@Override
	public void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots) {
		Logging.info(this,
				"updateValues clientsCount, selectedClientsCount " + clientsCount + ", " + selectedClientsCount);
		Logging.info(this,
				"updateValues clientsCount, selectedClientsCount " + clientsCount + ", " + selectedClientsCount);

		labelAllClientsCount.setText(Configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount);

		setFieldClientsCount(selectedClientsCount);

		if (selectedClientNames == null) {
			fieldSelectedClientsNames.setText("");
			fieldSelectedClientsNames.setToolTipText(null);
		} else {

			fieldSelectedClientsNames.setText(selectedClientNames);

			fieldSelectedClientsNames.setToolTipText(
					"<html><body><p>" + selectedClientNames.replace(";\n", "<br\\ >") + "</p></body></html>");

		}

		if (involvedDepots != null) {
			fieldInvolvedDepots.setText(involvedDepots);
			fieldInvolvedDepots.setToolTipText(
					"<html><body><p>" + involvedDepots.replace(";\n", "<br\\ >") + "</p></body></html>");
		}
	}

	@Override
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
		String newS = null;
		if (n != null) {
			newS = "" + n + " ";
		}

		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(bracketIndex);
			newS = newS + keep;
		}

		fieldActivatedClientsCount.setText(newS);
	}

	private void createGui() {

		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		JLabel labelActivated = new JLabel(Configed.getResourceValue("MainFrame.activated"));

		JLabel labelGroupActivated = new JLabel(Configed.getResourceValue("MainFrame.groupActivated"));

		fieldGroupActivated = new JTextField("");

		fieldGroupActivated.setPreferredSize(Globals.counterfieldDimension);
		fieldGroupActivated.setEditable(false);

		labelAllClientsCount = new JLabel("");
		labelAllClientsCount.setPreferredSize(Globals.labelDimension);

		JLabel labelSelectedClientsCount = new JLabel(Configed.getResourceValue("MainFrame.labelSelected"));

		JLabel labelSelectedClientsNames = new JLabel(Configed.getResourceValue("MainFrame.labelNames"));

		JLabel labelInvolvedDepots = new JLabel(Configed.getResourceValue("MainFrame.labelInDepot"));

		fieldActivatedClientsCount = new JTextField("");
		fieldActivatedClientsCount.setPreferredSize(Globals.counterfieldDimension);
		fieldActivatedClientsCount.setEditable(false);

		fieldSelectedClientsNames = new JTextField("");

		fieldSelectedClientsNames.setPreferredSize(Globals.counterfieldDimension);
		fieldSelectedClientsNames.setEditable(false);
		fieldSelectedClientsNames.setDragEnabled(true);

		fieldInvolvedDepots = new JTextField("");
		fieldInvolvedDepots.setPreferredSize(Globals.counterfieldDimension);
		fieldInvolvedDepots.setEditable(false);

		connectedIcon = Globals.createImageIcon("images/network-wireless-connected-100.png", "");
		disconnectedIcon = Globals.createImageIcon("images/network-wireless-disconnected.png", "");

		connectionStateLabel = new JLabel();

		initializeValues();

		layoutStatusPane.setHorizontalGroup(layoutStatusPane.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelAllClientsCount, 0, Globals.COUNTERFIELD_WIDTH, Globals.COUNTERFIELD_WIDTH)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelGroupActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(fieldGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelSelectedClientsNames, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addComponent(fieldSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(fieldActivatedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelInvolvedDepots, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(fieldInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE).addComponent(connectionStateLabel)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutStatusPane.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(labelAllClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelGroupActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldGroupActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelSelectedClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldActivatedClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelSelectedClientsNames, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldSelectedClientsNames, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelInvolvedDepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldInvolvedDepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(connectionStateLabel))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		if (!Main.THEMES) {

			Containership csStatusPane = new Containership(this);
			csStatusPane.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_3 },
					JTextComponent.class);
		}
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
