/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productaction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class PanelInstallOpsiPackage extends JPanel implements NameProducer {
	private static final String PAGE_SHARE_S = "opsi_workbench";

	private JButton buttonCallChooserPackage;
	private JFileChooser chooserPackage;
	private JComboBox<String> comboChooseDepot;
	private JButton buttonCallExecute;

	private String opsiPackageNameS;

	private JTextField fieldOpsiPackageName;

	private String opsiWorkBenchDirectoryS;
	private String opsiPackageServerPathS;

	private PanelMountShare panelMountShare;

	private final boolean isWindows;

	// server path finding
	private JTextField fieldServerPath;
	private JButton buttonCallChooserServerpath;
	private JFileChooser chooserServerpath;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private SecondaryFrame rootFrame;

	public PanelInstallOpsiPackage(ConfigedMain configedMain, SecondaryFrame rootFrame) {
		this.configedMain = configedMain;
		this.rootFrame = rootFrame;

		isWindows = Utils.isWindows();

		initComponents();

		panelMountShare = new PanelMountShare(this, rootFrame);

		defineLayout();
	}

	private boolean installProductFromWorkbench() {
		String opsiPackageOnWorkbenchS = null;
		opsiPackageNameS = null;

		try {
			File opsiPackagePathToHandle = new File(fieldOpsiPackageName.getText());

			opsiPackageNameS = opsiPackagePathToHandle.getName();

			opsiWorkBenchDirectoryS = fieldServerPath.getText();

			opsiPackageOnWorkbenchS = opsiWorkBenchDirectoryS + File.separator + opsiPackageNameS;

			Logging.debug(this, "getProductToWorkbench  target " + opsiPackageOnWorkbenchS);

			File opsiPackageOnWorkbench = new File(opsiPackageOnWorkbenchS);

			if (opsiPackageNameS == null || opsiPackageNameS.isBlank()) {
				return false;
			} else {
				if (opsiPackageOnWorkbench.exists()) {
					int returnedOption = JOptionPane.showOptionDialog(rootFrame,
							Configed.getResourceValue("InstallOpsiPackage.packageReinstall") + " "
									+ opsiWorkBenchDirectoryS + " "
									+ Configed.getResourceValue("InstallOpsiPackage.packageReinstall2"),
							Configed.getResourceValue("InstallOpsiPackage.packageReinstallTitle"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					return returnedOption == JOptionPane.YES_OPTION;
				} else {
					// it is not there and we have to copy it

					rootFrame.activateLoadingCursor();

					File opsiWorkBenchDirectory = new File(fieldServerPath.getText());

					Logging.debug(this,
							"getProductToWorkbench copy " + opsiPackagePathToHandle + ", " + opsiWorkBenchDirectory);
					FileUtils.copyFileToDirectory(opsiPackagePathToHandle, opsiWorkBenchDirectory);
					rootFrame.deactivateLoadingCursor();
					return true;
				}
			}
		} catch (IOException ex) {
			rootFrame.deactivateLoadingCursor();

			Logging.error("path problem ", ex);
		}

		return false;
	}

	private void produceServerPath() {
		Logging.debug(this, "produceServerPath ");

		opsiPackageServerPathS = persistenceController.getConfigDataService().getPackageServerDirectoryPD()
				+ opsiPackageNameS;
		Logging.debug(this, "produceServerPath " + opsiPackageServerPathS);
	}

	private void buildSambaTarget(String depotserver) {
		Map<String, Map<String, Object>> depot2depotMap = persistenceController.getHostInfoCollections().getDepots();

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

	private void execute() {
		if (installProductFromWorkbench()) {
			produceServerPath();
			rootFrame.activateLoadingCursor();
			persistenceController.getRPCMethodExecutor().setRights(opsiPackageServerPathS);
			boolean result = persistenceController.getRPCMethodExecutor().installPackage(opsiPackageServerPathS);
			rootFrame.deactivateLoadingCursor();

			Logging.info(this, "installPackage wrongly reporesult " + result);

			if (result) {
				JOptionPane.showMessageDialog(rootFrame, "Ready",
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
		comboChooseDepot.setSize(Globals.TEXT_FIELD_DIMENSION);

		Logging.debug(this, "defineChoosers, depots: " + persistenceController.getHostInfoCollections().getDepots());

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(configedMain.getLinkedDepots().toArray(new String[0])));

		// as long as we did not implement contacting a different depot
		comboChooseDepot.setEnabled(false);

		chooserPackage = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("opsi package", "opsi");
		chooserPackage.addChoosableFileFilter(filter);
		chooserPackage.setFileFilter(filter);
		chooserPackage.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserPackage);

		chooserPackage.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserPackage.setDialogTitle(Configed.getResourceValue("InstallOpsiPackage.chooser"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
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
		fieldOpsiPackageName.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		buttonCallChooserPackage = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserPackage.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallChooserPackage.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.chooserPackage"));

		buttonCallChooserPackage.addActionListener(actionEvent -> choosePackage());

		fieldServerPath = new JTextField(opsiWorkBenchDirectoryS);
		fieldServerPath.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		buttonCallChooserServerpath = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallChooserServerpath.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
		buttonCallChooserServerpath.addActionListener(actionEvent -> chooseServerpath());

		if (isWindows) {
			buttonCallChooserServerpath.setEnabled(false);
		}

		buttonCallExecute = new JButton(Utils.createImageIcon("images/installpackage.png", ""));

		buttonCallExecute.setSelectedIcon(Utils.createImageIcon("images/installpackage.png", ""));
		buttonCallExecute.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallExecute.setToolTipText(Configed.getResourceValue("InstallOpsiPackage.execute"));

		buttonCallExecute.addActionListener((ActionEvent e) -> {
			Logging.info(this, "actionPerformed on buttonCallExecute opsiPackageGotPathS,  depot:  "
					+ fieldOpsiPackageName.getText() + ", " + comboChooseDepot.getSelectedItem());
			execute();
		});
	}

	private void defineLayout() {
		JLabel topicLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.topic"));
		JLabel infoLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.info"));

		JLabel serverLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.chooseDepot"));

		JLabel serverPathLabel = new JLabel(Configed.getResourceValue("InstallOpsiPackage.serverpath"));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 4)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE * 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(infoLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
								.addComponent(buttonCallChooserPackage, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldOpsiPackageName, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
						.addGap(Globals.MIN_GAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(serverLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addComponent(panelMountShare, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.MIN_GAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(serverPathLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(buttonCallChooserServerpath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldServerPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))

						.addGap(Globals.GAP_SIZE)
						.addComponent(buttonCallExecute, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE * 2));

		layout.setHorizontalGroup(
				layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE)
								.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(Globals.HFIRST_GAP)
								.addComponent(infoLabel, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
										Globals.FIRST_LABEL_WIDTH)
								.addGap(Globals.GAP_SIZE)
								.addComponent(buttonCallChooserPackage, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HFIRST_GAP)
								.addComponent(fieldOpsiPackageName, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(Globals.HFIRST_GAP)
								.addComponent(serverLabel, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
										Globals.FIRST_LABEL_WIDTH)
								.addGap(80)
								.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Globals.BUTTON_WIDTH * 2)

								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))

						.addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(
								layout.createSequentialGroup().addGap(Globals.HFIRST_GAP)
										.addComponent(serverPathLabel, Globals.FIRST_LABEL_WIDTH,
												Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH)
										.addGap(Globals.GAP_SIZE)
										.addComponent(buttonCallChooserServerpath, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HFIRST_GAP)
										.addComponent(fieldServerPath, Globals.BUTTON_WIDTH * 2,
												Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(layout.createSequentialGroup().addGap(0, 600, Short.MAX_VALUE)
								.addComponent(buttonCallExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HFIRST_GAP, Globals.HFIRST_GAP, Short.MAX_VALUE)));
	}
}
