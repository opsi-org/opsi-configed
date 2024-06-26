/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

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
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandOpsiPackageManagerUninstall;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class PackageManagerUninstallParameterDialog extends PackageManagerParameterDialog {
	private static final String DEPOT_SELECTION_ALL_WHERE_INSTALLED = Configed
			.getResourceValue("SingleCommandOpsiPackageManager.DEPOT_SELECTION_ALL_WHERE_INSTALLED");

	private JPanel uninstallPanel = new JPanel();

	private JLabel jLabelUninstall = new JLabel();
	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelKeepFiles = new JLabel();

	protected JLabel jLabelLoglevel = new JLabel(Configed.getResourceValue("loglevel"));

	private JComboBox<String> jComboBoxOpsiProducts;
	private JComboBox<Integer> jComboboxLoglevel;

	private JCheckBox checkBoxKeepFiles;

	private JTextField textFieldProduct;
	private JTextField textFieldSelectedDepots;

	private JButton jButtonDepotSelection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private FDepotselectionList fDepotList;

	private List<String> possibleDepots;

	private SingleCommandOpsiPackageManagerUninstall commandPMUninstall = new SingleCommandOpsiPackageManagerUninstall();

	public PackageManagerUninstallParameterDialog(ConfigedMain configedMain) {
		super(Configed.getResourceValue("PackageManagerUninstallParameterDialog.title"));

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

		jButtonExecute.setEnabled(false);
		textFieldSelectedDepots.setText("");

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setSize(800, 350);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());

		super.setVisible(true);
	}

	private String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = fDepotList.getSelectedDepots();

		Logging.debug(this, "produceDepotParameter, selectedDepots " + selectedDepots);

		if (selectedDepots.isEmpty()) {
			if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
				depotParameter = PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS;
			} else if (!possibleDepots.isEmpty()) {
				depotParameter = possibleDepots.get(0);
			} else {
				jButtonExecute.setEnabled(false);
			}
		} else {
			jButtonExecute.setEnabled(true);

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

		Logging.info(this, "produce depot parameter " + depotParameter);

		return depotParameter;
	}

	private List<String> getPossibleDepots() {
		String selectedProduct = (String) jComboBoxOpsiProducts.getSelectedItem();

		List<String> result = new ArrayList<>();

		if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
			textFieldSelectedDepots.setEditable(true);
			result.add(PMInstallSettingsPanel.DEPOT_SELECTION_NODEPOTS);
			result.add(DEPOT_SELECTION_ALL_WHERE_INSTALLED);
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

		jLabelUninstall.setText(Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelUninstall"));

		jComboboxLoglevel = new JComboBox<>();
		jComboboxLoglevel.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		jComboboxLoglevel.setEditable(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		for (int i = 3; i <= 9; i++) {
			jComboboxLoglevel.addItem(i);
		}

		jComboboxLoglevel.setSelectedItem(4);
		jComboboxLoglevel.addItemListener(itemEvent -> updateLoglevel());

		jLabelKeepFiles.setText(Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelKeepFiles"));
		checkBoxKeepFiles = new JCheckBox();
		checkBoxKeepFiles.addItemListener(itemEvent -> changeKeepFiles());
		checkBoxKeepFiles.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

		textFieldProduct = new JTextField();

		textFieldProduct.setEditable(false);

		jComboBoxOpsiProducts = new JComboBox<>();
		jComboBoxOpsiProducts.setRenderer(new DefaultListCellRenderer());
		jComboBoxOpsiProducts.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
		jComboBoxOpsiProducts.setEnabled(true);
		jComboBoxOpsiProducts.setEditable(false);

		jComboBoxOpsiProducts.addItemListener((ItemEvent itemEvent) -> {
			textFieldSelectedDepots.setText("");

			jButtonExecute.setEnabled(false);
			textFieldProduct.setText((String) jComboBoxOpsiProducts.getSelectedItem());
		});

		jLabelOn.setText(Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelOn"));

		jButtonDepotSelection = new JButton(Configed.getResourceValue("depotSelection"));
		jButtonDepotSelection.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		jButtonDepotSelection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			fDepotList.setLocationRelativeTo(this);
			fDepotList.setVisible(true);
		});

		textFieldSelectedDepots = new JTextField();
		textFieldSelectedDepots.setEditable(false);

		initButtons(this);
		initLayout();
		resetProducts();
		changeProduct("");
		updateLoglevel();
	}

	private void resetProducts() {
		Logging.info(this, "resetProducts in combobox opsi products");
		jComboBoxOpsiProducts.removeAllItems();

		for (String item : persistenceController.getProductDataService().getProductIdsPD()) {
			jComboBoxOpsiProducts.addItem(item);
		}
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

	private void updateLoglevel() {
		Logging.info(this, "change loglevel , selected " + jComboboxLoglevel.getSelectedItem());
		commandPMUninstall.setLoglevel((int) jComboboxLoglevel.getSelectedItem());
	}

	private void changeProduct(String prod) {
		commandPMUninstall.setOpsiproduct(prod);
	}

	private boolean confirmAction() {
		FShowList fConfirmAction = new FShowList(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageManagerUninstallParameterDialog.title"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 400,
				200);

		fConfirmAction.setMessage(Configed.getResourceValue("PackageManagerUninstallParameterDialog.confirm") + "\n"
				+ textFieldProduct.getText() + "\n\n"
				+ Configed.getResourceValue("PackageManagerUninstallParameterDialog.jLabelOn") + "\n\n"
				+ textFieldSelectedDepots.getText());

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
				CommandExecutor executor = new CommandExecutor(configedMain, commandPMUninstall);
				executor.execute();
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
		uninstallPanelLayout.setAutoCreateGaps(true);
		uninstallPanelLayout.setAutoCreateContainerGaps(true);
		uninstallPanel.setLayout(uninstallPanelLayout);
		uninstallPanelLayout
				.setHorizontalGroup(
						uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelUninstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.GAP_SIZE * 2)
								.addGroup(uninstallPanelLayout
										.createSequentialGroup().addGroup(uninstallPanelLayout.createParallelGroup()
												.addGroup(uninstallPanelLayout.createSequentialGroup().addGap(5, 10, 20)
														.addComponent(jComboBoxOpsiProducts, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH)
														.addGap(Globals.MIN_GAP_SIZE).addGap(5, 10, 20))
												.addGroup(uninstallPanelLayout.createSequentialGroup()
														.addComponent(jLabelOn, GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
														.addGap(5, 10, 10).addComponent(jButtonDepotSelection,
																GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
												.addComponent(jLabelLoglevel, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabelKeepFiles, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(Globals.GAP_SIZE)
										.addGroup(uninstallPanelLayout
												.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(textFieldProduct, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addComponent(textFieldSelectedDepots, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addComponent(jComboboxLoglevel, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
														Globals.ICON_WIDTH)
												.addComponent(checkBoxKeepFiles, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))));

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
						.addComponent(jLabelLoglevel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboboxLoglevel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
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
