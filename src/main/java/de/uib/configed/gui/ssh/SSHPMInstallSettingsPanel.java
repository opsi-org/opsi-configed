/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SSHPMInstallSettingsPanel extends SSHPMInstallPanel {

	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelUpdateInstalled = new JLabel();
	private JLabel jLabelSetupInstalled = new JLabel();
	private JLabel jLabelProperties = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();

	private JComboBox<Integer> jComboBoxVerbosity;
	private JTextField jTextFieldSelecteddepots;
	private JButton jButtonDepotselection;
	private JCheckBox jCheckBoxProperties;
	private JCheckBox jCheckBoxUpdateInstalled;
	private JCheckBox jCheckBoxSetupInstalled;

	private FDepotselectionList fDepotList;
	private List<String> depots;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SSHPMInstallSettingsPanel(JDialog dia) {

		if (dia != null) {
			setFDepotList(dia);
		}

		initComponents();
		initLayout();
		initDepots();
	}

	private void initComponents() {

		jLabelOn.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jLabelProperties.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties"));

		jButtonDepotselection = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
		jButtonDepotselection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			if (jButtonDepotselection != null) {
				fDepotList.setLocationRelativeTo(jButtonDepotselection);
			}

			fDepotList.setVisible(true);
		});

		jTextFieldSelecteddepots = new JTextField();
		jTextFieldSelecteddepots.setEditable(false);

		jComboBoxVerbosity = new JComboBox<>();
		jComboBoxVerbosity.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++) {
			jComboBoxVerbosity.addItem(i);
		}

		jComboBoxVerbosity.setSelectedItem(1);

		jCheckBoxProperties = new JCheckBox();
		jCheckBoxProperties.setSelected(true);
		jLabelUpdateInstalled.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.updateInstalled"));
		jCheckBoxUpdateInstalled = new JCheckBox();

		jLabelSetupInstalled.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.setupInstalled"));
		jCheckBoxSetupInstalled = new JCheckBox();

	}

	private void setFDepotList(JDialog dia) {
		fDepotList = new FDepotselectionList(dia) {
			@Override
			public void setListData(List<String> v) {
				if (v == null || v.isEmpty()) {
					setListData(new ArrayList<>());
					jButton1.setEnabled(false);
				} else {
					super.setListData(v);
					jButton1.setEnabled(true);
				}
			}

			@Override
			public void doAction2() {
				jTextFieldSelecteddepots.setText(produceDepotParameter());
				super.doAction2();
			}
		};
	}

	private void initLayout() {
		if (!Main.THEMES) {
			this.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
								.addGroup(layout.createSequentialGroup()
										.addComponent(jLabelOn, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(jTextFieldSelecteddepots, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
								.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelProperties, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelSetupInstalled, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelUpdateInstalled, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)
								.addGroup(layout.createParallelGroup()
										.addComponent(jButtonDepotselection, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addComponent(jComboBoxVerbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addComponent(jCheckBoxProperties, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBoxSetupInstalled, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBoxUpdateInstalled, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelOn, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jTextFieldSelecteddepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jButtonDepotselection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxVerbosity, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelProperties, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxProperties, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelSetupInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxSetupInstalled, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelUpdateInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxUpdateInstalled, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));
	}

	private List<String> getAllowedInstallTargets() {
		List<String> result = new ArrayList<>();

		if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
			jTextFieldSelecteddepots.setEditable(true);
			result.add(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS);
			result.add(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL);
		} else {
			jTextFieldSelecteddepots.setEditable(false);
		}

		for (String depot : persistenceController.getHostInfoCollections().getDepotNamesList()) {
			if (persistenceController.getUserRolesConfigDataService().hasDepotPermission(depot)) {
				result.add(depot);
			}
		}

		Logging.info(this, "getAllowedInstallTargets " + result);

		return result;
	}

	private String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = fDepotList.getSelectedDepots();

		if (selectedDepots.isEmpty()) {
			if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
				depotParameter = OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS;
			} else if (!depots.isEmpty()) {
				depotParameter = depots.get(0);
			} else {
				Logging.warning(this, "cannot find depot to set depotParameter");
			}
		} else {
			if (selectedDepots.contains(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS)) {
				depotParameter = "";
			} else if (selectedDepots.contains(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL)) {
				depotParameter = "all";
			} else {
				StringBuilder sb = new StringBuilder();
				for (String s : selectedDepots) {
					sb.append(s);
					sb.append(",");
				}
				depotParameter = sb.toString();
				depotParameter = depotParameter.substring(0, depotParameter.length() - 1);
			}
		}

		Logging.info(this, "produce depot parameter " + depotParameter);
		return depotParameter;
	}

	private void initDepots() {
		depots = getAllowedInstallTargets();
		Logging.info(this, "depots: " + depots.toString());
		fDepotList.setListData(depots);
		if (depots.isEmpty()) {
			// probably no permission

			// To DO:
			jButtonDepotselection.setVisible(false);
		}
		jTextFieldSelecteddepots.setText("" + depots.get(0));
	}

	public CommandOpsiPackageManagerInstall updateCommand(CommandOpsiPackageManagerInstall basicCommand) {
		// settings for command c:
		basicCommand.setVerbosity((int) jComboBoxVerbosity.getSelectedItem());
		basicCommand.setProperty(jCheckBoxProperties.isSelected());
		basicCommand.setUpdateInstalled(jCheckBoxUpdateInstalled.isSelected());
		basicCommand.setSetupInstalled(jCheckBoxSetupInstalled.isSelected());
		if (jTextFieldSelecteddepots.getText().contains(
				Configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS"))) {
			basicCommand.setDepotForPInstall("");
		} else {
			basicCommand.setDepotForPInstall(jTextFieldSelecteddepots.getText());
		}

		return basicCommand;
	}
}
