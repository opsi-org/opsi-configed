/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandTemplate;
import de.uib.configed.share.Icons;
import de.uib.configed.share.NameProducer;
import de.uib.configed.share.Utils;
import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.WinProductUtils;
import de.uib.configed.share.WinProductsRetriever;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

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

	@SuppressWarnings("java:S2972")
	private class FileNameDocumentListener implements DocumentListener {
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

		String webDavPath = persistenceController.getDataServices().hostInfoCollections.getConfigServerWebDavPath();
		depotProductDirectory = webDavPath != null && !webDavPath.isEmpty() ? webDavPath : "depot/";
		if (!depotProductDirectory.endsWith("/")) {
			depotProductDirectory += "/";
		}
		Logging.info(this, "depotProductDirectory ", depotProductDirectory);

		jLabelTopic = Utils.createBoldLabel("PanelDriverUpload.topic");

		labelDriverToIntegrate = Utils.createBoldLabel("PanelDriverUpload.labelDriverToIntegrate");
		jLabelRetrievalText.setVisible(false);

		webDAVClient = new WebDAVClient();

		defineChoosers();

		Logging.info(this, "depotProductDirectory ", depotProductDirectory);

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

		JFileChooser chooserServerpath = new JFileChooser();
		chooserServerpath.setFileHidingEnabled(false);
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
	}

	protected void evaluateWinProducts() {
		WinProductsRetriever.Context ctx = new WinProductsRetriever.Context();
		ctx.owner = dialog;
		ctx.webDAVClient = webDAVClient;
		ctx.msg = jLabelRetrievalText;
		ctx.options = comboChooseWinProduct;
		ctx.onDone = () -> {
			winProduct = (String) comboChooseWinProduct.getSelectedItem();
			produceTarget();
		};
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
		btnCreateDrivers.addActionListener((ActionEvent actionEvent) -> {
			CommandExecutor executor = new CommandExecutor(configedMain,
					new SingleCommandTemplate("create_driver_links.py", "/var/lib/opsi/depot/"
							+ comboChooseWinProduct.getSelectedItem() + "/create_driver_links.py ",
							"create_driver_links.py"));
			executor.execute();
		});

		JLabel labelTargetPath = Utils.createBoldLabel("CompleteWinProducts.labelTargetPath");

		fieldServerPath = new JTextField();
		fieldServerPath.setEditable(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		fieldDriverPath = new JTextField();
		fieldDriverPath.setEditable(true);
		fieldDriverPath.getDocument().addDocumentListener(new FileNameDocumentListener());

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
		panelButtonGroup.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 1, true),
						new EmptyBorder(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
								Globals.MIN_GAP_SIZE)));

		panelButtonGroup.setLayout(new MigLayout("insets 0, wrap 1", "", "[]0"));

		panelButtonGroup.add(buttonStandard);
		panelButtonGroup.add(buttonPreferred);
		panelButtonGroup.add(buttonNotPreferred);
		panelButtonGroup.add(buttonAdditional);
		panelButtonGroup.add(buttonByAudit);

		panelButtonGroup.add(jLabelByAuditDriverLocationPath, "split 2, gapleft 50, aligny center");
		panelButtonGroup.add(fieldByAuditPath, "gapleft " + Globals.MIN_GAP_SIZE + ", wrap");

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
		serverPathChecked.setSelected(result);

		if (result) {
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("PanelDriverUpload.targetDirectoryExists"),
					Configed.getResourceValue("info"), JOptionPane.INFORMATION_MESSAGE);
			checkFiles();
		}

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
		depot.setText(persistenceController.getDataServices().hostInfoCollections.getConfigServer());
	}

	private void produceTarget() {
		if (fieldServerPath == null) {
			// caution we are not yet initialized
			return;
		}

		winProduct = winProduct == null || "null".equals(winProduct) ? "" : winProduct;
		String result = depotProductDirectory + winProduct + driverDirectory + "/";

		if (buttonByAudit.isSelected()) {
			result = result + byAuditPath + "/";
		}

		fieldServerPath.setText(result);
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
