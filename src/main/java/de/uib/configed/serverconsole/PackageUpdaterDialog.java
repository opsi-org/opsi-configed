/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.itextpdf.text.Font;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandPackageUpdater;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class PackageUpdaterDialog {

	private JComboBox<String> jComboBoxActions;
	private JComboBox<String> jComboBoxRepos;
	private SingleCommandPackageUpdater command;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public PackageUpdaterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("PackageUpdaterDialog.readOnly"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		command = new SingleCommandPackageUpdater();
		Logging.info(this, "with command");
		retrieveRepos();
		JPanel panel = initPanel();

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null,
				new Object[] { Configed.getResourceValue("buttonExecute"), Configed.getResourceValue("buttonCancel") });
		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageUpdaterDialog.title"));

		dialog.setVisible(true);
		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("buttonExecute"))) {
			execute();
		}
	}

	public JDialog getDialog() {
		return dialog;
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
		JLabel jLabelInfo = new JLabel(Configed.getResourceValue("PackageUpdaterDialog.info"));
		jLabelInfo.setFont(jLabelInfo.getFont().deriveFont(Font.BOLD));

		JLabel jLabelRepos = new JLabel(Configed.getResourceValue("PackageUpdaterDialog.repos"));
		jLabelRepos.setFont(jLabelRepos.getFont().deriveFont(Font.BOLD));

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
		GroupLayout inputPanelLayout = new GroupLayout(panel);
		panel.setLayout(inputPanelLayout);

		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup()
				.addComponent(jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxActions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jComboBoxRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
				.addComponent(jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxActions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
		return panel;
	}
}
