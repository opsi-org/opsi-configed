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

import org.apache.commons.io.FileUtils;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitCursor;

public class PanelInstallOpsiPackage extends JPanel implements NameProducer {

	int firstLabelWidth = Globals.firstLabelWidth;

	JButton buttonCallChooserPackage;
	JButton buttonSelectTmpDir;
	JFileChooser chooserPackage;
	JFileChooser chooserTmpDir;
	JComboBox<String> comboChooseDepot;
	JButton buttonCallExecute;

	String opsiPackagePathToHandleS = null;
	String opsiPackageOnWorkbenchS = null;
	File opsiPackageOnWorkbench;
	String opsiPackageNameS = null;

	JTextField fieldOpsiPackageName;
	JTextField fieldTmpDir;

	final String defaultTmpDir = "(Default)";

	final String packageShareS = "opsi_workbench";
	String opsiWorkBenchDirectoryS;
	File opsiWorkBenchDirectory;
	String opsiPackageServerPathS;

	boolean smbMounted;
	PanelMountShare panelMountShare;

	final boolean isWindows;

	// server path finding
	JTextField fieldServerPath;
	JButton buttonCallChooserServerpath;
	JFileChooser chooserServerpath;

	// String workbench;

	PersistenceController persist;
	ConfigedMain main;
	JFrame rootFrame;

	public PanelInstallOpsiPackage(ConfigedMain main, PersistenceController persist, JFrame root) {
		this.main = main;
		this.persist = persist;
		this.rootFrame = root;

		isWindows = de.uib.utilities.Globals.isWindows();

		initComponents();

		panelMountShare = new PanelMountShare((NameProducer) this, main, root);

		defineLayout();
	}

