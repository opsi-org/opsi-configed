/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandTemplate;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.NameProducer;
import de.uib.utils.WebDAVClient;
import de.uib.utils.WinProductUtils;
import de.uib.utils.logging.Logging;

public class PanelDriverUpload extends JPanel implements NameProducer {
	private static final String[] DIRECTORY_DRIVERS = new String[] { "drivers", "drivers" };
	private static final String[] DIRECTORY_DRIVERS_PREFERRED = new String[] { "drivers", "drivers", "preferred" };
	private static final String[] DIRECTORY_DRIVERS_EXCLUDED = new String[] { "drivers", "drivers", "excluded" };
	private static final String[] DIRECTORY_DRIVERS_ADDITIONAL = new String[] { "drivers", "drivers", "additional" };
	private static final String[] DIRECTORY_DRIVERS_BY_AUDIT = new String[] { "drivers", "drivers", "additional",
			"byAudit" };

	private String byAuditPath = "";

	private JTextField fieldByAuditPath;
	private JLabel labelClientName;

	private JLabel depot;
	private JComboBox<String> comboChooseWinProduct;

	private JLabel labelDriverToIntegrate;

	private String depotProductDirectory = "";
	private String driverDirectory = "";

	private JCheckBox driverPathChecked;
	private JCheckBox serverPathChecked;

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

