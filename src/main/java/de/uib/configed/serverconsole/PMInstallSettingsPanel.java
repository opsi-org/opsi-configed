/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.configed.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class PMInstallSettingsPanel extends PMInstallPanel {
	public static final String DEPOT_SELECTION_NODEPOTS = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_NODEPOTS");
	private static final String DEPOT_SELECTION_ALL = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_ALL");

	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelUpdateInstalled = new JLabel();
	private JLabel jLabelSetupInstalled = new JLabel();
	private JLabel jLabelProperties = new JLabel();
	private JLabel jLabelLoglevel = new JLabel();

	private JComboBox<Integer> jComboBoxLoglevel;
	private JTextField jTextFieldSelecteddepots;
	private JButton jButtonDepotselection;
	private JCheckBox jCheckBoxProperties;
	private JCheckBox jCheckBoxUpdateInstalled;
	private JCheckBox jCheckBoxSetupInstalled;

	private FDepotselectionList fDepotList;
	private List<String> depots;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PMInstallSettingsPanel(JDialog dia, ConfigedMain configedMain) {
		if (dia != null) {
			setFDepotList(dia, configedMain);
		}

		initComponents();
		initLayout();
		initDepots();
	}

	private void initComponents() {
		jLabelOn.setText(Configed.getResourceValue("PMInstallSettingsPanel.jLabelOn"));
		jLabelLoglevel.setText(Configed.getResourceValue("loglevel"));
		jLabelProperties.setText(Configed.getResourceValue("PMInstallSettingsPanel.lbl_properties"));
		jLabelSetupInstalled.setText(Configed.getResourceValue("PMInstallSettingsPanel.setupInstalled"));
		jLabelUpdateInstalled.setText(Configed.getResourceValue("PMInstallSettingsPanel.updateInstalled"));

		jButtonDepotselection = new JButton(Configed.getResourceValue("depotSelection"));
		jButtonDepotselection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			fDepotList.setLocationRelativeTo(this);
			fDepotList.setVisible(true);
		});

		jTextFieldSelecteddepots = new JTextField();
		jTextFieldSelecteddepots.setEditable(false);

		jComboBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLoglevel.addItem(i);
		}

		jComboBoxLoglevel.setSelectedItem(4);

		jCheckBoxProperties = new JCheckBox();
		jCheckBoxProperties.setSelected(true);
		jCheckBoxUpdateInstalled = new JCheckBox();
		jCheckBoxSetupInstalled = new JCheckBox();
	}

	private void setFDepotList(JDialog dia, ConfigedMain configedMain) {
		fDepotList = new FDepotselectionList(dia, configedMain) {
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
								.addComponent(jLabelLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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

										.addComponent(jComboBoxLoglevel, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
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
						.addComponent(jLabelLoglevel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxLoglevel, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
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
			result.add(DEPOT_SELECTION_NODEPOTS);
			result.add(DEPOT_SELECTION_ALL);
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
				depotParameter = DEPOT_SELECTION_NODEPOTS;
			} else if (!depots.isEmpty()) {
				depotParameter = depots.get(0);
			} else {
				Logging.warning(this, "cannot find depot to set depotParameter");
			}
		} else {
			if (selectedDepots.contains(DEPOT_SELECTION_NODEPOTS)) {
				depotParameter = "";
			} else if (selectedDepots.contains(DEPOT_SELECTION_ALL)) {
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
			jButtonDepotselection.setVisible(false);
		}
		jTextFieldSelecteddepots.setText("" + depots.get(0));
	}

	public SingleCommandOpsiPackageManagerInstall updateCommand(SingleCommandOpsiPackageManagerInstall basicCommand) {
		basicCommand.setLoglevel((int) jComboBoxLoglevel.getSelectedItem());
		applyPropertyDefaultsBasedOnCheckBox(basicCommand);
		toggleUpdateBasedOnCheckBox(basicCommand);
		toggleSetupBasedOnCheckBox(basicCommand);
		setDepotBasedOnSelectedDepot(basicCommand, jTextFieldSelecteddepots.getText());
		return basicCommand;
	}

	private void applyPropertyDefaultsBasedOnCheckBox(SingleCommandOpsiPackageManagerInstall basicCommand) {
		if (jCheckBoxProperties.isSelected()) {
			basicCommand.keepDepotDefaults();
		} else {
			basicCommand.usePackageDefaults();
		}
	}

	private void toggleUpdateBasedOnCheckBox(SingleCommandOpsiPackageManagerInstall basicCommand) {
		if (jCheckBoxUpdateInstalled.isSelected()) {
			basicCommand.enableUpdateInstalled();
		} else {
			basicCommand.disableUpdateInstalled();
		}
	}

	private void toggleSetupBasedOnCheckBox(SingleCommandOpsiPackageManagerInstall basicCommand) {
		if (jCheckBoxSetupInstalled.isSelected()) {
			basicCommand.enableSetupInstalled();
		} else {
			basicCommand.disableSetupInstalled();
		}
	}

	private static void setDepotBasedOnSelectedDepot(SingleCommandOpsiPackageManagerInstall basicCommand,
			String selectedDepot) {
		if (selectedDepot.contains(DEPOT_SELECTION_NODEPOTS)) {
			basicCommand.setDepotForPInstall("");
		} else {
			basicCommand.setDepotForPInstall(selectedDepot);
		}
	}
}
