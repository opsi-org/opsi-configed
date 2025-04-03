/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productaction;

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.connectx.SmbConnect;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.NameProducer;
import de.uib.utils.logging.Logging;

public class CompleteWinProductsDialog implements NameProducer {
	// file name conventions

	private String winProduct = "";
	private String selectedDepot;
	private Set<String> depots = new HashSet<>();

	private String depotProductDirectory;
	private boolean smbMounted;

	private JComboBox<String> comboChooseDepot;
	private JComboBox<String> comboChooseWinProduct;
	private JTextField fieldTargetPath;

	private JButton buttonCallSelectFolderWinPE;
	private JButton buttonCallSelectFolderInstallFiles;
	private JTextField fieldProductKey;

	private JTextField fieldPathWinPE;
	private JTextField fieldPathInstallFiles;

	private PanelMountShare panelMountShare;

	private JButton buttonCallExecute;

	private JFileChooser chooserFolder;

	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public CompleteWinProductsDialog() {
		defineChoosers();
		initComponentsForNameProducer();

		selectedDepot = "" + comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);

		panelMountShare = new PanelMountShare(this) {
			@Override
			protected boolean checkConnectionToShare() {
				boolean connected = super.checkConnectionToShare();
				if (comboChooseWinProduct != null && connected) {
					evaluateWinProducts();
				}

				return connected;
			}
		};

		initComponents();
		smbMounted = new File(depotProductDirectory).exists();
		panelMountShare.mount(smbMounted);

		evaluateWinProducts();

		JPanel panel = initLayout();

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { buttonCallExecute, Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("FProductAction.title"));
		dialog.setModal(false);

		persistenceController.registerPanelCompleteWinProducts(this);
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	public void evaluateWinProducts() {
		retrieveWinProducts();

		winProduct = (String) comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}

	private void retrieveWinProducts() {
		if (depotProductDirectory == null) {
			return;
		}

		// not yet a depot selected

		smbMounted = new File(depotProductDirectory).exists();

		List<String> winProducts = persistenceController.getProductDataService().getWinProducts(depotProductDirectory);

		comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
	}

