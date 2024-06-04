/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productaction;

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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import de.uib.utils.NameProducer;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SecondaryFrame;

public class FCompleteWinProducts extends SecondaryFrame implements NameProducer {
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

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public FCompleteWinProducts(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		super.setIconImage(Utils.getMainIcon());
		super.setTitle(Configed.getResourceValue("FProductAction.title"));

		defineChoosers();
		initComponentsForNameProducer();

		selectedDepot = "" + comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);

		panelMountShare = new PanelMountShare(this, this) {
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

		defineLayout();

		persistenceController.registerPanelCompleteWinProducts(this);
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
		chooserFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserFolder.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserFolder);

		chooserFolder.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserFolder.setDialogTitle(Configed.getResourceValue("CompleteWinProducts.chooser"));

		comboChooseDepot = new JComboBox<>();
		comboChooseDepot.setSize(Globals.TEXT_FIELD_DIMENSION);

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(configedMain.getLinkedDepots().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener((ActionEvent actionEvent) -> {
			selectedDepot = "" + comboChooseDepot.getSelectedItem();
			Logging.info(this, "actionPerformed  depot selected " + selectedDepot);
			depots.clear();
			depots.add(selectedDepot);
			SmbConnect.buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);
			evaluateWinProducts();
		});

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.setSize(Globals.TEXT_FIELD_DIMENSION);
		comboChooseWinProduct.addActionListener((ActionEvent actionEvent) -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			produceTarget();
		});
	}

	private void checkButtonCallExecute() {
		if (buttonCallExecute == null) {
			return;
		}

		buttonCallExecute.setEnabled(
				// true
				new File(fieldTargetPath.getText()).isDirectory());
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
		Logging.info(this, "produceName ? fieldTargetPath , depotProductDirectory " + fieldTargetPath + " , "
				+ depotProductDirectory);
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
		fieldProductKey.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		buttonCallSelectFolderWinPE = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallSelectFolderWinPE.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(getContentPane());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathWinPE = chooserFolder.getSelectedFile().getPath();
				fieldPathWinPE.setText(pathWinPE);
				fieldPathWinPE.setCaretPosition(pathWinPE.length());
			} else {
				fieldPathWinPE.setText("");
			}
		});

		buttonCallSelectFolderInstallFiles = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallSelectFolderInstallFiles
				.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderInstallFiles"));

		fieldPathInstallFiles = new JTextField();

		buttonCallSelectFolderInstallFiles.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(getContentPane());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserFolder.getSelectedFile().getPath();
				fieldPathInstallFiles.setText(pathInstallFiles);
				fieldPathInstallFiles.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldPathInstallFiles.setText("");
			}
		});

		buttonCallExecute = new JButton(Utils.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setSelectedIcon(Utils.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallExecute.setToolTipText(Configed.getResourceValue("CompleteWinProducts.execute"));

		buttonCallExecute.setEnabled(false);

		buttonCallExecute.addActionListener((ActionEvent e) -> {
			Logging.debug(this,
					"actionPerformed on buttonCallExecute pathWinPE, pathInstallFiles, productKey, winproduct "
							+ fieldPathWinPE.getText() + ", " + fieldPathInstallFiles.getText() + ", "
							+ fieldProductKey.getText() + ", " + comboChooseWinProduct.getSelectedItem());
			execute();
		});
	}

	private void execute() {
		activateLoadingCursor();

		try {
			File targetDirectory = null;

			String pathWinPE = fieldPathWinPE.getText().trim();
			Logging.debug(this, "copy  " + pathWinPE + " to " + targetDirectory);

			if (!pathWinPE.isEmpty()) {
				targetDirectory = new File(fieldTargetPath.getText() + File.separator + SmbConnect.DIRECTORY_PE);
				FileUtils.copyDirectory(new File(pathWinPE), targetDirectory);
			}

			String pathInstallFiles = fieldPathInstallFiles.getText().trim();
			Logging.debug(this, "copy  " + pathInstallFiles + " to " + targetDirectory);
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
			deactivateLoadingCursor();

			JOptionPane.showMessageDialog(this, "Ready", Configed.getResourceValue("CompleteWinProduct.reportTitle"),
					JOptionPane.INFORMATION_MESSAGE);

			List<String> values = new ArrayList<>();

			String productKey = fieldProductKey.getText().trim();
			values.add(productKey);

			// check if product key is new and should be changed
			Map<String, Object> propsMap = persistenceController.getProductDataService().getProductPropertiesPD(
					persistenceController.getHostInfoCollections().getConfigServer(), winProduct);
			Logging.debug(this, " getProductproperties " + propsMap);

			String oldProductKey = null;

			if (mapContainsProductKey(propsMap)) {
				oldProductKey = (String) ((List<?>) propsMap.get("productkey")).get(0);
			} else {
				oldProductKey = "";
			}
			long start = System.nanoTime();
			Logging.devel("" + (System.nanoTime() - start));

			depots.clear();
			depots.add((String) comboChooseDepot.getSelectedItem());

			if (!oldProductKey.equals(productKey)) {
				int returnedOption = JOptionPane.showConfirmDialog(this,
						Configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
						Configed.getResourceValue("CompleteWinProducts.questionSetProductKey"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (returnedOption == JOptionPane.YES_OPTION) {
					activateLoadingCursor();
					Logging.info(this, "setCommonProductPropertyValue " + depots + ", " + winProduct + ", " + values);
					persistenceController.getProductDataService().setCommonProductPropertyValue(depots, winProduct,
							"productkey", values);

					deactivateLoadingCursor();
				}
			}
		} catch (IOException ex) {
			deactivateLoadingCursor();
			Logging.error("copy error:\n" + ex, ex);
		} catch (HeadlessException ex) {
			deactivateLoadingCursor();
			Logging.error("Headless exception when invoking showOptionDialog", ex);
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

	private void defineLayout() {
		JLabel topicLabel = new JLabel(Configed.getResourceValue("CompleteWinProducts.topic"));

		JLabel labelServer = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelServer"));
		JLabel labelWinProduct = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelWinProduct"));
		JLabel labelFolderWinPE = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelFolderWinPE"));
		JLabel labelFolderInstallFiles = new JLabel(
				Configed.getResourceValue("CompleteWinProducts.labelFolderInstallFiles"));
		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		JLabel labelProductKey = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelProductKey"));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		int hFirstGap = Globals.HFIRST_GAP;

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 4)
				.addComponent(
						topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE * 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelServer, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(comboChooseWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectFolderWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldPathWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectFolderInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldPathInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(panelMountShare, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelProductKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldProductKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonCallExecute,
						Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE * 2));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelServer, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addGap(Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelWinProduct, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addGap(Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(comboChooseWinProduct, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelFolderWinPE, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addComponent(buttonCallSelectFolderWinPE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldPathWinPE, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelFolderInstallFiles, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addComponent(buttonCallSelectFolderInstallFiles, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldPathInstallFiles, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelTargetPath, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addGap(Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldTargetPath, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelProductKey, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addGap(Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldProductKey, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addGap(0, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH)
						.addGap(0, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addGap(0, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addGap(0, hFirstGap, hFirstGap).addGap(0, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
						.addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(hFirstGap, hFirstGap, Short.MAX_VALUE)));
	}
}
