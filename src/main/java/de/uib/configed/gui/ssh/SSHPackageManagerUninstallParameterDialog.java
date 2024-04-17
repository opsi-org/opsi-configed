/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.configed.gui.FShowList;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class SSHPackageManagerUninstallParameterDialog extends SSHPackageManagerParameterDialog {
	private JPanel uninstallPanel = new JPanel();

	private JLabel jLabelUninstall = new JLabel();
	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelKeepFiles = new JLabel();

	private JComboBox<String> jComboBoxOpsiProducts;
	private JComboBox<Integer> jComboBoxVerbosity;

	private JCheckBox checkBoxKeepFiles;

	private JTextField textFieldProduct;
	private JTextField textFieldSelectedDepots;

	private JButton jButtonDepotSelection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private FDepotselectionList fDepotList;

	private List<String> possibleDepots;

	private CommandOpsiPackageManagerUninstall commandPMUninstall = new CommandOpsiPackageManagerUninstall();

	public SSHPackageManagerUninstallParameterDialog(ConfigedMain configedMain) {
		super(Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"));

		this.configedMain = configedMain;

		fDepotList = new FDepotselectionList(this, configedMain) {
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
				textFieldSelectedDepots.setText(produceDepotParameter());
				super.doAction2();
			}
		};

		init();

		// requires valid depot selection
		jButtonExecute.setEnabled(false);
		textFieldSelectedDepots.setText("");

		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setSize(800, 350);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());

		super.setVisible(true);
	}

	@Override
	protected void setComponentsEnabled(boolean value) {
		super.setComponentsEnabled(value);
		jComboBoxOpsiProducts.setEnabled(true);
		jComboBoxOpsiProducts.setEditable(false);
		jComboBoxVerbosity.setEnabled(value);
		jComboBoxVerbosity.setEditable(value);

		checkBoxKeepFiles.setEnabled(value);

		jButtonDepotSelection.setEnabled(value);
		jButtonExecute.setEnabled(false);
	}

	private String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = fDepotList.getSelectedDepots();

		Logging.debug(this, "produceDepotParameter, selectedDepots " + selectedDepots);

		if (selectedDepots.isEmpty()) {
			if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
				depotParameter = OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS;
			} else if (!possibleDepots.isEmpty()) {
				depotParameter = possibleDepots.get(0);
			} else {
				jButtonExecute.setEnabled(false);
			}
		} else {
			jButtonExecute.setEnabled(true);

			if (selectedDepots.contains(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS)) {
				depotParameter = OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS;
			} else if (selectedDepots
					.contains(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL_WHERE_INSTALLED)) {
				int startIndex = possibleDepots
						.indexOf(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL_WHERE_INSTALLED) + 1;
				int endIndex = possibleDepots.size();

				depotParameter = possibleDepots.subList(startIndex, Math.max(startIndex, endIndex)).toString();
				depotParameter = depotParameter.substring(1, depotParameter.length() - 1);
			} else {
				depotParameter = selectedDepots.toString();
				depotParameter = depotParameter.substring(1, depotParameter.length() - 1);
			}
		}

		Logging.info(this, "produce depot parameter " + depotParameter);

		return depotParameter;
	}

	private List<String> getPossibleDepots() {
		String selectedProduct = (String) jComboBoxOpsiProducts.getSelectedItem();

		List<String> result = new ArrayList<>();

		if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
			textFieldSelectedDepots.setEditable(true);
			result.add(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_NODEPOTS);
			result.add(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL_WHERE_INSTALLED);
		} else {
			textFieldSelectedDepots.setEditable(false);
		}

		for (String depot : persistenceController.getHostInfoCollections().getDepotNamesList()) {
			if (isPossibleDepot(depot, selectedProduct)) {
				Logging.info(this, "taking this depot " + depot);
				result.add(depot);
			}
		}

		Logging.info(this, "getPossibleDepots " + result);

		return result;
	}

	private boolean isPossibleDepot(String depot, String selectedProduct) {
		if (!persistenceController.getUserRolesConfigDataService().hasDepotPermission(depot)) {
			return false;
		}

		if (persistenceController.getProductDataService().getDepot2LocalbootProductsPD().get(depot) != null
				&& persistenceController.getProductDataService().getDepot2LocalbootProductsPD().get(depot)
						.containsKey(selectedProduct)) {
			return true;
		}

		return persistenceController.getProductDataService().getDepot2NetbootProductsPD().get(depot) != null
				&& persistenceController.getProductDataService().getDepot2NetbootProductsPD().get(depot)
						.containsKey(selectedProduct);
	}

	private void initDepots() {
		possibleDepots = getPossibleDepots();
		fDepotList.setListData(possibleDepots);
		if (possibleDepots.isEmpty()) {
			// probably no permission

			jButtonExecute.setVisible(false);
			textFieldSelectedDepots.setText("");
		} else {
			textFieldSelectedDepots.setText("" + possibleDepots.get(0));
		}
	}

	private void init() {
		getContentPane().add(uninstallPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setPreferredSize(new Dimension(376, 220));

		jLabelUninstall.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelUninstall"));

		jComboBoxVerbosity = new JComboBox<>();
		jComboBoxVerbosity.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++) {
			jComboBoxVerbosity.addItem(i);
		}

		jComboBoxVerbosity.setSelectedItem(1);
		jComboBoxVerbosity.addItemListener(itemEvent -> changeVerbosity());

		jLabelKeepFiles.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelKeepFiles"));
		checkBoxKeepFiles = new JCheckBox();
		checkBoxKeepFiles.addItemListener(itemEvent -> changeKeepFiles());

		textFieldProduct = new JTextField();

		textFieldProduct.setEditable(false);

		jComboBoxOpsiProducts = new JComboBox<>();
		jComboBoxOpsiProducts.setRenderer(new DefaultListCellRenderer());
		jComboBoxOpsiProducts.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);

		jComboBoxOpsiProducts.addItemListener((ItemEvent itemEvent) -> {
			textFieldSelectedDepots.setText("");

			jButtonExecute.setEnabled(false);
			textFieldProduct.setText((String) jComboBoxOpsiProducts.getSelectedItem());
		});

		jLabelOn.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn"));

		jButtonDepotSelection = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
		jButtonDepotSelection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			fDepotList.setLocationRelativeTo(this);
			fDepotList.setVisible(true);
		});

		textFieldSelectedDepots = new JTextField();
		textFieldSelectedDepots.setEditable(false);

		initLabels();
		initButtons(this);
		initLayout();
		resetProducts();
		changeProduct("");
		changeVerbosity();
	}

	@Override
	protected void consolidate() {
		super.consolidate();
		resetProducts();
	}

	private void resetProducts() {
		Logging.info(this, "resetProducts in cb_opsiproducts");
		jComboBoxOpsiProducts.removeAllItems();
		if (persistenceController == null) {
			Logging.error(this, "resetProducts PersistenceController null");
		} else {
			NavigableSet<String> productnames = persistenceController.getProductDataService().getProductIdsPD();
			for (String item : productnames) {
				jComboBoxOpsiProducts.addItem(item);
			}
		}
	}

	private void changeKeepFiles() {
		commandPMUninstall.setKeepFiles(checkBoxKeepFiles.isSelected());
	}

	private void changeDepot() {
		if (textFieldSelectedDepots.getText().equals(
				Configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS"))) {
			commandPMUninstall.setDepot(null);
		} else if (textFieldSelectedDepots.getText().equals(Configed
				.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL_WHERE_INSTALLED"))) {
			commandPMUninstall.setDepot("all");
		} else {
			commandPMUninstall.setDepot(textFieldSelectedDepots.getText());
		}
	}

	private void changeVerbosity() {
		Logging.info(this, "changeVerbosity , selected " + jComboBoxVerbosity.getSelectedItem());
		commandPMUninstall.setVerbosity((int) jComboBoxVerbosity.getSelectedItem());
	}

	private void changeProduct(String prod) {
		commandPMUninstall.setOpsiproduct(prod);
	}

	/* This method is called when button 1 is pressed */
	private boolean confirmAction() {
		FShowList fConfirmAction = new FShowList(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 400,
				200);

		fConfirmAction.setMessage(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.confirm") + "\n"
						+ textFieldProduct.getText() + "\n\n"
						+ Configed
								.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn")
						+ "\n\n" + textFieldSelectedDepots.getText());

		fConfirmAction.setLocationRelativeTo(this);
		fConfirmAction.setAlwaysOnTop(true);
		fConfirmAction.setVisible(true);

		return fConfirmAction.getResult() == 2;
	}

	@Override
	public void doAction3() {
		changeDepot();
		final String prod = textFieldProduct.getText();
		Logging.info(this, "doAction3 uninstall  " + prod);

		changeProduct(prod);

		if (!commandPMUninstall.checkCommand() || !confirmAction()) {
			return;
		}

		Thread execThread = new Thread() {
			@Override
			public void run() {
				Logging.info(this, "start exec thread ");

				new SSHConnectExec(commandPMUninstall);

				Logging.debug(this, "end exec thread");
			}
		};

		execThread.start();
	}

	@Override
	public void doAction1() {
		this.setVisible(false);
		this.dispose();
	}

	@Override
	public void leave() {
		fDepotList.exit();
		super.leave();
	}

	private void initLayout() {
		GroupLayout uninstallPanelLayout = new GroupLayout(uninstallPanel);
		uninstallPanel.setLayout(uninstallPanelLayout);
		uninstallPanelLayout.setHorizontalGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jLabelUninstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(uninstallPanelLayout
						.createSequentialGroup().addGroup(uninstallPanelLayout.createParallelGroup()

								.addGroup(uninstallPanelLayout.createSequentialGroup().addGap(5, 10, 20)
										.addComponent(jComboBoxOpsiProducts, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												2 * Globals.BUTTON_WIDTH)
										.addGap(Globals.MIN_GAP_SIZE)

										.addGap(5, 10, 20))
								.addGroup(uninstallPanelLayout.createSequentialGroup()
										.addComponent(jLabelOn, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(5, 10, 10).addComponent(jButtonDepotSelection,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelKeepFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(textFieldProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(textFieldSelectedDepots, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(jComboBoxVerbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)

								.addComponent(checkBoxKeepFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))));

		uninstallPanelLayout.setVerticalGroup(uninstallPanelLayout.createSequentialGroup().addComponent(jLabelUninstall)
				.addGap(Globals.GAP_SIZE)

				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxOpsiProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addComponent(textFieldProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))

				.addGap(3 * Globals.GAP_SIZE)

				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelOn, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jButtonDepotSelection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(textFieldSelectedDepots, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))

				.addGap(3 * Globals.GAP_SIZE)
				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelKeepFiles, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)

						.addComponent(checkBoxKeepFiles, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));
	}
}
