/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandPackageUpdater;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class PackageUpdaterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel jLabelInfo;
	private JLabel jLabelRepos;
	private JComboBox<String> jComboBoxActions;
	private JComboBox<String> jComboBoxRepos;
	private JButton jButtonDoAction;
	private SingleCommandPackageUpdater command;

	private ConfigedMain configedMain;

	public PackageUpdaterDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("PackageUpdaterDialog.title"), false);
		this.configedMain = configedMain;
		command = new SingleCommandPackageUpdater();
		Logging.info(this.getClass(), "with command");
		retrieveRepos();
		init();
		initLayout();
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

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		jLabelInfo = new JLabel(Configed.getResourceValue("PackageUpdaterDialog.info"));
		jLabelRepos = new JLabel(Configed.getResourceValue("PackageUpdaterDialog.repos"));
		inputPanel.add(jLabelInfo);
		inputPanel.add(jLabelRepos);
		jButtonDoAction = new JButton(Configed.getResourceValue("buttonExecute"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonDoAction.addActionListener((ActionEvent actionEvent) -> {
				Logging.info(this, "btn_doAction pressed");
				doAction2();
			});
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonDoAction);

		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

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
		inputPanel.add(jComboBoxActions);
		inputPanel.add(jComboBoxRepos);
	}

	private void setComponentsEnabled(boolean value) {
		jButtonDoAction.setEnabled(value);
	}

	@Override
	public void doAction2() {
		command.setAction(command.getAction((String) jComboBoxActions.getSelectedItem()));
		String repo = (String) jComboBoxRepos.getSelectedItem();
		if (repo.equals(Configed.getResourceValue("PackageUpdaterDialog.allrepositories"))) {
			command.setRepo(null);
		} else {
			command.setRepo(repo);
		}

		Logging.info(this, "doAction2 opsi-package-updater: " + command.toString());
		CommandExecutor executor = new CommandExecutor(configedMain, command);
		executor.execute();
	}

	private void cancel() {
		super.doAction1();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jComboBoxActions, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addComponent(jComboBoxRepos, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout
				.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelInfo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxActions, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelRepos, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxRepos, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(2 * Globals.GAP_SIZE));

		this.setSize(600, 210);
		this.setLocationRelativeTo(ConfigedMain.getMainFrame());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
}
