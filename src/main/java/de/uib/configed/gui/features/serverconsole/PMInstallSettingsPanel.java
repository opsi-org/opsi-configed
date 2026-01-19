/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PMInstallSettingsPanel extends PMInstallPanel {
	public static final String DEPOT_SELECTION_NODEPOTS = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_NODEPOTS");
	private static final String DEPOT_SELECTION_ALL = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_ALL");

	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelLoglevel = new JLabel();

	private JComboBox<Integer> jComboBoxLoglevel;

	private JTextField jTextFieldSelecteddepots;
	private JButton jButtonDepotselection;
	private JCheckBox jCheckBoxUpdateInstalled;
	private JCheckBox jCheckBoxSetupInstalled;

	private ListSelectionDialog depotSelection;
	private List<String> depots;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PMInstallSettingsPanel(JDialog dialog) {
		depotSelection = new ListSelectionDialog(dialog, Configed.getResourceValue("FDepotselectionList.title"));

		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelOn.setText(Configed.getResourceValue("PMInstallSettingsPanel.jLabelOn"));
		jLabelOn.setFont(jLabelOn.getFont().deriveFont(Font.BOLD));

		jLabelLoglevel.setText(Configed.getResourceValue("loglevel"));
		jLabelLoglevel.setFont(jLabelLoglevel.getFont().deriveFont(Font.BOLD));

		jButtonDepotselection = new JButton(Configed.getResourceValue("depotSelection"));
		jButtonDepotselection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			depotSelection.show();

			if (depotSelection.wasAccepted()) {
				jTextFieldSelecteddepots.setText(produceDepotParameter());
			}
		});

		jTextFieldSelecteddepots = new JTextField();
		jTextFieldSelecteddepots.setEditable(false);

		jComboBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLoglevel.addItem(i);
		}

		jComboBoxLoglevel.setSelectedItem(4);

		jCheckBoxUpdateInstalled = new JCheckBox(Configed.getResourceValue("PMInstallSettingsPanel.updateInstalled"));
		jCheckBoxSetupInstalled = new JCheckBox(Configed.getResourceValue("PMInstallSettingsPanel.setupInstalled"));
	}

	private void initLayout() {
		setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		add(jLabelOn);
		add(jTextFieldSelecteddepots, "split 2, growx");
		add(jButtonDepotselection, "wrap, gapbottom " + Globals.GAP_SIZE);
		add(jLabelLoglevel);
		add(jComboBoxLoglevel, "gapbottom " + Globals.GAP_SIZE);
		add(jCheckBoxSetupInstalled);
		add(jCheckBoxUpdateInstalled);
	}

	private List<String> getAllowedInstallTargets() {
		List<String> result = new ArrayList<>();

		if (persistenceController.getDataServices().userRoles.hasDepotsFullPermissionPD()) {
			jTextFieldSelecteddepots.setEditable(true);
			result.add(DEPOT_SELECTION_NODEPOTS);
			result.add(DEPOT_SELECTION_ALL);
		} else {
			jTextFieldSelecteddepots.setEditable(false);
		}

		for (String depot : persistenceController.getDataServices().hostInfoCollections.getAllDepotNamesList()) {
			if (persistenceController.getDataServices().userRoles.hasDepotPermission(depot)) {
				result.add(depot);
			}
		}

		Logging.info(this, "getAllowedInstallTargets ", result);

		return result;
	}

	private String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = depotSelection.getSelectedValues();

		if (selectedDepots.isEmpty()) {
			if (persistenceController.getDataServices().userRoles.hasDepotsFullPermissionPD()) {
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

		Logging.info(this, "produce depot parameter ", depotParameter);
		return depotParameter;
	}

	private void initDepots() {
		depots = getAllowedInstallTargets();
		Logging.info(this, "depots: ", depots);
		depotSelection.setListData(depots);
		if (depots.isEmpty()) {
			jButtonDepotselection.setVisible(false);
		}
		jTextFieldSelecteddepots.setText("" + depots.get(0));
	}

	public SingleCommandOpsiPackageManagerInstall updateCommand(SingleCommandOpsiPackageManagerInstall basicCommand) {
		basicCommand.setLoglevel((int) jComboBoxLoglevel.getSelectedItem());
		toggleUpdateBasedOnCheckBox(basicCommand);
		toggleSetupBasedOnCheckBox(basicCommand);
		setDepotBasedOnSelectedDepot(basicCommand, jTextFieldSelecteddepots.getText());
		return basicCommand;
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
