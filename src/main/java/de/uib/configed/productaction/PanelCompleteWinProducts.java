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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.connectx.SmbConnect;
import de.uib.opsidatamodel.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class PanelCompleteWinProducts extends JPanel implements NameProducer {

	// file name conventions

	private String winProduct = "";
	private String server = "";
	private String selectedDepot;
	private Set<String> depots = new HashSet<>();

	private String depotProductDirectory;
	private boolean smbMounted;

	private int firstLabelWidth = Globals.FIRST_LABEL_WIDTH;

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
	private SecondaryFrame rootFrame;

	public PanelCompleteWinProducts(ConfigedMain main, SecondaryFrame rootFrame) {
		this.configedMain = main;
		this.rootFrame = rootFrame;
		server = main.getConfigserver();

		defineChoosers();
		initComponentsForNameProducer();

		selectedDepot = "" + comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.getInstance().buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);

		panelMountShare = new PanelMountShare(this, rootFrame) {
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

		List<String> winProducts = persistenceController.getWinProducts(server, depotProductDirectory);

		comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
	}

	private void defineChoosers() {
		chooserFolder = new JFileChooser();
		chooserFolder.setPreferredSize(Globals.FILE_CHOOSER_SIZE);
		chooserFolder.setPreferredSize(Globals.FILE_CHOOSER_SIZE);
		chooserFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserFolder.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserFolder);

		chooserFolder.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserFolder.setDialogTitle(Globals.APPNAME + " " + Configed.getResourceValue("CompleteWinProducts.chooser"));

		comboChooseDepot = new JComboBox<>();
		comboChooseDepot.setSize(Globals.TEXT_FIELD_DIMENSION);

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(configedMain.getLinkedDepots().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener((ActionEvent actionEvent) -> {
			selectedDepot = "" + comboChooseDepot.getSelectedItem();
			Logging.info(this, "actionPerformed  depot selected " + selectedDepot);
			depots.clear();
			depots.add(selectedDepot);
			SmbConnect.getInstance().buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);
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
		fieldTargetPath = new JTextField("");
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

		final JPanel panel = this;

		fieldProductKey = new JTextField("");
		fieldProductKey.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		buttonCallSelectFolderWinPE = new JButton("", Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectFolderWinPE.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener((ActionEvent actionEvent) -> {

			int returnVal = chooserFolder.showOpenDialog(panel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathWinPE = chooserFolder.getSelectedFile().getPath();
				fieldPathWinPE.setText(pathWinPE);
				fieldPathWinPE.setCaretPosition(pathWinPE.length());
			} else {
				fieldPathWinPE.setText("");
			}
		});

		buttonCallSelectFolderInstallFiles = new JButton("", Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectFolderInstallFiles
				.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderInstallFiles"));

		fieldPathInstallFiles = new JTextField();

		buttonCallSelectFolderInstallFiles.addActionListener((ActionEvent actionEvent) -> {

			int returnVal = chooserFolder.showOpenDialog(panel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserFolder.getSelectedFile().getPath();
				fieldPathInstallFiles.setText(pathInstallFiles);
				fieldPathInstallFiles.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldPathInstallFiles.setText("");
			}
		});

		buttonCallExecute = new JButton("", Utils.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setSelectedIcon(Utils.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallExecute.setToolTipText(Configed.getResourceValue("CompleteWinProducts.execute"));

		buttonCallExecute.setEnabled(false);

		buttonCallExecute.addActionListener((ActionEvent e) -> {
			Logging.debug(this,
					"actionPerformed on buttonCallExecute pathWinPE, pathInstallFiles, productKey, winproduct "
							+ fieldPathWinPE.getText() + ", " + fieldPathInstallFiles.getText() + ", "
							+ fieldProductKey.getText() + ", " + comboChooseWinProduct.getSelectedItem());

			if (!Main.THEMES) {
				buttonCallExecute.setBackground(Globals.FAILED_BACKGROUND_COLOR);
			}

			execute();

			if (!Main.THEMES) {
				buttonCallExecute.setBackground(buttonCallExecute.getBackground());
			}
		});

	}

	private void execute() {

		rootFrame.activateLoadingCursor();

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

			persistenceController.setRights("/" + SmbConnect.unixPath(SmbConnect.directoryProducts) + "/" + winProduct
					+ "/" + SmbConnect.DIRECTORY_PE);
			persistenceController.setRights("/" + SmbConnect.unixPath(SmbConnect.directoryProducts) + "/" + winProduct
					+ "/" + SmbConnect.DIRECTORY_INSTALL_FILES);
			rootFrame.disactivateLoadingCursor();

			JOptionPane.showMessageDialog(rootFrame, "Ready", // resultMessage,
					Configed.getResourceValue("CompleteWinProduct.reportTitle"), JOptionPane.INFORMATION_MESSAGE);

			List<String> values = new ArrayList<>();

			String productKey = fieldProductKey.getText().trim();
			values.add(productKey);

			// check if product key is new and should be changed
			Map<String, Object> propsMap = persistenceController.getProductProperties(server, winProduct);
			Logging.debug(this, " getProductproperties " + propsMap);

			String oldProductKey = null;

			if (mapContainsProductKey(propsMap)) {
				oldProductKey = (String) ((List<?>) propsMap.get("productkey")).get(0);
			} else {
				oldProductKey = "";
			}

			depots.clear();
			depots.add((String) comboChooseDepot.getSelectedItem());

			if (!oldProductKey.equals(productKey)) {

				int returnedOption = JOptionPane.showOptionDialog(rootFrame,
						Configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
						Configed.getResourceValue("CompleteWinProducts.questionSetProductKey"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

				if (returnedOption == JOptionPane.YES_OPTION) {
					rootFrame.activateLoadingCursor();
					Logging.info(this, "setCommonProductPropertyValue " + depots + ", " + winProduct + ", " + values);
					persistenceController.setCommonProductPropertyValue(depots, winProduct, "productkey", values);

					rootFrame.disactivateLoadingCursor();
				}
			}

		} catch (IOException ex) {
			rootFrame.disactivateLoadingCursor();
			Logging.error("copy error:\n" + ex, ex);
		} catch (HeadlessException ex) {
			rootFrame.disactivateLoadingCursor();
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
		setBorder(new LineBorder(Globals.BACKGROUND_COLOR_6, 2, true));
		JLabel topicLabel = new JLabel(Configed.getResourceValue("CompleteWinProducts.topic"));

		JLabel labelServer = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelServer"));
		JLabel labelWinProduct = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelWinProduct"));
		JLabel labelFolderWinPE = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelFolderWinPE"));
		JLabel labelFolderInstallFiles = new JLabel(
				Configed.getResourceValue("CompleteWinProducts.labelFolderInstallFiles"));
		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		JLabel labelProductKey = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelProductKey"));

		JPanel panel = this;
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		int hFirstGap = Globals.HFIRST_GAP;

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE * 3, Globals.VGAP_SIZE * 4)
				.addComponent(
						topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelServer, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(comboChooseWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectFolderWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldPathWinPE, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectFolderInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldPathInstallFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(panelMountShare,
						Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelProductKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldProductKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonCallExecute,
						Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2));

		layout.setHorizontalGroup(
				layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE)
								.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(labelServer, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Globals.BUTTON_WIDTH * 2)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(labelWinProduct, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(comboChooseWinProduct, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Globals.BUTTON_WIDTH * 2)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(labelFolderWinPE, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(buttonCallSelectFolderWinPE, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldPathWinPE, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(
								layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
										.addComponent(labelFolderInstallFiles, firstLabelWidth, firstLabelWidth,
												firstLabelWidth)
										.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
										.addComponent(buttonCallSelectFolderInstallFiles, Globals.GRAPHIC_BUTTON_WIDTH,
												Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(hFirstGap, hFirstGap, hFirstGap)
										.addComponent(fieldPathInstallFiles, Globals.BUTTON_WIDTH * 2,
												Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addComponent(panelMountShare,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(labelTargetPath, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldTargetPath, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(labelProductKey, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldProductKey, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Globals.BUTTON_WIDTH * 2)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(
								layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
										.addGap(0, firstLabelWidth, firstLabelWidth)
										.addGap(0, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
										.addGap(0, Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(0, hFirstGap, hFirstGap)
										.addGap(0, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
										.addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(hFirstGap, hFirstGap, Short.MAX_VALUE))

		);
	}
}
