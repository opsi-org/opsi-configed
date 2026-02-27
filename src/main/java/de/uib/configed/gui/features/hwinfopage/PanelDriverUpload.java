/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.event.ItemEvent;
import java.io.File;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.formdev.flatlaf.util.SystemFileChooser;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandTemplate;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.WinProductUtils;
import de.uib.configed.share.WinProductsRetriever;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelDriverUpload extends JPanel {
	private static final String[] DIRECTORY_DRIVERS = new String[] { "drivers", "drivers" };
	private static final String[] DIRECTORY_DRIVERS_PREFERRED = new String[] { "drivers", "drivers", "preferred" };
	private static final String[] DIRECTORY_DRIVERS_EXCLUDED = new String[] { "drivers", "drivers", "excluded" };
	private static final String[] DIRECTORY_DRIVERS_ADDITIONAL = new String[] { "drivers", "drivers", "additional" };
	private static final String[] DIRECTORY_DRIVERS_BY_AUDIT = new String[] { "drivers", "drivers", "additional",
			"byAudit" };

	private JTextField fieldByAuditPath;
	private JLabel labelClientName;

	private JLabel depot;
	private JComboBox<String> comboChooseWinProduct;

	private String depotProductDirectory = "";
	private String driverDirectory = "";

	private JCheckBox driverPathChecked;
	private JCheckBox serverPathChecked;

	private JLabel jLabelRetrievalText = new JLabel(
			Configed.getResourceValue("PanelDriverUpload.retrievingWinProducts"));

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

	private RadioButtonIntegrationType buttonByAudit;

	private JTextField fieldDriverPath;
	private SystemFileChooser chooserDriverPath;

	// server path finding
	private JTextField fieldServerPath;

	private File driverPath;
	private File targetPath;

	private JButton buttonUploadDrivers;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private JDialog dialog;

	private WebDAVClient webDAVClient;

	public PanelDriverUpload(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		defineChoosers();

		String webDavPath = persistenceController.getDataServices().hostInfoCollections.getConfigServerWebDavPath();
		depotProductDirectory = webDavPath != null && !webDavPath.isEmpty() ? webDavPath : "depot/";
		if (!depotProductDirectory.endsWith("/")) {
			depotProductDirectory += "/";
		}
		Logging.info(this, "depotProductDirectory ", depotProductDirectory);

		jLabelRetrievalText.setVisible(false);

		webDAVClient = new WebDAVClient();

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
		depot = new JLabel(persistenceController.getDataServices().hostInfoCollections.getDepotNamesList().getFirst());

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.addActionListener(actionEvent -> produceTarget());

		chooserDriverPath = new SystemFileChooser();
		chooserDriverPath.setFileHidingEnabled(false);
		chooserDriverPath.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
		chooserDriverPath.setDialogType(SystemFileChooser.OPEN_DIALOG);
		chooserDriverPath.setDialogTitle(Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));
	}

	protected void evaluateWinProducts() {
		WinProductsRetriever.Context ctx = new WinProductsRetriever.Context();
		ctx.owner = dialog;
		ctx.webDAVClient = webDAVClient;
		ctx.msg = jLabelRetrievalText;
		ctx.options = comboChooseWinProduct;
		ctx.onDone = this::produceTarget;
		WinProductsRetriever retriever = new WinProductsRetriever(ctx);
		retriever.execute();
	}

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

			if (stateServerPath && stateDriverPath
					&& !persistenceController.getDataServices().userRoles.isGlobalReadOnly()) {
				result = true;
			}
		}

		Logging.info(this, "checkFiles ", result);

		if (buttonUploadDrivers != null) {
			buttonUploadDrivers.setEnabled(result);

			if (result) {
				buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.execute"));
			} else {
				buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.driverPathNotFound"));
			}
		}

		return result;
	}

	private void buildPanel() {
		fieldByAuditPath = new JTextField();
		fieldByAuditPath.setEditable(false);

		JLabel jLabelTopic = Utils.createBoldLabel("PanelDriverUpload.topic");

		labelClientName = new JLabel();

		JLabel jLabelDepotServer = Utils.createBoldLabel("PanelDriverUpload.DepotServer");

		JLabel jLabelWinProduct = Utils.createBoldLabel("PanelDriverUpload.labelWinProduct");

		JButton buttonCallSelectDriverFiles = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectDriverFiles
				.setToolTipText(Configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate"));

		JButton buttonCallCreateTargetDirectory = new JButton(Icons.getIntellijIcon("open"));
		buttonCallCreateTargetDirectory
				.setToolTipText(Configed.getResourceValue("PanelDriverUpload.determineServerPath"));
		buttonCallCreateTargetDirectory.addActionListener(actionEvent -> makePath(new File(fieldServerPath.getText())));

		JLabel jLabelShowDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelShowDrivers"));
		JButton buttonShowDrivers = new JButton(Icons.getIntellijIcon("run"));
		buttonShowDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnShowDrivers.tooltip"));
		buttonShowDrivers.addActionListener(actionEvent -> showDrivers());

		JLabel jLabelCreateDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelCreateDriverLinks"));
		JButton btnCreateDrivers = new JButton(Icons.getIntellijIcon("run"));
		btnCreateDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnCreateDrivers.tooltip"));
		btnCreateDrivers
				.addActionListener(
						actionEvent -> new CommandExecutor(configedMain,
								new SingleCommandTemplate("create_driver_links.py",
										"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem()
												+ "/create_driver_links.py ",
										"create_driver_links.py")).executeAsync());

		JLabel labelTargetPath = Utils.createBoldLabel("CompleteWinProducts.labelTargetPath");

		fieldServerPath = new JTextField();
		fieldServerPath.setEditable(true);
		fieldServerPath.getDocument().addDocumentListener(Utils.onDocumentChange(this::checkFiles));

		JLabel labelDriverToIntegrate = Utils.createBoldLabel("PanelDriverUpload.labelDriverToIntegrate");

		fieldDriverPath = new JTextField();
		fieldDriverPath.setEditable(true);
		fieldDriverPath.getDocument().addDocumentListener(Utils.onDocumentChange(this::checkFiles));
		buttonCallSelectDriverFiles.addActionListener(actionEvent -> chooseDriverPath());

		JLabel labelDriverLocationType = Utils.createBoldLabel("PanelDriverUpload.type");

		JPanel panelButtonGroup = createPanelButtonGroup();

		driverPathChecked = new JCheckBox(Configed.getResourceValue("PanelDriverUpload.driverpathConnected"), true);
		driverPathChecked.setEnabled(false);

		serverPathChecked = new JCheckBox(Configed.getResourceValue("PanelDriverUpload.targetdirConnected"), true);
		serverPathChecked.setEnabled(false);

		buttonUploadDrivers = new JButton(Configed.getResourceValue("FDriverUpload.upload"));
		buttonUploadDrivers.setEnabled(false);

		buttonUploadDrivers.addActionListener(actionEvent -> execute());

		this.setLayout(new MigLayout("insets 0, wrap 1", "[]" + Globals.MIN_GAP_SIZE + "[]", "[]0"));
		this.add(jLabelTopic);
		this.add(labelClientName, "gapbottom " + Globals.GAP_SIZE);

		this.add(jLabelDepotServer);
		this.add(depot, "gapbottom " + Globals.GAP_SIZE);

		this.add(jLabelWinProduct);
		this.add(comboChooseWinProduct, "gapbottom " + Globals.GAP_SIZE);

		this.add(jLabelRetrievalText, "hidemode 3, gapbottom " + Globals.GAP_SIZE);
		this.add(jLabelShowDrivers, "split 2, gapbottom " + Globals.GAP_SIZE);
		this.add(buttonShowDrivers, "aligny center, wrap, gapbottom " + Globals.GAP_SIZE);

		this.add(jLabelCreateDrivers, "split 2, gapbottom " + Globals.GAP_SIZE);
		this.add(btnCreateDrivers, "aligny center, wrap, gapbottom " + Globals.GAP_SIZE);

		this.add(labelDriverToIntegrate);
		this.add(fieldDriverPath, "split 2, growx, pushx, gapbottom " + Globals.GAP_SIZE);
		this.add(buttonCallSelectDriverFiles, "aligny center, wrap, gapbottom " + Globals.GAP_SIZE);

		this.add(labelDriverLocationType);
		this.add(panelButtonGroup, "growx, pushx, gapbottom " + Globals.GAP_SIZE);

		this.add(labelTargetPath);
		this.add(fieldServerPath, "split 2, growx, pushx, gapbottom " + Globals.GAP_SIZE);
		this.add(buttonCallCreateTargetDirectory, "aligny center, wrap, gapbottom " + Globals.GAP_SIZE);

		this.add(driverPathChecked);
		this.add(serverPathChecked);
	}

	private JPanel createPanelButtonGroup() {
		JLabel jLabelByAuditDriverLocationPath = new JLabel(
				Configed.getResourceValue("PanelDriverUpload.byAuditDriverLocationPath"));

		buttonByAudit = new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.byAudit"),
				getLocalsystemPath(DIRECTORY_DRIVERS_BY_AUDIT));

		List<RadioButtonIntegrationType> radioButtons = List.of(
				new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.standard"),
						getLocalsystemPath(DIRECTORY_DRIVERS)),
				new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.preferred"),
						getLocalsystemPath(DIRECTORY_DRIVERS_PREFERRED)),
				new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.excluded"),
						getLocalsystemPath(DIRECTORY_DRIVERS_EXCLUDED)),
				new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.additional"),
						getLocalsystemPath(DIRECTORY_DRIVERS_ADDITIONAL)),
				buttonByAudit);

		ButtonGroup buttonGroup = new ButtonGroup();
		radioButtons.forEach(buttonGroup::add);
		radioButtons.forEach(button -> button
				.addItemListener(itemEvent -> reactToIntegrationTypeChange(itemEvent.getStateChange(), button)));

		JPanel panelButtonGroup = new JPanel();

		panelButtonGroup.setLayout(new MigLayout("insets 0, wrap 1", "", "[]0"));

		radioButtons.forEach(panelButtonGroup::add);

		panelButtonGroup.add(jLabelByAuditDriverLocationPath, "split 2, gapleft 50, aligny center");
		panelButtonGroup.add(fieldByAuditPath, "gapleft " + Globals.MIN_GAP_SIZE + ", wrap");

		return panelButtonGroup;
	}

	private void reactToIntegrationTypeChange(int stateChange, RadioButtonIntegrationType button) {
		if (stateChange == ItemEvent.SELECTED) {
			Logging.debug(this, stateChange);
			driverDirectory = button.getSubdir();

			produceTarget();
		}
	}

	private void initValues() {
		buttonByAudit.setSelected(true);
	}

	private void makePath(File path) {
		Logging.info(this, "makePath for ", path);

		boolean result = WinProductUtils.ensureServerDirectoryExists(dialog, webDAVClient, path,
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.title"));
		serverPathChecked.setSelected(result);

		if (result) {
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("PanelDriverUpload.targetDirectoryExists"),
					Configed.getResourceValue("info"), JOptionPane.INFORMATION_MESSAGE);
			checkFiles();
		}

		Logging.info(this, "makePath result ", path, " exists or created ", result);
	}

	private void showDrivers() {
		CommandExecutor executor = new CommandExecutor(configedMain,
				new SingleCommandTemplate("show_drivers.py", "/var/lib/opsi/depot/"
						+ comboChooseWinProduct.getSelectedItem() + "/show_drivers.py " + labelClientName.getText(),
						"show_drivers.py"));
		executor.executeAsync();
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

	public void setByAuditPath(String byAuditPath) {
		fieldByAuditPath.setText(byAuditPath);
		produceTarget();
	}

	public void setClientName(String s) {
		labelClientName.setText(s);
	}

	public void setDepot() {
		depot.setText(persistenceController.getDataServices().hostInfoCollections.getConfigServer());
	}

	private void produceTarget() {
		if (fieldServerPath == null) {
			// caution we are not yet initialized
			return;
		}

		String winProduct = (String) comboChooseWinProduct.getSelectedItem();
		winProduct = winProduct == null || "null".equals(winProduct) ? "" : winProduct;

		String result = depotProductDirectory + winProduct + driverDirectory + "/";

		if (buttonByAudit.isSelected()) {
			result += fieldByAuditPath.getText() + "/";
		}

		fieldServerPath.setText(result);
	}

	private void chooseDriverPath() {
		if (chooserDriverPath.showOpenDialog(dialog) == SystemFileChooser.APPROVE_OPTION) {
			String pathInstallFiles = chooserDriverPath.getSelectedFile().getPath();
			fieldDriverPath.setText(pathInstallFiles);
			fieldDriverPath.setCaretPosition(pathInstallFiles.length());
		} else {
			fieldDriverPath.setText("");
		}
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