	@SuppressWarnings("java:S2972")
	private class FileNameDocumentListener implements DocumentListener {
		private boolean checkFiles() {
			boolean result = false;

			if (fieldServerPath != null && fieldDriverPath != null) {
				targetPath = new File(fieldServerPath.getText().replace("\\", "/"));
				driverPath = new File(fieldDriverPath.getText().replace("\\", "/"));

				boolean stateServerPath = webDAVClient.existsAndIsDirectory(targetPath.getPath().replace("\\", "/"));
				serverPathChecked.setSelected(stateServerPath);
				Logging.info(this, "checkFiles  stateServerPath targetPath ", targetPath);
				Logging.info(this, "checkFiles  stateServerPath driverPath ", driverPath);
				Logging.info(this, "checkFiles  stateServerPath isDirectory ", stateServerPath);

				boolean stateDriverPath = driverPath.exists();
				driverPathChecked.setSelected(stateDriverPath);
				Logging.info(this, "checkFiles stateDriverPath ", stateDriverPath);

				if (stateServerPath && stateDriverPath) {
					result = true;
				}
			}

			Logging.info(this, "checkFiles ", result);

			if (buttonUploadDrivers != null) {
				buttonUploadDrivers.setEnabled(result);

				if (result) {
					buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.execute"));
				} else {
					buttonUploadDrivers
							.setToolTipText(Configed.getResourceValue("PanelDriverUpload.driverPathNotFound"));
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

	private JTextField fieldDriverPath;
	private JFileChooser chooserDriverPath;

	// server path finding
	private JTextField fieldServerPath;
	private JFileChooser chooserServerpath;

	private File driverPath;
	private File targetPath;

	private JButton buttonUploadDrivers;

	private String winProduct = "";

	private JLabel jLabelTopic;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private JDialog dialog;

	private WebDAVClient webDAVClient;

	public PanelDriverUpload(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		defineChoosers();

		depotProductDirectory = "depot/";
		Logging.info(this, "depotProductDirectory ", depotProductDirectory);

		jLabelTopic = new JLabel(Configed.getResourceValue("PanelDriverUpload.topic"));
		jLabelTopic.setFont(jLabelTopic.getFont().deriveFont(Font.BOLD));

		labelDriverToIntegrate = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));
		labelDriverToIntegrate.setFont(labelDriverToIntegrate.getFont().deriveFont(Font.BOLD));

		webDAVClient = new WebDAVClient();

		defineChoosers();

		Logging.info(this, "depotProductDirectory ", depotProductDirectory);

		evaluateWinProducts();

		buildPanel();

		// We init the values later, since there are listeners attached to it.
		// If we init the values earlier, null objects will be accessed
		initValues();
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	public JButton getButtonUploadDrivers() {
		return buttonUploadDrivers;
	}

	private void defineChoosers() {
		depot = new JLabel(persistenceController.getHostInfoCollections().getDepotNamesList().getFirst());

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.addActionListener((ActionEvent actionEvent) -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			Logging.info(this, "winProduct  ", winProduct);
			produceTarget();
		});

		chooserDriverPath = new JFileChooser();
		chooserDriverPath.setFileHidingEnabled(false);
		chooserDriverPath.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		SwingUtilities.updateComponentTreeUI(chooserDriverPath);

		chooserDriverPath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserDriverPath.setDialogTitle(Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setFileHidingEnabled(false);
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
	}

	private void evaluateWinProducts() {
		if (depotProductDirectory != null) {
			comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(
					WinProductUtils.getWinProducts(webDAVClient, depotProductDirectory).toArray(new String[0])));
		}

		winProduct = (String) comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}

	@SuppressWarnings("java:S138")
	private void buildPanel() {
		fieldByAuditPath = new JTextField();
		fieldByAuditPath.setEditable(false);

		labelClientName = new JLabel();

		JLabel jLabelDepotServer = new JLabel(Configed.getResourceValue("PanelDriverUpload.DepotServer"));
		jLabelDepotServer.setFont(jLabelDepotServer.getFont().deriveFont(Font.BOLD));

		JLabel jLabelWinProduct = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelWinProduct"));
		jLabelWinProduct.setFont(jLabelWinProduct.getFont().deriveFont(Font.BOLD));

		JButton buttonCallSelectDriverFiles = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectDriverFiles
				.setToolTipText(Configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate"));

		JButton buttonCallChooserServerpath = new JButton(Icons.getIntellijIcon("open"));
		buttonCallChooserServerpath.setToolTipText(Configed.getResourceValue("PanelDriverUpload.determineServerPath"));
		buttonCallChooserServerpath.addActionListener(actionEvent -> chooseServerpath());

		JLabel jLabelShowDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelShowDrivers"));
		JButton buttonShowDrivers = new JButton(Icons.getIntellijIcon("run"));
		buttonShowDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnShowDrivers.tooltip"));
		buttonShowDrivers.addActionListener(actionEvent -> showDrivers());

		JLabel jLabelCreateDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelCreateDriverLinks"));
		JButton btnCreateDrivers = new JButton(Icons.getIntellijIcon("run"));
		btnCreateDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnCreateDrivers.tooltip"));
		btnCreateDrivers.addActionListener((ActionEvent actionEvent) -> {
			CommandExecutor executor = new CommandExecutor(configedMain,
					new SingleCommandTemplate("create_driver_links.py", "/var/lib/opsi/depot/"
							+ comboChooseWinProduct.getSelectedItem() + "/create_driver_links.py ",
							"create_driver_links.py"));
			executor.execute();
		});

		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		labelTargetPath.setFont(labelTargetPath.getFont().deriveFont(Font.BOLD));

		fieldServerPath = new JTextField();
		fieldServerPath.setEditable(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		fieldDriverPath = new JTextField();
		fieldDriverPath.setEditable(true);
		fieldDriverPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		buttonCallSelectDriverFiles.addActionListener(actionEvent -> chooseDriverPath());

		JLabel labelDriverLocationType = new JLabel(Configed.getResourceValue("PanelDriverUpload.type"));
		labelDriverLocationType.setFont(labelDriverLocationType.getFont().deriveFont(Font.BOLD));

		JPanel panelButtonGroup = createPanelButtonGroup();

		driverPathChecked = new JCheckBox(Configed.getResourceValue("PanelDriverUpload.driverpathConnected"), true);
		driverPathChecked.setEnabled(false);

		serverPathChecked = new JCheckBox(Configed.getResourceValue("PanelDriverUpload.targetdirConnected"), true);
		serverPathChecked.setEnabled(false);

		buttonUploadDrivers = new JButton(Configed.getResourceValue("FDriverUpload.upload"));
		buttonUploadDrivers.setEnabled(false);

		buttonUploadDrivers.addActionListener(actionEvent -> execute());

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		layoutByAuditInfo.setVerticalGroup(layoutByAuditInfo.createSequentialGroup()
				.addComponent(jLabelTopic, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(labelClientName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelDepotServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(depot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboChooseWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelShowDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonShowDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelCreateDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btnCreateDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelDriverToIntegrate, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(fieldDriverPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCallSelectDriverFiles, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelDriverLocationType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addComponent(labelTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonCallChooserServerpath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldServerPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(driverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(serverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		layoutByAuditInfo.setHorizontalGroup(layoutByAuditInfo.createParallelGroup().addGroup(layoutByAuditInfo
				.createParallelGroup()
				.addComponent(jLabelTopic, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(labelClientName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelDepotServer)
				.addComponent(depot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboChooseWinProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addComponent(jLabelShowDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE).addComponent(buttonShowDrivers, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addComponent(jLabelCreateDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE).addComponent(btnCreateDrivers, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(labelDriverToIntegrate, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addComponent(fieldDriverPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonCallSelectDriverFiles, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(labelDriverLocationType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(labelTargetPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addComponent(fieldServerPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonCallChooserServerpath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addComponent(driverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(serverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private JPanel createPanelButtonGroup() {
		JLabel jLabelByAuditDriverLocationPath = new JLabel(
				Configed.getResourceValue("PanelDriverUpload.byAuditDriverLocationPath"));

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
					Logging.debug(this, " ", e);
					driverDirectory = button.getSubdir();

					produceTarget();
				}
			});
		}

		JPanel panelButtonGroup = new JPanel();
		GroupLayout layoutButtonGroup = new GroupLayout(panelButtonGroup);
		panelButtonGroup.setLayout(layoutButtonGroup);
		panelButtonGroup.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 1, true),
						new EmptyBorder(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
								Globals.MIN_GAP_SIZE)));

		layoutButtonGroup.setVerticalGroup(layoutButtonGroup.createSequentialGroup().addComponent(buttonStandard)
				.addComponent(buttonPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonNotPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonAdditional, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonByAudit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutButtonGroup.createParallelGroup()
						.addComponent(jLabelByAuditDriverLocationPath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldByAuditPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		layoutButtonGroup.setHorizontalGroup(layoutButtonGroup.createParallelGroup()
				.addComponent(buttonStandard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonNotPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonAdditional, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonByAudit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutButtonGroup.createSequentialGroup().addGap(50)
						.addComponent(jLabelByAuditDriverLocationPath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldByAuditPath, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		return panelButtonGroup;
	}

	private void initValues() {
		buttonByAudit.setSelected(true);
	}

	private void makePath(File path) {
		Logging.info(this, "makePath for ", path);

		boolean result = WinProductUtils.ensureServerDirectoryExists(dialog, webDAVClient, path,
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.title"));

		Logging.info(this, "makePath result ", path, " exists or created ", result);
	}

	private void showDrivers() {
		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain,
						new SingleCommandTemplate("show_drivers.py",
								"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/show_drivers.py "
										+ labelClientName.getText(),
								"show_drivers.py"));
				executor.execute();
			}
		}.start();
	}

	private void execute() {
		PanelDriverUploadWorker.Context ctx = new PanelDriverUploadWorker.Context();
		ctx.owner = dialog;
		ctx.webDAVClient = webDAVClient;
		ctx.executeButton = buttonUploadDrivers;
		ctx.targetPath = targetPath;
		ctx.driverPath = driverPath;
		ctx.serverPathChecked = serverPathChecked;
		new PanelDriverUploadWorker(ctx).execute();
	}

	public void setByAuditPath(String s) {
		byAuditPath = s;
		fieldByAuditPath.setText(s);
		produceTarget();
	}

	public void setClientName(String s) {
		labelClientName.setText(s);
	}

	public void setDepot() {
		depot.setText(persistenceController.getHostInfoCollections().getConfigServer());
	}

	private void produceTarget() {
		if (fieldServerPath == null) {
			// caution we are not yet initialized
			return;
		}

		String result = depotProductDirectory + winProduct + driverDirectory + "/";

		if (buttonByAudit.isSelected()) {
			result = result + byAuditPath + "/";
		}

		fieldServerPath.setText(result);
	}

	private void chooseServerpath() {
		String oldServerPath = fieldServerPath.getText();
		File currentDirectory = new File(oldServerPath);

		makePath(currentDirectory);
		chooserServerpath.setCurrentDirectory(currentDirectory);

		int returnVal = chooserServerpath.showOpenDialog(dialog);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String serverPathGot = chooserServerpath.getSelectedFile().getPath();
			fieldServerPath.setText(serverPathGot);
			fieldServerPath.setCaretPosition(serverPathGot.length());
		}
	}

	private void chooseDriverPath() {
		if (chooserDriverPath.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
			String pathInstallFiles = chooserDriverPath.getSelectedFile().getPath();
			fieldDriverPath.setText(pathInstallFiles);
			fieldDriverPath.setCaretPosition(pathInstallFiles.length());
		} else {
			fieldDriverPath.setText("");
		}
	}

	// implements NameProducer
	@Override
	public String produceName() {
		if (fieldServerPath != null) {
			Logging.info(this, "produceName ? fieldServerPath , depotProductDirectory ", fieldServerPath.getText(),
					" , ", depotProductDirectory);
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
			result.append("/" + parts[i]);
		}

		return result.toString();
	}
}
