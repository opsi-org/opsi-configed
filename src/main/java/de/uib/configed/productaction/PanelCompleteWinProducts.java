/* 
 * PanelCompleteWinProducts
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014,2017 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.configed.productaction;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.connectx.SmbConnect;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitCursor;

public class PanelCompleteWinProducts extends JPanel
		implements de.uib.utilities.observer.DataRefreshedObserver, NameProducer

{

	// ============== file name conventions

	String winProduct = "";
	String server = "";
	String selectedDepot = null;
	Set<String> depots = new HashSet<String>();
	// String standardProductDirectory;
	String depotProductDirectory;
	boolean smbMounted;

	// ==============

	int firstLabelWidth = Globals.firstLabelWidth;

	JComboBox comboChooseDepot;
	JComboBox comboChooseWinProduct;
	JTextField fieldTargetPath;

	JButton buttonCallSelectFolderWinPE;
	JButton buttonCallSelectFolderInstallFiles;
	JTextField fieldProductKey;

	JTextField fieldPathWinPE;
	JTextField fieldPathInstallFiles;

	PanelMountShare panelMountShare;

	JButton buttonCallExecute;
	JTextField fieldResult;

	JFileChooser chooserFolder;

	PersistenceController persist;
	ConfigedMain main;
	JFrame rootFrame;

	ArrayList<String> winProducts;

	public PanelCompleteWinProducts(ConfigedMain main, PersistenceController persist, JFrame root) {
		this.main = main;
		this.persist = persist;
		this.rootFrame = root;
		server = main.getConfigserver();

		defineChoosers();
		initComponentsForNameProducer();

		selectedDepot = "" + comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.getInstance().buildSambaTarget(selectedDepot,
				de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW);

		panelMountShare = new PanelMountShare((NameProducer) this, main, root)

		{
			@Override
			protected boolean checkConnectionToShare() {
				boolean connected = super.checkConnectionToShare();
				if (comboChooseWinProduct != null // we have an initialized gui
						&& connected) {
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

		// evaluateWinProducts();

		persist.registerDataRefreshedObserver(this);

	}

	private void evaluateWinProducts() {
		retrieveWinProducts();

		winProduct = "" + comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}

	// implementation of DataRefreshedObserver
	public void gotNotification(Object mesg) {
		evaluateWinProducts();
	}

	private void retrieveWinProducts() {
		if (depotProductDirectory == null)
			return;

		// not yet a depot selected

		smbMounted = new File(depotProductDirectory).exists();

		ArrayList<String> winProducts = persist.getWinProducts(server, depotProductDirectory);

		comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
	}

	private void defineChoosers() {
		chooserFolder = new JFileChooser();
		chooserFolder.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		chooserFolder.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		chooserFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserFolder.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserFolder);
		// chooserFolder.setControlButtonsAreShown(false);

		chooserFolder.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserFolder.setDialogTitle(Globals.APPNAME + " " + configed.getResourceValue("CompleteWinProducts.chooser"));

		comboChooseDepot = new JComboBox();
		comboChooseDepot.setSize(Globals.textfieldDimension);

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(main.getLinkedDepots().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedDepot = "" + comboChooseDepot.getSelectedItem();
				logging.info(this, "actionPerformed  depot selected " + selectedDepot);
				depots.clear();
				depots.add(selectedDepot);
				SmbConnect.getInstance().buildSambaTarget(selectedDepot, de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW);
				evaluateWinProducts();
			}
		});

		comboChooseWinProduct = new JComboBox();
		comboChooseWinProduct.setSize(Globals.textfieldDimension);
		comboChooseWinProduct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				winProduct = "" + comboChooseWinProduct.getSelectedItem();
				produceTarget();
			}
		});

		// logging.debug(this, "defineChoosers, depots: " + persist.getWinProducts());

	}

	private void checkButtonCallExecute() {
		if (buttonCallExecute == null)
			return;

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

	// =======
	// implements NameProducer
	public String produceName() {
		logging.info(this, "produceName ? fieldTargetPath , depotProductDirectory " + fieldTargetPath + " , "
				+ depotProductDirectory);
		if (fieldTargetPath == null || fieldTargetPath.getText().equals("")
				|| fieldTargetPath.getText().startsWith(depotProductDirectory))
			return depotProductDirectory;

		return fieldTargetPath.getText();
	}

	public String getDefaultName() {
		return de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW;
	}

	// =======

	private void initComponentsForNameProducer() {
		fieldTargetPath = new JTextField("");
		fieldTargetPath.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}

			public void insertUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}

			public void removeUpdate(DocumentEvent e) {
				checkButtonCallExecute();
			}
		});

		fieldPathWinPE = new JTextField();
	}

	private void initComponents() {

		// fieldServerPath = new JTextField("");

		final JPanel panel = this;

		fieldProductKey = new JTextField("");
		fieldProductKey.setPreferredSize(Globals.textfieldDimension);

		// fieldTargetPath.setForeground(Globals.greyed);

		buttonCallSelectFolderWinPE = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderWinPE.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectFolderWinPE.setToolTipText(configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int returnVal = chooserFolder.showOpenDialog(panel);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String pathWinPE = chooserFolder.getSelectedFile().getPath();
					fieldPathWinPE.setText(pathWinPE);
					fieldPathWinPE.setCaretPosition(pathWinPE.length());
				} else {
					fieldPathWinPE.setText("");
				}

			}
		});

		buttonCallSelectFolderInstallFiles = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectFolderInstallFiles.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectFolderInstallFiles
				.setToolTipText(configed.getResourceValue("CompleteWinProducts.chooserFolderInstallFiles"));

		fieldPathInstallFiles = new JTextField();

		buttonCallSelectFolderInstallFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int returnVal = chooserFolder.showOpenDialog(panel);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String pathInstallFiles = chooserFolder.getSelectedFile().getPath();
					fieldPathInstallFiles.setText(pathInstallFiles);
					fieldPathInstallFiles.setCaretPosition(pathInstallFiles.length());
				} else {
					fieldPathInstallFiles.setText("");
				}

			}
		});

		buttonCallExecute = new JButton("", Globals.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setSelectedIcon(Globals.createImageIcon("images/upload2product.png", ""));
		buttonCallExecute.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallExecute.setToolTipText(configed.getResourceValue("CompleteWinProducts.execute"));

		buttonCallExecute.setEnabled(false);

		buttonCallExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.debug(this,
						"actionPerformed on buttonCallExecute pathWinPE, pathInstallFiles, productKey, winproduct "
								+ fieldPathWinPE.getText() + ", " + fieldPathInstallFiles.getText() + ", "
								+ fieldProductKey.getText() + ", " + comboChooseWinProduct.getSelectedItem());

				final Color saveColor = buttonCallExecute.getBackground();
				// final Icon saveIcon = buttonCallExecute.getIcon();
				buttonCallExecute.setBackground(Globals.failedBackColor);

				execute();

				buttonCallExecute.setBackground(saveColor);
			}
		});

	}

	protected void execute() {

		WaitCursor waitCursor = null;

		try {

			waitCursor = new WaitCursor(rootFrame);

			File targetDirectory = null;

			String pathWinPE = fieldPathWinPE.getText().trim();
			logging.debug(this, "copy  " + pathWinPE + " to " + targetDirectory);

			if (!pathWinPE.equals("")) {
				targetDirectory = new File(fieldTargetPath.getText() + File.separator + SmbConnect.directoryPE);
				FileUtils.copyDirectory(new File(pathWinPE), targetDirectory);
			}

			String pathInstallFiles = fieldPathInstallFiles.getText().trim();
			logging.debug(this, "copy  " + pathInstallFiles + " to " + targetDirectory);
			if (!pathInstallFiles.equals("")) {
				targetDirectory = new File(
						fieldTargetPath.getText() + File.separator + SmbConnect.directoryInstallFiles);
				FileUtils.copyDirectory(new File(pathInstallFiles), targetDirectory);
			}

			persist.setRights("/" + SmbConnect.unixPath(SmbConnect.directoryProducts) + "/" + winProduct + "/"
					+ SmbConnect.directoryPE);
			persist.setRights("/" + SmbConnect.unixPath(de.uib.connectx.SmbConnect.directoryProducts) + "/" + winProduct
					+ "/" + SmbConnect.directoryInstallFiles);
			waitCursor.stop();

			JOptionPane.showMessageDialog(rootFrame, "Ready", // resultMessage,
					configed.getResourceValue("CompleteWinProduct.reportTitle"), JOptionPane.INFORMATION_MESSAGE);

			java.util.List<String> values = new ArrayList<String>();

			String productKey = fieldProductKey.getText().trim();
			values.add(productKey);

			// check if product key is new and should be changed
			Map<String, Object> propsMap = persist.getProductproperties(server, winProduct);
			logging.debug(this, " getProductproperties " + propsMap);

			String oldProductKey = null;

			if (propsMap != null && propsMap.get("productkey") != null
					&& propsMap.get("productkey") instanceof java.util.List
					&& (((java.util.List) propsMap.get("productkey")).size() > 0)
					&& !(((java.util.List) propsMap.get("productkey")).get(0).equals("")))
				oldProductKey = (String) ((java.util.List) propsMap.get("productkey")).get(0);

			if (oldProductKey == null)
				oldProductKey = "";

			depots.clear();
			depots.add((String) comboChooseDepot.getSelectedItem());

			// logging.info(this, "setCommonProductPropertyValue " + depots + ", " +
			// winProduct + ", " + values);

			if (!oldProductKey.equals(productKey)) {

				int returnedOption = JOptionPane.NO_OPTION;
				returnedOption = JOptionPane.showOptionDialog(rootFrame,
						configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
						configed.getResourceValue("CompleteWinProducts.questionSetProductKey"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

				if (returnedOption == JOptionPane.YES_OPTION) {
					waitCursor = new WaitCursor(rootFrame);
					logging.info(this, "setCommonProductPropertyValue " + depots + ", " + winProduct + ", " + values);
					persist.setCommonProductPropertyValue(depots, winProduct, "productkey", values);

					waitCursor.stop();
				}
			}

		} catch (Exception ex) {
			waitCursor.stop();
			logging.error("copy error:\n" + ex, ex);
		}

		waitCursor = null;
	}

	public void defineLayout() {
		setBorder(Globals.createPanelBorder());
		JLabel topicLabel = new JLabel(configed.getResourceValue("CompleteWinProducts.topic"));

		JLabel labelServer = new JLabel(configed.getResourceValue("CompleteWinProducts.labelServer"));
		JLabel labelWinProduct = new JLabel(configed.getResourceValue("CompleteWinProducts.labelWinProduct"));
		JLabel labelFolderWinPE = new JLabel(configed.getResourceValue("CompleteWinProducts.labelFolderWinPE"));
		JLabel labelFolderInstallFiles = new JLabel(
				configed.getResourceValue("CompleteWinProducts.labelFolderInstallFiles"));
		JLabel labelTargetPath = new JLabel(configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		JLabel labelProductKey = new JLabel(configed.getResourceValue("CompleteWinProducts.labelProductKey"));

		JPanel panel = this;
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		int hFirstGap = Globals.hFirstGap;

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize * 3, Globals.vGapSize * 4)
				.addComponent(
						topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelServer, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(comboChooseDepot, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelWinProduct, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight)
								.addComponent(comboChooseWinProduct, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderWinPE, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(buttonCallSelectFolderWinPE, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight)
						.addComponent(fieldPathWinPE, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFolderInstallFiles, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight)
						.addComponent(buttonCallSelectFolderInstallFiles, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight)
						.addComponent(fieldPathInstallFiles, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(panelMountShare,
						Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelTargetPath, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(fieldTargetPath, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelProductKey, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(fieldProductKey, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonCallExecute,
						Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize * 2, Short.MAX_VALUE)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 2, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelServer, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(comboChooseDepot, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Globals.buttonWidth * 2)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelWinProduct, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(comboChooseWinProduct, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Globals.buttonWidth * 2)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelFolderWinPE, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(buttonCallSelectFolderWinPE, Globals.graphicButtonWidth,
								Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldPathWinPE, Globals.buttonWidth * 2, Globals.buttonWidth * 2, Short.MAX_VALUE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelFolderInstallFiles, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(buttonCallSelectFolderInstallFiles, Globals.graphicButtonWidth,
								Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldPathInstallFiles, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelTargetPath, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldTargetPath, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(labelProductKey, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldProductKey, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Globals.buttonWidth * 2)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addGap(0, firstLabelWidth, firstLabelWidth).addGap(0, Globals.hGapSize, Globals.hGapSize)
						.addGap(0, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(0, hFirstGap, hFirstGap).addGap(0, Globals.buttonWidth * 2, Short.MAX_VALUE)
						.addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(hFirstGap, hFirstGap, Short.MAX_VALUE))

		);
	}

}
