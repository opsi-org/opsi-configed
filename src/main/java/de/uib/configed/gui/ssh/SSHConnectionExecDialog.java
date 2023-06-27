/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnect;
import de.uib.utilities.logging.Logging;

public final class SSHConnectionExecDialog extends SSHConnectionOutputDialog {

	private static SSHConnectionExecDialog instance;

	private static final int INFO_LENGTH = 40;

	private JButton jButtonClear;
	private JButton jButtonKillProcess;

	private SSHConnectionExecDialog() {
		super(Configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput"));
		if (!SSHConnect.isConnectionAllowed()) {
			Logging.warning(this, "Connection forbidden. Close exec dialog.");
			this.cancel();
		} else {
			buildFrame = false;
			initGUI();

			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			this.setSize(900, 500);
			this.setLocationRelativeTo(ConfigedMain.getMainFrame());
			Logging.info(this, "SSHConnectionExecDialog built");

			this.setVisible(!SSHCommandFactory.sshAlwaysExecInBackground);
			buildFrame = true;
		}
	}

	public static SSHConnectionExecDialog getInstance() {
		Logging.info("SSHConnectionExecDialog.getInstance, existing " + instance);

		if (instance == null) {
			instance = getNewInstance();
		}
		return instance;

	}

	private static SSHConnectionExecDialog getNewInstance() {

		if (instance != null) {
			instance.leave();
			instance = null;
		}

		instance = new SSHConnectionExecDialog();
		SwingUtilities.invokeLater(() -> {
			instance.setLocationRelativeTo(ConfigedMain.getMainFrame());
			instance.setVisible(true);
		});
		return instance;
	}

	private void initGUI() {
		try {
			jButtonKillProcess = new IconButton(Configed.getResourceValue("SSHConnection.buttonKillProcess"),
					"images/edit-delete.png", "images/edit-delete.png", "images/edit-delete.png", true);
			jButtonKillProcess
					.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3));
			jButtonKillProcess.setToolTipText(Configed.getResourceValue("SSHConnection.buttonKillProcess"));

			jButtonClear = new IconButton(Configed.getResourceValue("SSHConnection.buttonClear"),
					"images/user-trash.png", "images/user-trash.png", "images/user-trash.png", true);
			jButtonClear.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3));
			jButtonClear.setToolTipText(Configed.getResourceValue("SSHConnection.buttonClear"));
			jButtonClear.addActionListener(actionEvent -> clear());
			createLayout(konsolePanelLayout, jScrollPane, Globals.GAP_SIZE, Globals.GAP_SIZE, true);
			createLayout(mainPanelLayout, inputPanel, 0, 0, false);
		} catch (Exception e) {
			Logging.warning(this, "initGui, exception occurred ", e);
		}
	}

	public void addKillProcessListener(ActionListener l) {
		jButtonKillProcess.addActionListener(l);
	}

	public void removeKillProcessListener(ActionListener l) {
		jButtonKillProcess.removeActionListener(l);
	}

	private void createLayout(GroupLayout layout, Component comp, int vgap, int hgap, boolean addInputField) {
		int pref = Globals.BUTTON_HEIGHT;
		int max = Short.MAX_VALUE;
		layout.setAutoCreateGaps(true);
		SequentialGroup verticalGroup = layout.createSequentialGroup();

		verticalGroup.addGap(vgap).addComponent(comp);
		if (addInputField) {
			verticalGroup.addGroup(layout.createParallelGroup().addGap(vgap, vgap, vgap)
					.addComponent(jButtonClose, pref, pref, pref).addComponent(jButtonClear, pref, pref, pref)
					.addComponent(jButtonKillProcess, pref, pref, pref).addGap(vgap));
		}

		verticalGroup.addGap(vgap);

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap).addComponent(comp).addGap(hgap));
		if (addInputField) {
			horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap, hgap, max)
					.addComponent(jButtonClose, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addComponent(jButtonClear, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addComponent(jButtonKillProcess, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addGap(hgap));
		}

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
	}

	private void clear() {
		output.setText("");
	}

	public void appendLater(String line) {

		append(line);
	}

	@Override
	public void append(String caller, String line) {
		Logging.debug(this, "append " + line);
		int callerlength = caller.length();

		StringBuilder callerBuilder = new StringBuilder(caller);
		for (int i = callerlength; i <= INFO_LENGTH; i++) {
			callerBuilder.append(" ");
		}

		super.append(callerBuilder.toString(), line);
	}
}
