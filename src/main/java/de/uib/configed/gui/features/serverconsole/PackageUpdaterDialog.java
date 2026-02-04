/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandPackageUpdater;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PackageUpdaterDialog {
	private JComboBox<String> jComboBoxActions;
	private JComboBox<String> jComboBoxRepos;
	private SingleCommandPackageUpdater command;

	private ConfigedMain configedMain;

	public PackageUpdaterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		command = new SingleCommandPackageUpdater();
		Logging.info(this, "with command");
		retrieveRepos();
		JPanel panel = initPanel();

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null,
				new Object[] { buttonExecute, Configed.getResourceValue("buttonCancel") });

		JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageUpdaterDialog.title"));
		dialog.setModal(false);
		dialog.setVisible(true);
	}

	private void retrieveRepos() {
		CommandExecutor executor = new CommandExecutor(configedMain, command);
		executor.setWithGUI(false);
		String result = executor.execute();

		if (result == null) {
			Logging.error("retrieve repos FAILED");
			command.setRepos(null);
		} else {
			String[] lines = result.split("\n");
			Map<String, String> repos = new HashMap<>();
			for (int i = 1; i < lines.length; i++) {
				String repostatus = lines[i].split(":")[0];

				// escaping ansicodes
				repostatus = repostatus.replace("\\[[0-9];[0-9][0-9];[0-9][0-9]m", "");
				repostatus = repostatus.replace("\u001B", "").replace("\u001B", "");

				String[] repoStatus = repostatus.split("\\(");
				repoStatus[1] = repoStatus[1].split("\\)")[0];
				repos.put(repoStatus[0], repoStatus[1]);
			}
			command.setRepos(repos);
		}
	}

	private void execute() {
		command.setAction(command.getAction((String) jComboBoxActions.getSelectedItem()));
		String repo = (String) jComboBoxRepos.getSelectedItem();
		if (repo.equals(Configed.getResourceValue("PackageUpdaterDialog.allrepositories"))) {
			command.setRepo(null);
		} else {
			command.setRepo(repo);
		}

		Logging.info(this, "doAction2 opsi-package-updater: ", command);
		CommandExecutor executor = new CommandExecutor(configedMain, command);
		executor.execute();
	}

	private JPanel initPanel() {
		JLabel jLabelInfo = SwingUtils.createBoldLabel("PackageUpdaterDialog.info");
		JLabel jLabelRepos = SwingUtils.createBoldLabel("PackageUpdaterDialog.repos");

		jComboBoxActions = new JComboBox<>(command.getActionsText());
		jComboBoxActions.addItemListener((ItemEvent itemEvent) -> {
			if (((String) itemEvent.getItem())
					.equals(Configed.getResourceValue("SingleCommandPackageUpdater.action.list"))) {
				jComboBoxRepos.setEnabled(itemEvent.getStateChange() != ItemEvent.SELECTED);
			}
		});

		if (command.getRepos() != null) {
			jComboBoxRepos = new JComboBox<>(command.getRepos().keySet().toArray(new String[0]));
		} else {
			jComboBoxRepos = new JComboBox<>();
		}

		jComboBoxRepos.addItem(Configed.getResourceValue("PackageUpdaterDialog.allrepositories"));
		jComboBoxRepos.setSelectedItem(Configed.getResourceValue("PackageUpdaterDialog.allrepositories"));
		jComboBoxActions.setEnabled(true);

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "[grow, fill]", "[]0"));
		panel.add(jLabelInfo);
		panel.add(jComboBoxActions, "growx, gapbottom " + Globals.GAP_SIZE);
		panel.add(jLabelRepos);
		panel.add(jComboBoxRepos, "growx, gapbottom " + Globals.GAP_SIZE);

		return panel;
	}
}
