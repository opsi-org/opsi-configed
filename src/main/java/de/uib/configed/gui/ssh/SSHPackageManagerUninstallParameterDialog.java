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

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.IconAsButton;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JComboBoxSimpleToolTip;

public class SSHPackageManagerUninstallParameterDialog extends SSHPackageManagerParameterDialog {

	private JPanel uninstallPanel = new JPanel();

	private JLabel jLabelUninstall = new JLabel();
	private JLabel jLabelProduct = new JLabel();
	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelFullCommand = new JLabel();
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

	public SSHPackageManagerUninstallParameterDialog(ConfigedMain m) {
		super(Globals.APPNAME + "  "
				+ Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"));
		frameWidth = 850;
		frameHeight = 350;

		configedMain = m;

		fDepotList = new FDepotselectionList(this) {
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

		super.pack();
		super.setSize(frameWidth, frameHeight);
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly());

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
			if (persistenceController.isDepotsFullPermission()) {
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
				StringBuilder sb = new StringBuilder();
				int startIndex = possibleDepots
						.indexOf(OpsiServiceNOMPersistenceController.DEPOT_SELECTION_ALL_WHERE_INSTALLED);

				for (int i = startIndex + 1; i < possibleDepots.size() - 1; i++) {

					sb.append(possibleDepots.get(i));
					sb.append(",");
				}
				sb.append(possibleDepots.get(possibleDepots.size() - 1));
				depotParameter = sb.toString();
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

	private List<String> getPossibleDepots() {
		String selectedProduct = (String) jComboBoxOpsiProducts.getSelectedItem();

		List<String> result = new ArrayList<>();

		if (persistenceController.isDepotsFullPermission()) {
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
		if (!persistenceController.hasDepotPermission(depot)) {
			return false;
		}

		if (persistenceController.getDepot2LocalbootProducts().get(depot) != null
				&& persistenceController.getDepot2LocalbootProducts().get(depot).containsKey(selectedProduct)) {
			return true;
		}

		return persistenceController.getDepot2NetbootProducts().get(depot) != null
				&& persistenceController.getDepot2NetbootProducts().get(depot).containsKey(selectedProduct);
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
		if (!Main.THEMES) {
			uninstallPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}
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

		jLabelProduct.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelProduct"));

		textFieldProduct = new JTextField();
		if (!Main.THEMES) {
			textFieldProduct.setBackground(Globals.BACKGROUND_COLOR_9);
		}
		textFieldProduct.setEditable(false);

		jComboBoxOpsiProducts = new JComboBoxSimpleToolTip();
		jComboBoxOpsiProducts.setRenderer(new DefaultListCellRenderer());
		jComboBoxOpsiProducts.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);

		jComboBoxOpsiProducts.addItemListener((ItemEvent itemEvent) -> {
			textFieldSelectedDepots.setText("");

			jButtonExecute.setEnabled(false);
			textFieldProduct.setText((String) jComboBoxOpsiProducts.getSelectedItem());
		});

		IconAsButton iconButtonUpdateList = new IconAsButton("buttonUpdateList", "images/reload16.png",
				"images/reload16.png", "images/reload16.png", "images/reload16.png");
		if (!Main.THEMES) {
			iconButtonUpdateList.setBackground(Globals.BACKGROUND_COLOR_3);
		}
		iconButtonUpdateList.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_uninstall.JButtonUpdateList.tooltip"));

		iconButtonUpdateList.addActionListener((ActionEvent actionEvent) -> {
			Logging.info(this, "actionPerformed");
			resetProducts();
		});

		jLabelOn.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn"));

		jButtonDepotSelection = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
		jButtonDepotSelection.addActionListener((ActionEvent actionEvent) -> {
			initDepots();
			if (jButtonDepotSelection != null) {
				fDepotList.setLocationRelativeTo(jButtonDepotSelection);
			}

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
			NavigableSet<String> productnames = persistenceController.getProductIds();
			for (String item : productnames) {
				jComboBoxOpsiProducts.addItem(item);
			}
		}
	}

	private void updateCommand() {
		jLabelFullCommand.setText(commandPMUninstall.getCommand());
	}

	private void changeKeepFiles() {
		commandPMUninstall.setKeepFiles(checkBoxKeepFiles.isSelected());
		updateCommand();
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

		updateCommand();
	}

	private void changeVerbosity() {
		Logging.info(this, "changeVerbosity , selected " + jComboBoxVerbosity.getSelectedItem());
		commandPMUninstall.setVerbosity((int) jComboBoxVerbosity.getSelectedItem());
		updateCommand();
	}

	private void changeProduct(String prod) {
		commandPMUninstall.setOpsiproduct(prod);
		updateCommand();
	}

	/* This method is called when button 1 is pressed */
	private boolean confirmAction() {
		FShowList fConfirmAction = new FShowList(ConfigedMain.getMainFrame(),
				Globals.APPNAME + " "
						+ Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"),
				true, new String[] { Configed.getResourceValue("buttonCANCEL"), Configed.getResourceValue("buttonOK") },
				400, 200);

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
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		GroupLayout uninstallPanelLayout = new GroupLayout(uninstallPanel);
		uninstallPanel.setLayout(uninstallPanelLayout);
		uninstallPanelLayout.setHorizontalGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jLabelUninstall, pref, pref, max).addGap(Globals.GAP_SIZE * 2)
				.addGroup(uninstallPanelLayout
						.createSequentialGroup().addGroup(uninstallPanelLayout.createParallelGroup()

								.addGroup(uninstallPanelLayout.createSequentialGroup().addGap(5, 10, 20)
										.addComponent(jComboBoxOpsiProducts, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												2 * Globals.BUTTON_WIDTH)
										.addGap(5, 5, 5)

										.addGap(5, 10, 20))
								.addGroup(uninstallPanelLayout.createSequentialGroup()
										.addComponent(jLabelOn, pref, pref, pref).addGap(5, 10, 10)
										.addComponent(jButtonDepotSelection, pref, pref, pref))
								.addComponent(jLabelVerbosity, pref, pref, pref)
								.addComponent(jLabelKeepFiles, pref, pref, pref))
						.addGap(Globals.GAP_SIZE)
						.addGroup(uninstallPanelLayout.createParallelGroup(leading)
								.addComponent(textFieldProduct, pref, pref, max)
								.addComponent(textFieldSelectedDepots, pref, pref, max)
								.addComponent(jComboBoxVerbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)

								.addComponent(checkBoxKeepFiles, pref, pref, pref)

						)

				)

		);

		uninstallPanelLayout.setVerticalGroup(uninstallPanelLayout.createSequentialGroup().addComponent(jLabelUninstall)
				.addGap(Globals.GAP_SIZE)

				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxOpsiProducts, pref, pref, pref)

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
