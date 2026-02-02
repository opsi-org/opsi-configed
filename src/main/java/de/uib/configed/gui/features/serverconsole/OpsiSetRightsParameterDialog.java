/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
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
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

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
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
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

		jLabelInfo = Utils.createBoldLabel("SingleCommandOpsiSetRights.additionalPath");

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
		CommandExecutor executor = new CommandExecutor(configedMain, commandOpsiSetRights);
		executor.executeAsync();
	}

	private void initLayout() {
		inputPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE, "", "[]0"));
		inputPanel.add(jLabelInfo, "wrap");
		inputPanel.add(jComboBoxAutoCompletion, "split 2, growx, gapright " + Globals.GAP_SIZE);
		inputPanel.add(jButtonSearchDir, "wrap");
	}
}
