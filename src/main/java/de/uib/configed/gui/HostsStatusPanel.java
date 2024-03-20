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

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.messagebus.MessagebusListener;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class HostsStatusPanel extends JPanel implements MessagebusListener {
	public static final int MAX_CLIENT_NAMES_IN_FIELD = 10;

	private static final int ICON_SIZE = 22;
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
		Logging.info(this, "setGroupName " + s);
		resetReportedClients();
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

	private void initComponents() {
		labelActivated = new JLabel(Configed.getResourceValue("MainFrame.activated"));

		labelGroupActivated = new JLabel(Configed.getResourceValue("MainFrame.groupActivated"));

		fieldGroupActivated = new JTextField();

		fieldGroupActivated.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
		fieldGroupActivated.setEditable(false);

		labelAllClientsCount = new JLabel();
		labelAllClientsCount.setPreferredSize(Globals.LABEL_DIMENSION);

		labelSelectedClientsCount = new JLabel(Configed.getResourceValue("MainFrame.labelSelected"));

		labelSelectedClientsNames = new JLabel(Configed.getResourceValue("MainFrame.labelNames"));

		labelInvolvedDepots = new JLabel(Configed.getResourceValue("MainFrame.labelInDepot"));

		fieldActivatedClientsCount = new JTextField();
		fieldActivatedClientsCount.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
		fieldActivatedClientsCount.setEditable(false);

		fieldSelectedClientsNames = new JTextField();

		fieldSelectedClientsNames.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
		fieldSelectedClientsNames.setEditable(false);
		fieldSelectedClientsNames.setDragEnabled(true);

		fieldInvolvedDepots = new JTextField();
		fieldInvolvedDepots.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
		fieldInvolvedDepots.setEditable(false);

		connectedIcon = Utils.createImageIcon("bootstrap/check_circle_blue.png", "", ICON_SIZE, ICON_SIZE);
		disconnectedIcon = Utils.createImageIcon("bootstrap/circle_blue.png", "", ICON_SIZE, ICON_SIZE);

		connectionStateLabel = new JLabel();

		initializeValues();
	}

	private void setupLayout() {
		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		layoutStatusPane.setHorizontalGroup(layoutStatusPane.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(labelAllClientsCount, 0, Globals.COUNTERFIELD_WIDTH, Globals.COUNTERFIELD_WIDTH)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(labelActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(labelGroupActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(fieldGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(labelSelectedClientsNames, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(fieldSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(fieldActivatedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelInvolvedDepots, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(fieldInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE).addComponent(connectionStateLabel).addGap(Globals.GAP_SIZE));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAllClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(labelActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
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
