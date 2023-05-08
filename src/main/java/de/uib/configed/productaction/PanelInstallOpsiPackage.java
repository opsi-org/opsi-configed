/* 
 * PanelInstallOpsiPackage
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014 uib.de
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
import java.util.Map;

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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.thread.WaitCursor;

public class PanelInstallOpsiPackage extends JPanel implements NameProducer {

	private static final String PAGE_SHARE_S = "opsi_workbench";

	private JButton buttonCallChooserPackage;
	private JFileChooser chooserPackage;
	private JComboBox<String> comboChooseDepot;
	private JButton buttonCallExecute;

	private String opsiPackageNameS;

	private JTextField fieldOpsiPackageName;

	private String opsiWorkBenchDirectoryS;
	private File opsiWorkBenchDirectory;
	private String opsiPackageServerPathS;

	private PanelMountShare panelMountShare;

	private final boolean isWindows;

	// server path finding
	private JTextField fieldServerPath;
	private JButton buttonCallChooserServerpath;
	private JFileChooser chooserServerpath;

	private AbstractPersistenceController persist;
	ConfigedMain main;
	private JFrame rootFrame;

	public PanelInstallOpsiPackage(ConfigedMain main, AbstractPersistenceController persist, JFrame root) {
		this.main = main;
		this.persist = persist;
		this.rootFrame = root;

		isWindows = Globals.isWindows();

		initComponents();

		panelMountShare = new PanelMountShare(this, main, root);

		defineLayout();
	}

	private boolean installProductFromWorkbench() {
		WaitCursor waitCursor = null;

		String opsiPackageOnWorkbenchS = null;
		opsiPackageNameS = null;

		try {
			File opsiPackagePathToHandle = new File(fieldOpsiPackageName.getText());

			opsiPackageNameS = opsiPackagePathToHandle.getName();

			opsiWorkBenchDirectoryS = fieldServerPath.getText();

			opsiPackageOnWorkbenchS = opsiWorkBenchDirectoryS + File.separator + opsiPackageNameS;

			Logging.debug(this, "getProductToWorkbench  target " + opsiPackageOnWorkbenchS);

			File opsiPackageOnWorkbench = new File(opsiPackageOnWorkbenchS);

			if (opsiPackageNameS == null || opsiPackageNameS.trim().isEmpty()) {
				return false;
			} else {

				if (opsiPackageOnWorkbench.exists()) {

					int returnedOption = JOptionPane.showOptionDialog(rootFrame,
							Configed.getResourceValue("InstallOpsiPackage.packageReinstall") + " "
									+ opsiWorkBenchDirectoryS + " "
									+ Configed.getResourceValue("InstallOpsiPackage.packageReinstall2"),
							Globals.APPNAME + " "
									+ Configed.getResourceValue("InstallOpsiPackage.packageReinstallTitle"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					return returnedOption == JOptionPane.YES_OPTION;

				} else {
					// it is not there and we have to copy it

					waitCursor = new WaitCursor(rootFrame);

					try {
						opsiWorkBenchDirectory = new File(fieldServerPath.getText());

						// we start at a local directory
					} catch (Exception ex) {
						Logging.info(this, "trying to build file " + opsiWorkBenchDirectoryS + " : " + ex);
					}

					Logging.debug(this,
							"getProductToWorkbench copy " + opsiPackagePathToHandle + ", " + opsiWorkBenchDirectory);
					FileUtils.copyFileToDirectory(opsiPackagePathToHandle, opsiWorkBenchDirectory);
					waitCursor.stop();
					return true;
				}
			}

		} catch (Exception ex) {
			if (waitCursor != null) {
				waitCursor.stop();
			}

			Logging.error("library missing or path problem " + ex, ex);
		}

		return false;

	}

	private void produceServerPath() {
		Logging.debug(this, "produceServerPath ");

		opsiPackageServerPathS = AbstractPersistenceController.packageServerDirectoryS + opsiPackageNameS;
		Logging.debug(this, "produceServerPath " + opsiPackageServerPathS);
	}

	private void buildSambaTarget(String depotserver) {
		Map<String, Map<String, Object>> depot2depotMap = persist.getHostInfoCollections().getDepots();

		Logging.info(this, "buildSambaTarget for depotserver " + depotserver);

		if (depot2depotMap.get(depotserver) == null) {
			return;
		}

		String depotRemoteUrl = (String) depot2depotMap.get(depotserver).get("depotRemoteUrl");

		if (depotRemoteUrl == null) {
			Logging.warning(this, "buildSambaTarget, depotRemoteUrl null");
			return;
		}

		String[] parts = depotRemoteUrl.split("/");
		String netbiosName = "";

		if (parts.length > 2) {
			netbiosName = parts[2];
			Logging.info(this, "buildSambaTarget " + netbiosName);
		} else {
			Logging.warning(this, "buildSambaTarget, no splitting for " + depotRemoteUrl);
		}
	}

	public void execute() {

		if (installProductFromWorkbench()) {
			produceServerPath();
			WaitCursor waitCursor = new WaitCursor(rootFrame);
			persist.setRights(opsiPackageServerPathS);
			boolean result = persist.installPackage(opsiPackageServerPathS);
			waitCursor.stop();

			Logging.info(this, "installPackage wrongly reporesult " + result);

			if (result) {
				JOptionPane.showMessageDialog(rootFrame, "Ready", // resultMessage,
						Configed.getResourceValue("InstallOpsiPackage.reportTitle"), JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private void choosePackage() {

		Logging.debug(this, "buttonCallChooserPackage action  starting ");

		int returnVal = chooserPackage.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String opsiPackageGotPathS = chooserPackage.getSelectedFile().getPath();
			fieldOpsiPackageName.setText(opsiPackageGotPathS);
			fieldOpsiPackageName.setCaretPosition(opsiPackageGotPathS.length());

		}
	}

	private void chooseServerpath() {

		int returnVal = chooserServerpath.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String serverPathGot = chooserServerpath.getSelectedFile().getPath();
			fieldServerPath.setText(serverPathGot);
			fieldServerPath.setCaretPosition(serverPathGot.length());

		}

	}

	private void defineChoosers() {

		comboChooseDepot = new JComboBox<>();
		comboChooseDepot.setSize(Globals.textfieldDimension);

		Logging.debug(this, "defineChoosers, depots: " + persist.getHostInfoCollections().getDepots());

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(main.getLinkedDepots().toArray(new String[0])));

		// as long as we did not implement contacting a different depot
		comboChooseDepot.setEnabled(false);

		chooserPackage = new JFileChooser();
		chooserPackage.setPreferredSize(Globals.filechooserSize);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("opsi package", "opsi");
		chooserPackage.addChoosableFileFilter(filter);
		chooserPackage.setFileFilter(filter);
		chooserPackage.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserPackage);

		chooserPackage.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserPackage.setDialogTitle(Globals.APPNAME + " " + Configed.getResourceValue("InstallOpsiPackage.chooser"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setPreferredSize(Globals.filechooserSize);
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(
				Globals.APPNAME + " " + Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
	}

	// implements NameProducer
	@Override
	public String produceName() {
		return opsiWorkBenchDirectoryS;
	}

	@Override
	public String getDefaultName() {
		return PAGE_SHARE_S;
	}

	private void initComponents() {
		defineChoosers();

		buildSambaTarget("" + comboChooseDepot.getSelectedItem());

		fieldOpsiPackageName = new JTextField();
		fieldOpsiPackageName.setEditable(true);
		fieldOpsiPackageName.setPreferredSize(Globals.textfieldDimension);

		buttonCallChooserPackage = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallChooserPackage.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.chooserPackage"));

		buttonCallChooserPackage.addActionListener(actionEvent -> choosePackage());

		fieldServerPath = new JTextField(opsiWorkBenchDirectoryS);
		if (!ConfigedMain.THEMES) {
			fieldServerPath.setForeground(Globals.greyed);
		}

		fieldServerPath.setPreferredSize(Globals.textfieldDimension);

		buttonCallChooserServerpath = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallChooserServerpath.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));

		buttonCallChooserServerpath.addActionListener(actionEvent -> chooseServerpath());

		buttonCallExecute = new JButton("", Globals.createImageIcon("images/installpackage.png", ""));

		buttonCallExecute.setSelectedIcon(Globals.createImageIcon("images/installpackage.png", ""));
		buttonCallExecute.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallExecute.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.execute"));

		buttonCallExecute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Color saveColor = buttonCallExecute.getBackground();

				if (!ConfigedMain.THEMES) {
					buttonCallExecute.setBackground(Globals.FAILED_BACKGROUND_COLOR);
				}

				Logging.info(this, "actionPerformed on buttonCallExecute opsiPackageGotPathS,  depot:  "
						+ fieldOpsiPackageName.getText() + ", " + comboChooseDepot.getSelectedItem());

				execute();

				if (!ConfigedMain.THEMES) {
					buttonCallExecute.setBackground(saveColor);
				}
			}
		});
	}

	private void defineLayout() {
		setBorder(Globals.createPanelBorder());

		JLabel topicLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.topic"));
		JLabel infoLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.info"));

		JLabel serverLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.chooseDepot"));

		JLabel serverPathLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.serverpath"));

		if (isWindows) {
			serverPathLabel.setForeground(Globals.greyed);
			buttonCallChooserServerpath.setEnabled(false);
		}

		JPanel panel = this;
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		int hFirstGap = Globals.HFIRST_GAP;

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE * 3, Globals.VGAP_SIZE * 4)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(infoLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
								.addComponent(buttonCallChooserPackage, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldOpsiPackageName, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(serverLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)

						).addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(panelMountShare,
								Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(serverPathLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(buttonCallChooserServerpath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldServerPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))

						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
								buttonCallExecute, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2));

		layout.setHorizontalGroup(
				layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE)
								.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(infoLabel, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
										Globals.FIRST_LABEL_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(buttonCallChooserPackage, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldOpsiPackageName, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(serverLabel, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
										Globals.FIRST_LABEL_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Globals.BUTTON_WIDTH * 2)

								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addComponent(panelMountShare,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(serverPathLabel, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
										Globals.FIRST_LABEL_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(buttonCallChooserServerpath, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldServerPath, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(5, 5, 5).addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(
								layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
										.addGap(0, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH)
										.addGap(0, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
										.addGap(0, Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(0, hFirstGap, hFirstGap)
										.addGap(0, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
										.addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(hFirstGap, hFirstGap, Short.MAX_VALUE)));
	}
}
