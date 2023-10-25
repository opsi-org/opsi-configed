/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.productaction.PanelMountShare;
import de.uib.connectx.SmbConnect;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.FLoadingWaiter;
import de.uib.utilities.swing.JTextShowField;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class PanelDriverUpload extends JPanel implements NameProducer {
	private static final String[] DIRECTORY_DRIVERS = new String[] { "drivers", "drivers" };
	private static final String[] DIRECTORY_DRIVERS_PREFERRED = new String[] { "drivers", "drivers", "preferred" };
	private static final String[] DIRECTORY_DRIVERS_EXCLUDED = new String[] { "drivers", "drivers", "excluded" };
	private static final String[] DIRECTORY_DRIVERS_ADDITIONAL = new String[] { "drivers", "drivers", "additional" };
	private static final String[] DIRECTORY_DRIVERS_BY_AUDIT = new String[] { "drivers", "drivers", "additional",
			"byAudit" };
	private int hFirstGap = Globals.HFIRST_GAP;

	private int hGap = Globals.GAP_SIZE / 2;
	private int vGap = Globals.GAP_SIZE / 2;

	private String byAuditPath = "";

	private JTextShowField fieldByAuditPath;
	private JTextShowField fieldClientname;

	private JComboBox<String> comboChooseDepot;
	private JComboBox<String> comboChooseWinProduct;

	private JLabel labelDriverToIntegrate;
	private PanelMountShare panelMountShare;

	private String depotProductDirectory = "";
	private boolean smbMounted;
	private String driverDirectory = "";

	private boolean stateDriverPath;
	private CheckedLabel driverPathChecked;
	private boolean stateServerPath;
	private CheckedLabel serverPathChecked;

	private static class RadioButtonIntegrationType extends JRadioButton {
		private String subdir;

		public RadioButtonIntegrationType(String text, String subdir) {
			super(text);
			this.subdir = subdir;
		}

		public String getSubdir() {
			return subdir;
		}
	}

	private class FileNameDocumentListener implements DocumentListener {
		private boolean checkFiles() {
			boolean result = false;

			if (fieldServerPath != null && fieldDriverPath != null) {

				targetPath = new File(fieldServerPath.getText());
				driverPath = new File(fieldDriverPath.getText());

				stateServerPath = targetPath.isDirectory();
				serverPathChecked.setSelected(stateServerPath);
				Logging.info(this, "checkFiles  stateServerPath targetPath " + targetPath);
				Logging.info(this, "checkFiles  stateServerPath driverPath " + driverPath);
				Logging.info(this, "checkFiles  stateServerPath isDirectory " + stateServerPath);

				stateDriverPath = driverPath.exists();
				driverPathChecked.setSelected(stateDriverPath);
				Logging.info(this, "checkFiles stateDriverPath " + stateDriverPath);

				if (stateServerPath && stateDriverPath) {
					result = true;
				}
			}

			Logging.info(this, "checkFiles " + result);

			if (buttonUploadDrivers != null) {
				buttonUploadDrivers.setEnabled(result);

				if (result) {
					buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.execute"));
				} else {
					buttonUploadDrivers.setToolTipText("Treiber- bzw. Zielpfad noch nicht gefunden");
				}
			}

			return result;
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			Logging.debug(this, "changedUpdate ");
			checkFiles();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			Logging.debug(this, "insertUpdate ");
			checkFiles();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			Logging.debug(this, "removeUpdate ");
			checkFiles();
		}
	}

	private RadioButtonIntegrationType buttonByAudit;

	private JTextShowField fieldDriverPath;
	private JFileChooser chooserDriverPath;

	// server path finding
	private JTextShowField fieldServerPath;
	private JFileChooser chooserServerpath;

	private File driverPath;
	private File targetPath;

	private JButton buttonUploadDrivers;

	private String selectedDepot;
	private String winProduct = "";

	private JLabel jLabelTopic;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private String server;
	private SecondaryFrame rootFrame;

	public PanelDriverUpload(ConfigedMain configedMain, SecondaryFrame root) {
		this.configedMain = configedMain;
		this.rootFrame = root;
		server = configedMain.getConfigserver();

		defineChoosers();

		selectedDepot = (String) comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.buildSambaTarget(selectedDepot, SmbConnect.PRODUCT_SHARE_RW);
		Logging.info(this.getClass(), "depotProductDirectory " + depotProductDirectory);

		jLabelTopic = new JLabel(Configed.getResourceValue("PanelDriverUpload.topic"));

		labelDriverToIntegrate = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));

		panelMountShare = new PanelMountShare(this, root, labelDriverToIntegrate.getPreferredSize().width + hGap) {
			@Override
			protected boolean checkConnectionToShare() {
				boolean connected = super.checkConnectionToShare();

				if (comboChooseWinProduct != null && connected) {
					// we have an initialized gui and are connected

					evaluateWinProducts();
				}

				return connected;
			}
		};

		initComponents();

		Logging.info(this.getClass(), "depotProductDirectory " + depotProductDirectory);
		smbMounted = new File(depotProductDirectory).exists();
		panelMountShare.mount(smbMounted);

		evaluateWinProducts();

		buildPanel();

		// We init the values later, since there are listeners attached to it.
		// If we init the values earlier, null objects will be accessed
		initValues();
	}

	private void defineChoosers() {
		comboChooseDepot = new JComboBox<>();
		comboChooseDepot.setSize(Globals.TEXT_FIELD_DIMENSION);

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(configedMain.getLinkedDepots().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener((ActionEvent actionEvent) -> {
			selectedDepot = (String) comboChooseDepot.getSelectedItem();
			Logging.info(this, "actionPerformed  depot selected " + selectedDepot);
		});

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.setSize(Globals.TEXT_FIELD_DIMENSION);
		comboChooseWinProduct.addActionListener((ActionEvent actionEvent) -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			Logging.info(this, "winProduct  " + winProduct);
			produceTarget();
		});

		chooserDriverPath = new JFileChooser();
		chooserDriverPath.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooserDriverPath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserDriverPath);

		chooserDriverPath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserDriverPath.setDialogTitle(
				Globals.APPNAME + " " + Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(
				Globals.APPNAME + " " + Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));

	}

	private void initComponents() {
		defineChoosers();
	}

	private void evaluateWinProducts() {
		retrieveWinProducts();

		winProduct = (String) comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}

	private void retrieveWinProducts() {
		Logging.info(this, "retrieveWinProducts in " + depotProductDirectory);

		if (depotProductDirectory == null) {
			return;
		}

		// not yet a depot selected

		smbMounted = new File(depotProductDirectory).exists();

		Logging.info(this, "retrieveWinProducts smbMounted " + smbMounted);

		List<String> winProducts = persistenceController.getProductDataService().getWinProducts(server,
				depotProductDirectory);

		comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
	}

	private void buildPanel() {

		fieldByAuditPath = new JTextShowField();

		fieldClientname = new JTextShowField();

		JLabel jLabelDepotServer = new JLabel(Configed.getResourceValue("PanelDriverUpload.DepotServer"));
		JLabel jLabelWinProduct = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelWinProduct"));

		JButton buttonCallSelectDriverFiles = new JButton("", Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectDriverFiles.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectDriverFiles.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallSelectDriverFiles
				.setToolTipText(Configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate"));

		fieldServerPath = new JTextShowField(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		JButton buttonCallChooserServerpath = new JButton("", Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		buttonCallChooserServerpath.setToolTipText(Configed.getResourceValue("PanelDriverUpload.determineServerPath"));

		buttonCallChooserServerpath.addActionListener(actionEvent -> chooseServerpath());

		JLabel jLabelShowDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelShowDrivers"));
		JButton buttonShowDrivers = new JButton("", Utils.createImageIcon("images/show-menu.png", ""));
		buttonShowDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnShowDrivers.tooltip"));
		buttonShowDrivers.addActionListener(actionEvent -> showDrivers());

		JLabel jLabelCreateDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelCreateDriverLinks"));
		JButton btnCreateDrivers = new JButton("", Utils.createImageIcon("images/run-build-file.png", ""));
		btnCreateDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnCreateDrivers.tooltip"));
		btnCreateDrivers.addActionListener(actionEvent -> new SSHConnectExec(configedMain,
				// id not needed
				new EmptyCommand("create_driver_links.py",
						"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/create_driver_links.py ",
						// menutext - not needed
						"create_driver_links.py",
						// need sudo ?
						true)));

		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		fieldServerPath = new JTextShowField(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		fieldDriverPath = new JTextShowField(true);
		fieldDriverPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		final JPanel thisPanel = this;

		buttonCallSelectDriverFiles.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserDriverPath.showOpenDialog(thisPanel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserDriverPath.getSelectedFile().getPath();
				fieldDriverPath.setText(pathInstallFiles);
				fieldDriverPath.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldDriverPath.setText("");
			}
		});

		JLabel jLabelByAuditDriverLocationPath = new JLabel(
				Configed.getResourceValue("PanelDriverUpload.byAuditDriverLocationPath"));
		JLabel labelDriverLocationType = new JLabel(Configed.getResourceValue("PanelDriverUpload.type"));

		List<RadioButtonIntegrationType> radioButtons = new ArrayList<>();

		RadioButtonIntegrationType buttonStandard = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.standard"), getLocalsystemPath(DIRECTORY_DRIVERS));
		RadioButtonIntegrationType buttonPreferred = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.preferred"),
				getLocalsystemPath(DIRECTORY_DRIVERS_PREFERRED));
		RadioButtonIntegrationType buttonNotPreferred = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.excluded"),
				getLocalsystemPath(DIRECTORY_DRIVERS_EXCLUDED));
		RadioButtonIntegrationType buttonAdditional = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.additional"),
				getLocalsystemPath(DIRECTORY_DRIVERS_ADDITIONAL));
		buttonByAudit = new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.byAudit"),
				getLocalsystemPath(DIRECTORY_DRIVERS_BY_AUDIT));

		radioButtons.add(buttonStandard);
		radioButtons.add(buttonPreferred);
		radioButtons.add(buttonNotPreferred);
		radioButtons.add(buttonAdditional);
		radioButtons.add(buttonByAudit);

		ButtonGroup buttonGroup = new ButtonGroup();

		for (final RadioButtonIntegrationType button : radioButtons) {
			buttonGroup.add(button);
			button.addItemListener((ItemEvent e) -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Logging.debug(this, " " + e);
					driverDirectory = button.getSubdir();

					produceTarget();
				}
			});
		}

		JPanel panelButtonGroup = new JPanel();
		GroupLayout layoutButtonGroup = new GroupLayout(panelButtonGroup);
		panelButtonGroup.setLayout(layoutButtonGroup);
		panelButtonGroup.setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 1, true));

		layoutButtonGroup.setVerticalGroup(layoutButtonGroup.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(labelDriverLocationType, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(vGap, vGap, vGap)
				.addComponent(buttonStandard, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonPreferred, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonNotPreferred, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonAdditional, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonByAudit, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGroup(layoutButtonGroup.createParallelGroup()
						.addComponent(jLabelByAuditDriverLocationPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldByAuditPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(vGap, vGap, vGap));

		layoutButtonGroup
				.setHorizontalGroup(
						layoutButtonGroup.createSequentialGroup().addGap(hGap, hGap, hGap)
								.addGroup(layoutButtonGroup.createParallelGroup()
										.addComponent(labelDriverLocationType, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonStandard, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonPreferred, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonNotPreferred, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonAdditional, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonByAudit, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGroup(layoutButtonGroup.createSequentialGroup().addGap(50, 50, 50)
												.addComponent(jLabelByAuditDriverLocationPath, 10,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(hGap, hGap, hGap)
												.addComponent(fieldByAuditPath, Globals.BUTTON_WIDTH,
														Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
												.addGap(hGap, hGap, hGap)))
								.addGap(hGap, hGap, hGap)

				);

		driverPathChecked = new CheckedLabel(Configed.getResourceValue("PanelDriverUpload.driverpathConnected"),
				Utils.createImageIcon("images/checked_withoutbox.png", ""),
				Utils.createImageIcon("images/checked_empty_withoutbox.png", ""), stateDriverPath);

		serverPathChecked = new CheckedLabel(Configed.getResourceValue("PanelDriverUpload.targetdirConnected"),
				Utils.createImageIcon("images/checked_withoutbox.png", "Z"),
				Utils.createImageIcon("images/checked_empty_withoutbox.png", ""), true);

		buttonUploadDrivers = new JButton(Configed.getResourceValue("FDriverUpload.upload"));
		buttonUploadDrivers.setEnabled(false);

		buttonUploadDrivers.addActionListener((ActionEvent actionEvent) -> {
			Logging.info(this, "actionPerformed on buttonUploadDrivers from " + fieldDriverPath.getText() + " to "
					+ fieldServerPath.getText());

			execute();
		});

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = Globals.LINE_HEIGHT - 4;
		layoutByAuditInfo.setVerticalGroup(layoutByAuditInfo.createSequentialGroup().addGap(vGap, vGap * 2, vGap * 2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelTopic, lh, lh, lh).addComponent(fieldClientname, lh, lh, lh))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelDepotServer, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jLabelWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelShowDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonShowDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelCreateDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(btnCreateDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelDriverToIntegrate, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectDriverFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldDriverPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addComponent(panelMountShare, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonCallChooserServerpath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldServerPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(driverPathChecked, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(serverPathChecked, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonUploadDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(vGap, vGap * 2, vGap * 2)
				.addGroup(
						layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER).addGap(Globals.LINE_HEIGHT))
				.addGap(vGap, vGap * 2, vGap * 2));

		layoutByAuditInfo.setHorizontalGroup(layoutByAuditInfo.createParallelGroup().addGroup(layoutByAuditInfo
				.createSequentialGroup().addGap(hFirstGap, hFirstGap, hFirstGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup()
								.addComponent(jLabelTopic, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(hFirstGap, hFirstGap, hFirstGap)

								.addComponent(fieldClientname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH * 2))
						.addGroup(layoutByAuditInfo.createSequentialGroup().addComponent(
								panelMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutByAuditInfo.createSequentialGroup().addComponent(jLabelDepotServer)
								.addGap(hGap, hGap, hGap)
								.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH * 2)
								.addGap(hGap, hGap, hGap)
								.addComponent(jLabelWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(hGap, hGap, hGap).addComponent(comboChooseWinProduct, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 3))
						.addGroup(layoutByAuditInfo.createSequentialGroup()
								.addComponent(jLabelShowDrivers, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(hGap, hGap, hGap).addComponent(buttonShowDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH))
						.addGroup(layoutByAuditInfo.createSequentialGroup()
								.addComponent(jLabelCreateDrivers, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(hGap, hGap, hGap).addComponent(btnCreateDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
										Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH))
						.addGroup(
								layoutByAuditInfo.createSequentialGroup()
										.addComponent(labelDriverToIntegrate, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(hGap, hGap, hGap)
										.addComponent(buttonCallSelectDriverFiles, Globals.GRAPHIC_BUTTON_WIDTH,
												Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(hFirstGap, hFirstGap, hFirstGap).addComponent(fieldDriverPath,
												Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE))
						.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(layoutByAuditInfo.createSequentialGroup().addComponent(panelMountShare,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(
								layoutByAuditInfo.createSequentialGroup()
										.addComponent(labelTargetPath, labelDriverToIntegrate.getPreferredSize().width,
												labelDriverToIntegrate.getPreferredSize().width,
												labelDriverToIntegrate.getPreferredSize().width)
										.addGap(hGap, hGap, hGap)
										.addComponent(buttonCallChooserServerpath, Globals.GRAPHIC_BUTTON_WIDTH,
												Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(hFirstGap, hFirstGap, hFirstGap).addComponent(fieldServerPath,
												Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE))

				).addGap(hFirstGap, hFirstGap, hFirstGap))
				.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(5, 5, Short.MAX_VALUE)

						.addComponent(driverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(hGap, 2 * hGap, 2 * hGap)

						.addComponent(serverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(hGap, 2 * hGap, 2 * hGap)
						.addComponent(buttonUploadDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(hFirstGap / 2, hFirstGap / 2, hFirstGap / 2))

				.addGroup(layoutByAuditInfo.createSequentialGroup()

						.addGap(hGap, hGap, Short.MAX_VALUE)

						.addGap(hFirstGap / 2, hFirstGap / 2, hFirstGap / 2)));
	}

	private void initValues() {
		buttonByAudit.setSelected(true);
	}

	private void makePath(File path) {
		Logging.info(this, "makePath for " + path);

		if (path != null && !path.exists()) {

			int returnedOption = JOptionPane.showOptionDialog(rootFrame,
					Configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
					Configed.getResourceValue("PanelDriverUpload.makeFilePath.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				path.mkdirs();
			}
		}

		Logging.info(this, "makePath result " + path);
	}

	private void showDrivers() {
		new Thread() {
			@Override
			public void run() {
				new SSHConnectExec(configedMain,
						// Empty_Command(String id, String c, String mt, boolean ns)
						// id not needed
						new EmptyCommand("show_drivers.py",
								"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/show_drivers.py "
										+ fieldClientname.getText(),
								// menuText - not needed
								"show_drivers.py",
								// need Sudo?
								false));
			}
		}.start();
	}

	private void execute() {
		new PanelDriverUploadThread().start();
	}

	private class PanelDriverUploadThread extends Thread {

		@Override
		public void run() {
			final FLoadingWaiter waiter = new FLoadingWaiter(PanelDriverUpload.this, Globals.APPNAME,
					Configed.getResourceValue("PanelDriverUpload.execute.running"));
			waiter.startWaiting();
			rootFrame.activateLoadingCursor();

			Logging.info(this, "copy  " + driverPath + " to " + targetPath);

			makePath(targetPath);

			stateServerPath = targetPath.exists();
			serverPathChecked.setSelected(stateServerPath);
			if (stateServerPath) {
				try {
					if (driverPath.isDirectory()) {
						FileUtils.copyDirectoryToDirectory(driverPath, targetPath);
					} else {
						FileUtils.copyFileToDirectory(driverPath, targetPath);
					}
				} catch (IOException iox) {
					rootFrame.disactivateLoadingCursor();
					Logging.error("copy error:\n" + iox, iox);
				}
			} else {
				Logging.info(this, "execute: targetPath does not exist");
			}

			if (stateServerPath) {
				String driverDir = "/" + SmbConnect.unixPath(SmbConnect.directoryProducts.toArray(String[]::new)) + "/"
						+ winProduct + "/" + SmbConnect.unixPath(DIRECTORY_DRIVERS);
				Logging.info(this, "set rights for " + driverDir);
				persistenceController.getRPCMethodExecutor().setRights(driverDir);
			}

			rootFrame.disactivateLoadingCursor();

			waiter.setReady();
		}
	}

	public void setByAuditPath(String s) {
		byAuditPath = s;
		fieldByAuditPath.setText(s);
		produceTarget();
	}

	public void setClientName(String s) {
		fieldClientname.setText(s);
	}

	public void setDepot(String s) {
		comboChooseDepot.setModel(new DefaultComboBoxModel<>(new String[] { s }));
	}

	private void produceTarget() {
		if (fieldServerPath == null) {

			// caution we are not yet initialized
			return;
		}

		String result = depotProductDirectory + File.separator + winProduct + File.separator + driverDirectory;

		if (buttonByAudit.isSelected()) {
			result = result + File.separator + byAuditPath;
		}

		fieldServerPath.setText(result);
	}

	private void chooseServerpath() {

		String oldServerPath = fieldServerPath.getText();
		File currentDirectory = new File(oldServerPath);

		makePath(currentDirectory);
		chooserServerpath.setCurrentDirectory(currentDirectory);

		int returnVal = chooserServerpath.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String serverPathGot = chooserServerpath.getSelectedFile().getPath();
			fieldServerPath.setText(serverPathGot);
			fieldServerPath.setCaretPosition(serverPathGot.length());
		}
	}

	// implements NameProducer
	@Override
	public String produceName() {

		if (fieldServerPath != null) {
			Logging.info(this, "produceName ? fieldServerPath , depotProductDirectory " + fieldServerPath.getText()
					+ " , " + depotProductDirectory);
		}

		if (fieldServerPath == null || fieldServerPath.getText().isEmpty()
				|| fieldServerPath.getText().startsWith(depotProductDirectory)) {
			return depotProductDirectory;
		}

		return fieldServerPath.getText();
	}

	@Override
	public String getDefaultName() {
		return byAuditPath;
	}

	private static String getLocalsystemPath(String[] parts) {
		if (parts == null || parts.length == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder(parts[0]);

		for (int i = 1; i < parts.length; i++) {
			result.append(File.separator + parts[i]);
		}

		return result.toString();
	}
}
