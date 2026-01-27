/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiPackageManagerUninstall;
import de.uib.configed.gui.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PackageManagerUninstallParameterDialog {
	private static final String DEPOT_SELECTION_ALL_WHERE_INSTALLED = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_ALL_WHERE_INSTALLED");

	private JPanel uninstallPanel = new JPanel();

	private JLabel jLabelUninstall;
	private JLabel jLabelOn;

	protected JLabel jLabelLoglevel;

	private JComboBox<String> jComboBoxOpsiProducts;
	private JComboBox<Integer> jComboBoxLogLevel;

	private JCheckBox checkBoxKeepFiles;

	private JTextField textFieldSelectedDepots;

	private JButton jButtonDepotSelection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ListSelectionDialog depotSelection;

	private List<String> possibleDepots;

	private SingleCommandOpsiPackageManagerUninstall commandPMUninstall = new SingleCommandOpsiPackageManagerUninstall();

	private ConfigedMain configedMain;

	public PackageManagerUninstallParameterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;

		init();

		textFieldSelectedDepots.setText("");

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JOptionPane optionPane = new JOptionPane(uninstallPanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { buttonExecute, Configed.getResourceValue("buttonCancel") });

		JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageManagerUninstallParameterDialog.title"));
		dialog.setModal(false);

		depotSelection = new ListSelectionDialog(dialog, Configed.getResourceValue("FDepotselectionList.title"));

		dialog.setVisible(true);
	}

	private String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = depotSelection.getSelectedValues();

		Logging.debug(this, "produceDepotParameter, selectedDepots ", selectedDepots);

		if (selectedDepots.isEmpty()) {
			if (persistenceController.getDataServices().userRoles.hasDepotsFullPermissionPD()) {
				depotParameter = PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS;
			} else if (!possibleDepots.isEmpty()) {
				depotParameter = possibleDepots.get(0);
			} else {
				// Do nothing if no depots are available
			}
		} else {
			if (selectedDepots.contains(PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS)) {
				depotParameter = PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS;
			} else if (selectedDepots.contains(DEPOT_SELECTION_ALL_WHERE_INSTALLED)) {
				int startIndex = possibleDepots.indexOf(DEPOT_SELECTION_ALL_WHERE_INSTALLED) + 1;
				int endIndex = possibleDepots.size();

				depotParameter = possibleDepots.subList(startIndex, Math.max(startIndex, endIndex)).toString();
				depotParameter = depotParameter.substring(1, depotParameter.length() - 1);
			} else {
				depotParameter = selectedDepots.toString();
				depotParameter = depotParameter.substring(1, depotParameter.length() - 1);
			}
		}

		Logging.info(this, "produce depot parameter ", depotParameter);

		return depotParameter;
	}

	private List<String> getPossibleDepots() {
		String selectedProduct = (String) jComboBoxOpsiProducts.getSelectedItem();

		List<String> result = new ArrayList<>();

		if (persistenceController.getDataServices().userRoles.hasDepotsFullPermissionPD()) {
			textFieldSelectedDepots.setEditable(true);
			result.add(PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS);
			result.add(DEPOT_SELECTION_ALL_WHERE_INSTALLED);
		} else {
			textFieldSelectedDepots.setEditable(false);
		}

		for (String depot : persistenceController.getDataServices().hostInfoCollections.getAllDepotNamesList()) {
			if (isPossibleDepot(depot, selectedProduct)) {
				Logging.info(this, "taking this depot ", depot);
				result.add(depot);
			}
		}

		Logging.info(this, "getPossibleDepots ", result);

		return result;
	}

	private boolean isPossibleDepot(String depot, String selectedProduct) {
		if (!persistenceController.getDataServices().userRoles.hasDepotPermission(depot)) {
			return false;
		}

		if (persistenceController.getDataServices().product.getDepot2LocalbootProductsPD().get(depot) != null
				&& persistenceController.getDataServices().product.getDepot2LocalbootProductsPD().get(depot)
						.containsKey(selectedProduct)) {
			return true;
		}

		return persistenceController.getDataServices().product.getDepot2NetbootProductsPD().get(depot) != null
				&& persistenceController.getDataServices().product.getDepot2NetbootProductsPD().get(depot)
						.containsKey(selectedProduct);
	}

	private void initDepots() {
		possibleDepots = getPossibleDepots();
		depotSelection.setListData(possibleDepots);
		if (possibleDepots.isEmpty()) {
			// probably no permission

			textFieldSelectedDepots.setText("");
		} else {
			textFieldSelectedDepots.setText("" + possibleDepots.get(0));
		}
	}

	private void init() {
		jLabelUninstall = Utils.createBoldLabel("PackageManagerUninstallParameterDialog.jLabelUninstall");
		jLabelLoglevel = Utils.createBoldLabel("PackageManagerUninstallParameterDialog.jLabelLoglevel");

		jComboBoxLogLevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLogLevel.addItem(i);
		}

		jComboBoxLogLevel.setSelectedItem(4);
		jComboBoxLogLevel.addItemListener(itemEvent -> updateLoglevel());

		checkBoxKeepFiles = new JCheckBox(
				Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelKeepFiles"));
		checkBoxKeepFiles.addItemListener(itemEvent -> changeKeepFiles());

		jComboBoxOpsiProducts = new JComboBox<>();
		jComboBoxOpsiProducts.setRenderer(new DefaultListCellRenderer());
		jComboBoxOpsiProducts.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
		jComboBoxOpsiProducts.setEnabled(true);
		jComboBoxOpsiProducts.setEditable(false);

		jComboBoxOpsiProducts.addItemListener(itemEvent -> textFieldSelectedDepots.setText(""));

		jLabelOn = Utils.createBoldLabel("PackageManagerUninstallParameterDialog.jLabelOn");

		jButtonDepotSelection = new JButton(Icons.getIntellijIcon("edit"));
		jButtonDepotSelection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			depotSelection.show();

			if (depotSelection.wasAccepted()) {
				textFieldSelectedDepots.setText(produceDepotParameter());
			}
		});

		textFieldSelectedDepots = new JTextField();
		textFieldSelectedDepots.setEditable(false);

		initLayout();
		resetProducts();
		changeProduct("");
		updateLoglevel();
	}

	private void resetProducts() {
		Logging.info(this, "resetProducts in combobox opsi products");
		jComboBoxOpsiProducts.removeAllItems();

		for (String item : persistenceController.getDataServices().product.getProductIdsPD()) {
			jComboBoxOpsiProducts.addItem(item);
		}
	}

	private void updateLoglevel() {
		Logging.info(this, "change loglevel , selected ", jComboBoxLogLevel.getSelectedItem());
		commandPMUninstall.setLoglevel((int) jComboBoxLogLevel.getSelectedItem());
	}

	private void changeKeepFiles() {
		if (checkBoxKeepFiles.isSelected()) {
			commandPMUninstall.enableKeepingFiles();
		} else {
			commandPMUninstall.disableKeepingFiels();
		}
	}

	private void changeDepot() {
		if (textFieldSelectedDepots.getText().equals(PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS)) {
			commandPMUninstall.setDepot(null);
		} else if (textFieldSelectedDepots.getText().equals(DEPOT_SELECTION_ALL_WHERE_INSTALLED)) {
			commandPMUninstall.setDepot("all");
		} else {
			commandPMUninstall.setDepot(textFieldSelectedDepots.getText());
		}
	}

	private void changeProduct(String prod) {
		commandPMUninstall.setOpsiproduct(prod);
	}

	private boolean confirmAction() {
		String message = Configed.getResourceValue("PackageManagerUninstallParameterDialog.confirm") + "\n"
				+ jComboBoxOpsiProducts.getSelectedItem() + "\n\n"
				+ Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelOn") + "\n\n"
				+ textFieldSelectedDepots.getText();

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("PackageManagerUninstallParameterDialog.title"),
				JOptionPane.OK_CANCEL_OPTION);

		return answer == JOptionPane.OK_OPTION;
	}

	private void execute() {
		changeDepot();
		final String prod = (String) jComboBoxOpsiProducts.getSelectedItem();
		Logging.info(this, "doAction3 uninstall  ", prod);

		changeProduct(prod);

		if (!commandPMUninstall.checkCommand() || !confirmAction()) {
			return;
		}

		Thread execThread = new Thread() {
			@Override
			public void run() {
				Logging.info(this, "start exec thread ");
				CommandExecutor executor = new CommandExecutor(configedMain, commandPMUninstall);
				executor.execute();
				Logging.debug(this, "end exec thread");
			}
		};

		execThread.start();
	}

	private void initLayout() {
		uninstallPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		uninstallPanel.add(jLabelUninstall);
		uninstallPanel.add(jComboBoxOpsiProducts, "growx, gapbottom " + Globals.GAP_SIZE);
		uninstallPanel.add(jLabelOn);
		uninstallPanel.add(textFieldSelectedDepots, "split 2, growx, gapright " + Globals.GAP_SIZE);
		uninstallPanel.add(jButtonDepotSelection, "wrap, gapbottom " + Globals.GAP_SIZE);
		uninstallPanel.add(jLabelLoglevel);
		uninstallPanel.add(jComboBoxLogLevel, "gapbottom " + Globals.GAP_SIZE);
		uninstallPanel.add(checkBoxKeepFiles);
	}
}
