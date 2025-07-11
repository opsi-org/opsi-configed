/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.serverconsole;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.serverconsole.command.CommandFactory;
import de.uib.configed.gui.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class OpsiSetRightsParameterDialog {
	private static final int COMBOBOX_LIST_WIDTH = 200;

	private JPanel inputPanel = new JPanel();

	private JLabel jLabelInfo;
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonSearchDir;

	private SingleCommandOpsiSetRights commandOpsiSetRights;
	private List<String> additionalDefaultPaths = new ArrayList<>();
	private CompletionComboButton completion;

	private ConfigedMain configedMain;

	public OpsiSetRightsParameterDialog(ConfigedMain configedMain, SingleCommandOpsiSetRights command) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		commandOpsiSetRights = command;
		init();
		initLayout();

		JButton jButtonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		jButtonExecute.addActionListener(actionEvent -> execute());

		JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { jButtonExecute, Configed.getResourceValue("buttonCancel") });
		Utils.enableDialogResizing(optionPane);
		JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("SingleCommandOpsiSetRights.title"));
		dialog.setModal(false);
		dialog.pack();

		dialog.setVisible(true);

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void init() {
		additionalDefaultPaths.add(CommandFactory.WEBDAV_OPSI_PATH_VAR_DEPOT);
		completion = new CompletionComboButton(additionalDefaultPaths);

		jLabelInfo = new JLabel(Configed.getResourceValue("SingleCommandOpsiSetRights.additionalPath"));
		jLabelInfo.setFont(jLabelInfo.getFont().deriveFont(Font.BOLD));

		jButtonSearchDir = completion.getButton();
		jComboBoxAutoCompletion = completion.getCombobox();

		jComboBoxAutoCompletion.setEnabled(true);
		jComboBoxAutoCompletion.addItem("");
		jComboBoxAutoCompletion.setSelectedItem("");
		((DefaultListCellRenderer) jComboBoxAutoCompletion.getRenderer())
				.setPreferredSize(new Dimension(COMBOBOX_LIST_WIDTH, (Integer) UIManager.get("Table.rowHeight")));
	}

	private void execute() {
		commandOpsiSetRights.setDir(Utils.getServerPathFromWebDAVPath(completion.comboBoxGetStringItem()));
		Logging.info(this, "doAction2 opsi-set-rights with path: ",
				Utils.getServerPathFromWebDAVPath(commandOpsiSetRights.getDir()));
		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain, commandOpsiSetRights);
				executor.execute();
			}
		}.start();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup()
				.addComponent(jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(inputPanelLayout.createSequentialGroup()
						.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
				.addComponent(
						jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));
	}
}