	private void defineChoosers() {
		chooserFolder = new JFileChooser();
		chooserFolder.setFileHidingEnabled(false);
		chooserFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		SwingUtilities.updateComponentTreeUI(chooserFolder);

		chooserFolder.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserFolder.setDialogTitle(Configed.getResourceValue("CompleteWinProducts.chooser"));

		comboChooseDepot = new JComboBox<>();

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(
				persistenceController.getHostInfoCollections().getDepotNamesList().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener((ActionEvent actionEvent) -> {
			selectedDepot = "" + comboChooseDepot.getSelectedItem();
			Logging.info(this, "actionPerformed  depot selected ", selectedDepot);
			depots.clear();
			depots.add(selectedDepot);
			SmbConnect.buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);
			evaluateWinProducts();
		});

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.addActionListener((ActionEvent actionEvent) -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			produceTarget();
		});
	}

	private void checkButtonCallExecute() {
		if (buttonCallExecute == null) {
			return;
		}

		buttonCallExecute.setEnabled(new File(fieldTargetPath.getText()).isDirectory());
	}

	private void produceTarget() {
		if (fieldTargetPath != null) {
			fieldTargetPath.setText(depotProductDirectory + File.separator + winProduct);
			checkButtonCallExecute();
		}
	}

	// implements NameProducer
	@Override
	public String produceName() {
		Logging.info(this, "produceName ? fieldTargetPath , depotProductDirectory ", fieldTargetPath, " , ",
				depotProductDirectory);
		if (fieldTargetPath == null || fieldTargetPath.getText().isEmpty()
				|| fieldTargetPath.getText().startsWith(depotProductDirectory)) {
			return depotProductDirectory;
		}

		return fieldTargetPath.getText();
	}

	@Override
	public String getDefaultName() {
		return SmbConnect.PRODUCT_SHARE_RW;
	}

	private void initComponentsForNameProducer() {
		fieldTargetPath = new JTextField();
		fieldTargetPath.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}
		});

		fieldPathWinPE = new JTextField();
	}

	private void initComponents() {
		fieldProductKey = new JTextField();

		buttonCallSelectFolderWinPE = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectFolderWinPE.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(dialog);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathWinPE = chooserFolder.getSelectedFile().getPath();
				fieldPathWinPE.setText(pathWinPE);
				fieldPathWinPE.setCaretPosition(pathWinPE.length());
			} else {
				fieldPathWinPE.setText("");
			}
		});

		buttonCallSelectFolderInstallFiles = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectFolderInstallFiles
				.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderInstallFiles"));

		fieldPathInstallFiles = new JTextField();

		buttonCallSelectFolderInstallFiles.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(dialog);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserFolder.getSelectedFile().getPath();
				fieldPathInstallFiles.setText(pathInstallFiles);
				fieldPathInstallFiles.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldPathInstallFiles.setText("");
			}
		});

		buttonCallExecute = new JButton(Configed.getResourceValue("CompleteWinProducts.execute"));

		buttonCallExecute.setEnabled(false);

		buttonCallExecute.addActionListener(actionEvent -> execute());
	}

	private void execute() {
		dialog.setCursor(Globals.WAIT_CURSOR);

		try {
			File targetDirectory = null;

			String pathWinPE = fieldPathWinPE.getText().trim();
			Logging.debug(this, "copy  ", pathWinPE, " to ", targetDirectory);

			if (!pathWinPE.isEmpty()) {
				targetDirectory = new File(fieldTargetPath.getText() + File.separator + SmbConnect.DIRECTORY_PE);
				FileUtils.copyDirectory(new File(pathWinPE), targetDirectory);
			}

			String pathInstallFiles = fieldPathInstallFiles.getText().trim();
			Logging.debug(this, "copy  ", pathInstallFiles, " to ", targetDirectory);
			if (!pathInstallFiles.isEmpty()) {
				targetDirectory = new File(
						fieldTargetPath.getText() + File.separator + SmbConnect.DIRECTORY_INSTALL_FILES);
				FileUtils.copyDirectory(new File(pathInstallFiles), targetDirectory);
			}

			persistenceController.getRPCMethodExecutor()
					.setRights("/" + SmbConnect.unixPath(SmbConnect.directoryProducts.toArray(String[]::new)) + "/"
							+ winProduct + "/" + SmbConnect.DIRECTORY_PE);
			persistenceController.getRPCMethodExecutor()
					.setRights("/" + SmbConnect.unixPath(SmbConnect.directoryProducts.toArray(String[]::new)) + "/"
							+ winProduct + "/" + SmbConnect.DIRECTORY_INSTALL_FILES);
			dialog.setCursor(null);

			JOptionPane.showMessageDialog(dialog, "Ready", Configed.getResourceValue("CompleteWinProduct.reportTitle"),
					JOptionPane.INFORMATION_MESSAGE);

			List<String> values = new ArrayList<>();

			String productKey = fieldProductKey.getText().trim();
			values.add(productKey);

			// check if product key is new and should be changed
			Map<String, Object> propsMap = persistenceController.getProductDataService().getProductPropertiesPD(
					persistenceController.getHostInfoCollections().getConfigServer(), winProduct);
			Logging.debug(this, " getProductproperties ", propsMap);

			String oldProductKey = null;

			if (mapContainsProductKey(propsMap)) {
				oldProductKey = (String) ((List<?>) propsMap.get("productkey")).get(0);
			} else {
				oldProductKey = "";
			}

			depots.clear();
			depots.add((String) comboChooseDepot.getSelectedItem());

			if (!oldProductKey.equals(productKey)) {
				int returnedOption = JOptionPane.showConfirmDialog(dialog,
						Configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
						Configed.getResourceValue("CompleteWinProducts.questionSetProductKey"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (returnedOption == JOptionPane.YES_OPTION) {
					dialog.setCursor(Globals.WAIT_CURSOR);
					Logging.info(this, "setCommonProductPropertyValue ", depots, ", ", winProduct, ", ", values);
					persistenceController.getProductDataService().setCommonProductPropertyValue(depots, winProduct,
							"productkey", values);

					dialog.setCursor(null);
				}
			}
		} catch (IOException ex) {
			dialog.setCursor(null);
			Logging.error(ex, "copy error:\n", ex);
		} catch (HeadlessException ex) {
			dialog.setCursor(null);
			Logging.error(ex, "Headless exception when invoking showOptionDialog");
		}
	}

	private static boolean mapContainsProductKey(Map<String, Object> propsMap) {
		if (propsMap == null || !(propsMap.get("productkey") instanceof List)) {
			return false;
		} else {
			return !((List<?>) propsMap.get("productkey")).isEmpty()
					&& !"".equals(((List<?>) propsMap.get("productkey")).get(0));
		}
	}

	private JPanel initLayout() {
		JLabel topicLabel = new JLabel(Configed.getResourceValue("CompleteWinProducts.topic"));
		topicLabel.setFont(topicLabel.getFont().deriveFont(Font.BOLD));

		JLabel labelServer = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelServer"));
		labelServer.setFont(labelServer.getFont().deriveFont(Font.BOLD));

		JLabel labelWinProduct = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelWinProduct"));
		labelWinProduct.setFont(labelWinProduct.getFont().deriveFont(Font.BOLD));

		JLabel labelFolderWinPE = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelFolderWinPE"));
		labelFolderWinPE.setFont(labelFolderWinPE.getFont().deriveFont(Font.BOLD));

		JLabel labelFolderInstallFiles = new JLabel(
				Configed.getResourceValue("CompleteWinProducts.labelFolderInstallFiles"));
		labelFolderInstallFiles.setFont(labelFolderInstallFiles.getFont().deriveFont(Font.BOLD));

		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		labelTargetPath.setFont(labelTargetPath.getFont().deriveFont(Font.BOLD));

		JLabel labelProductKey = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelProductKey"));
		labelProductKey.setFont(labelProductKey.getFont().deriveFont(Font.BOLD));

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboChooseDepot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboChooseWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelFolderWinPE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(fieldPathWinPE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCallSelectFolderWinPE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelFolderInstallFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(fieldPathInstallFiles, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCallSelectFolderInstallFiles, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelProductKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldProductKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(labelServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboChooseDepot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(labelWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboChooseWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(labelFolderWinPE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(fieldPathWinPE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonCallSelectFolderWinPE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				.addComponent(labelFolderInstallFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(fieldPathInstallFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonCallSelectFolderInstallFiles,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(labelTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(fieldTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(labelProductKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(fieldProductKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE));

		return panel;
	}
}