	private boolean installProductFromWorkbench() {
		WaitCursor waitCursor = null;

		opsiPackageOnWorkbenchS = null;
		opsiPackageNameS = null;

		try {
			File opsiPackagePathToHandle = new File(fieldOpsiPackageName.getText());

			opsiPackageNameS = opsiPackagePathToHandle.getName();

			opsiWorkBenchDirectoryS = fieldServerPath.getText();

			opsiPackageOnWorkbenchS = opsiWorkBenchDirectoryS + File.separator + opsiPackageNameS;

			logging.debug(this, "getProductToWorkbench  target " + opsiPackageOnWorkbenchS);

			opsiPackageOnWorkbench = new File(opsiPackageOnWorkbenchS);

			if (opsiPackageNameS == null || opsiPackageNameS.trim().equals(""))
				return false;

			else {

				if (opsiPackageOnWorkbench.exists()) {
					int returnedOption = JOptionPane.NO_OPTION;

					returnedOption = JOptionPane.showOptionDialog(rootFrame,
							configed.getResourceValue("InstallOpsiPackage.packageReinstall") + " "
									+ opsiWorkBenchDirectoryS + " "
									+ configed.getResourceValue("InstallOpsiPackage.packageReinstall2"),
							Globals.APPNAME + " "
									+ configed.getResourceValue("InstallOpsiPackage.packageReinstallTitle"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					if (returnedOption == JOptionPane.YES_OPTION)
						return true;
					else
						return false;
				} else
				// it is not there and we have to copy it
				{
					waitCursor = new WaitCursor(rootFrame);

					try {
						opsiWorkBenchDirectory = new File(fieldServerPath.getText());
						// chooserPackage.setCurrentDirectory( opsiWorkBenchDirectory );
						// we start at a local directory
					} catch (Exception ex) {
						logging.info(this, "trying to build file " + opsiWorkBenchDirectoryS + " : " + ex);
					}

					logging.debug(this,
							"getProductToWorkbench copy " + opsiPackagePathToHandle + ", " + opsiWorkBenchDirectory);
					FileUtils.copyFileToDirectory(opsiPackagePathToHandle, opsiWorkBenchDirectory);
					waitCursor.stop();
					return true;
				}
			}

		} catch (Exception ex) {
			if (waitCursor != null)
				waitCursor.stop();
			logging.error("library missing or path problem " + ex, ex);
		}

		return false;

	}

	private void produceServerPath() {
		logging.debug(this, "produceServerPath ");

		opsiPackageServerPathS = PersistenceController.packageServerDirectoryS + opsiPackageNameS;
		logging.debug(this, "produceServerPath " + opsiPackageServerPathS);
	}

	private void buildSambaTarget(String depotserver) {
		Map<String, Map<String, Object>> depot2depotMap = persist.getHostInfoCollections().getDepots();

		logging.info(this, "buildSambaTarget for depotserver " + depotserver);
		// logging.info(this, "buildSambaTarget depotMap " +
		// depot2depotMap.get(depotserver));

		if (depot2depotMap.get(depotserver) == null)
			return;

		String depotRemoteUrl = (String) depot2depotMap.get(depotserver).get("depotRemoteUrl");

		if (depotRemoteUrl == null) {
			logging.warning(this, "buildSambaTarget, depotRemoteUrl null");
			return;
		}

		String[] parts = depotRemoteUrl.split("/");
		String netbiosName = "";

		if (parts.length > 2) {
			netbiosName = parts[2];
			logging.info(this, "buildSambaTarget " + netbiosName);
		} else {
			logging.warning(this, "buildSambaTarget, no splitting for " + depotRemoteUrl);
		}

		opsiWorkBenchDirectoryS = File.separator + File.separator + netbiosName + File.separator + packageShareS;

		logging.info(this, "buildSambaTarget " + opsiWorkBenchDirectoryS);

		smbMounted = new File(opsiWorkBenchDirectoryS).exists();

	}

	public void execute() {
		boolean force = true;
		Object propertyDefaultValues = null;

		if (installProductFromWorkbench()) {
			produceServerPath();
			WaitCursor waitCursor = new WaitCursor(rootFrame);
			persist.setRights(opsiPackageServerPathS);
			boolean result = persist.installPackage(opsiPackageServerPathS);
			waitCursor.stop();

			String resultMessage = "";

			if (result) {
				resultMessage = configed.getResourceValue("InstallOpsiPackage.reportSuccessful");
			} else {
				resultMessage = configed.getResourceValue("InstallOpsiPackage.reportFailed");
			}

			logging.info(this, "installPackage wrongly reporesult " + result);

			if (result)
				JOptionPane.showMessageDialog(rootFrame, "Ready", // resultMessage,
						configed.getResourceValue("InstallOpsiPackage.reportTitle"), JOptionPane.INFORMATION_MESSAGE);

		}
	}

	private void choosePackage() {

		logging.debug(this, "buttonCallChooserPackage action  starting ");

		int returnVal = chooserPackage.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String opsiPackageGotPathS = chooserPackage.getSelectedFile().getPath();
			fieldOpsiPackageName.setText(opsiPackageGotPathS);
			fieldOpsiPackageName.setCaretPosition(opsiPackageGotPathS.length());

		}
		/*
		 * else
		 * {
		 * opsiPackageGotPathS = null;
		 * fieldOpsiPackageName.setText("");
		 * }
		 */
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

		logging.debug(this, "defineChoosers, depots: " + persist.getHostInfoCollections().getDepots());

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(main.getLinkedDepots().toArray(new String[0])));
		comboChooseDepot.setEnabled(false); // as long as we did not implement contacting a different depot

		chooserPackage = new JFileChooser();
		chooserPackage.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		// chooserPackage.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
				"opsi package", "opsi");
		chooserPackage.addChoosableFileFilter(filter);
		chooserPackage.setFileFilter(filter);
		chooserPackage.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserPackage);
		// chooserPackage.setControlButtonsAreShown(false);

		chooserPackage.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserPackage.setDialogTitle(Globals.APPNAME + " " + configed.getResourceValue("InstallOpsiPackage.chooser"));

		chooserTmpDir = new JFileChooser();
		chooserTmpDir.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		chooserTmpDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserTmpDir.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserTmpDir);
		// chooserTmpDir.setControlButtonsAreShown(false);

		chooserTmpDir.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserTmpDir.setDialogTitle(Globals.APPNAME + " " + configed.getResourceValue("InstallOpsiPackage.chooser"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);
		// chooserServerpath.setControlButtonsAreShown(false);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(
				Globals.APPNAME + " " + configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));

	}

	// =======
	// implements NameProducer
	public String produceName() {
		return opsiWorkBenchDirectoryS;
	}

	public String getDefaultName() {
		return packageShareS;
	}
	// =======

	private void initComponents() {
		defineChoosers();

		buildSambaTarget("" + comboChooseDepot.getSelectedItem());

		// workbench = "\\\\" + comboChooseDepot.getSelectedItem() + "\\" +
		// packageShareS;

		// fieldServerPath = new JTextField("");

		final JPanel panel = this;

		fieldOpsiPackageName = new JTextField();
		fieldOpsiPackageName.setEditable(true);
		fieldOpsiPackageName.setPreferredSize(Globals.textfieldDimension);

		buttonCallChooserPackage = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallChooserPackage.setToolTipText(configed.getResourceValue("InstallOpsiPackage.chooserPackage"));

		buttonCallChooserPackage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				choosePackage();
			}
		});

		/*
		 * buttonMountShare = new JButton("",
		 * Globals.createImageIcon("images/windows16.png", "" ));
		 * buttonMountShare.setSelectedIcon(
		 * Globals.createImageIcon("images/windows16.png", "" ) );
		 * buttonMountShare.setPreferredSize(Globals.graphicButtonDimension);
		 * buttonMountShare.setToolTipText(configed.getResourceValue(
		 * "InstallOpsiPackage.mountShareDescription")) ;
		 * 
		 * buttonMountShare.setEnabled(isWindows);
		 * 
		 * buttonMountShare.addActionListener(new ActionListener(){
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * callMountWorkbench();
		 * }
		 * }
		 * );
		 */

		buttonSelectTmpDir = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonSelectTmpDir.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonSelectTmpDir.setPreferredSize(Globals.graphicButtonDimension);
		buttonSelectTmpDir.setToolTipText(configed.getResourceValue("InstallOpsiPackage.chooserTmpDir"));

		fieldServerPath = new JTextField(opsiWorkBenchDirectoryS);
		fieldServerPath.setForeground(Globals.greyed);

		fieldServerPath.setPreferredSize(Globals.textfieldDimension);

		buttonCallChooserServerpath = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallChooserServerpath.setToolTipText(configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));

		buttonCallChooserServerpath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseServerpath();
			}
		});

		fieldTmpDir = new JTextField(defaultTmpDir) {
			@Override
			public String getText() {
				if (super.getText().equals(defaultTmpDir))
					return "";
				else
					return super.getText();
			}
		};

		fieldTmpDir.setPreferredSize(Globals.textfieldDimension);

		buttonSelectTmpDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int returnVal = chooserTmpDir.showOpenDialog(panel);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String tmpDir = chooserTmpDir.getSelectedFile().getPath();
					logging.info(this, "file chosen : " + tmpDir);
					fieldTmpDir.setText(tmpDir);
					fieldTmpDir.setCaretPosition(tmpDir.length());
				} else {
					fieldTmpDir.setText(defaultTmpDir);
				}

			}
		});

		buttonCallExecute = new JButton("", Globals.createImageIcon("images/installpackage.png", ""));
		// Flashing( Globals.createImageIcon("images/installpackage.png", "" ) );

		buttonCallExecute.setSelectedIcon(Globals.createImageIcon("images/installpackage.png", ""));
		buttonCallExecute.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallExecute.setToolTipText(configed.getResourceValue("InstallOpsiPackage.execute"));

		buttonCallExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Color saveColor = buttonCallExecute.getBackground();
				// final Icon saveIcon = buttonCallExecute.getIcon();
				buttonCallExecute.setBackground(Globals.failedBackColor);
				// buttonCallExecute.setIcon(null);
				logging.info(this, "actionPerformed on buttonCallExecute opsiPackageGotPathS,  depot:  "
						+ fieldOpsiPackageName.getText() + ", " + comboChooseDepot.getSelectedItem());
				// SwingUtilities.invokeLater(new Runnable(){
				// public void run()
				// {
				execute();

				buttonCallExecute.setBackground(saveColor);
				// buttonCallExecute.setIcon(saveIcon);
				// }
				// }
				// );

			}
		});
	}

	public void defineLayout() {
		setBorder(Globals.createPanelBorder());

		JLabel topicLabel = new JLabel(configed.getResourceValue("InstallOpsiPackage.topic"));
		JLabel infoLabel = new JLabel(configed.getResourceValue("InstallOpsiPackage.info"));

		JLabel serverLabel = new JLabel(configed.getResourceValue("InstallOpsiPackage.chooseDepot"));
		// JLabel tmpdirLabel = new JLabel(
		// configed.getResourceValue("InstallOpsiPackage.tmpDir") );
		// mountShareLabel = new JLabel(
		// configed.getResourceValue("InstallOpsiPackage.mountShare" ) );
		// mountShareLabel = new JLabel("");
		// mountShareDescriptionLabel = new JLabel(
		// configed.getResourceValue("InstallOpsiPackage.mountShareResult0" ) );

		// checkConnectionToWorkbench(10);
		// mountShareDescriptionLabel.setVisible(isWindows);

		JLabel serverPathLabel = new JLabel(configed.getResourceValue("InstallOpsiPackage.serverpath"));

		if (isWindows) {
			serverPathLabel.setForeground(Globals.greyed);
			buttonCallChooserServerpath.setEnabled(false);

		}

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
						.addComponent(infoLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(buttonCallChooserPackage, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight)
						.addComponent(fieldOpsiPackageName, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(serverLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(comboChooseDepot, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				// .addComponent(fieldDepotName, Globals.lineHeight, Globals.lineHeight,
				// Globals.lineHeight)
				).addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(panelMountShare,
						Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(serverPathLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(buttonCallChooserServerpath, Globals.lineHeight, Globals.lineHeight,
								Globals.lineHeight)
						.addComponent(fieldServerPath, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))

				/*
				 * .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
				 * .addGroup( layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				 * .addComponent(tmpdirLabel, Globals.lineHeight, Globals.lineHeight,
				 * Globals.lineHeight)
				 * .addComponent(buttonSelectTmpDir, Globals.lineHeight, Globals.lineHeight,
				 * Globals.lineHeight)
				 * .addComponent(fieldTmpDir, Globals.lineHeight, Globals.lineHeight,
				 * Globals.lineHeight)
				 * //.addComponent(fieldDepotName, Globals.lineHeight, Globals.lineHeight,
				 * Globals.lineHeight)
				 * )
				 */
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonCallExecute,
						Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize * 2, Short.MAX_VALUE)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 2, Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(infoLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(buttonCallChooserPackage, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(hFirstGap, hFirstGap, hFirstGap)
								.addComponent(fieldOpsiPackageName, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
										Short.MAX_VALUE)
								.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(serverLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(comboChooseDepot, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Globals.buttonWidth * 2)
						// .addComponent(fieldDepotName, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(serverPathLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(buttonCallChooserServerpath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(hFirstGap, hFirstGap, hFirstGap)
						.addComponent(fieldServerPath, Globals.buttonWidth * 2, Globals.buttonWidth * 2,
								Short.MAX_VALUE)
						.addGap(5, 5, 5).addGap(Globals.hGapSize, Globals.hGapSize * 3, Short.MAX_VALUE))
				/*
				 * .addGroup(layout.createSequentialGroup()
				 * .addGap(hFirstGap, hFirstGap, hFirstGap)
				 * .addComponent(tmpdirLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
				 * .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
				 * .addComponent(buttonSelectTmpDir,Globals.graphicButtonWidth,
				 * Globals.graphicButtonWidth, Globals.graphicButtonWidth)
				 * .addGap(hFirstGap, hFirstGap, hFirstGap)
				 * .addComponent(fieldTmpDir, Globals.buttonWidth *2 , Globals.buttonWidth*2,
				 * Short.MAX_VALUE)
				 * //.addComponent(fieldDepotName, GroupLayout.PREFERRED_SIZE,
				 * GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				 * .addGap(Globals.hGapSize, Globals.hGapSize*3, Short.MAX_VALUE)
				 * )
				 */

				/*
				 * links platziert
				 * .addGroup(layout.createSequentialGroup()
				 * .addGap(hFirstGap, hFirstGap, hFirstGap)
				 * .addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE,
				 * GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				 * .addGap(hFirstGap, hFirstGap, Short.MAX_VALUE)
				 * )
				 */
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
