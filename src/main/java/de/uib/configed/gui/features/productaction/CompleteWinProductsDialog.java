/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productaction;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.Icons;
import de.uib.configed.share.NameProducer;
import de.uib.configed.share.Utils;
import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.WinProductsRetriever;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CompleteWinProductsDialog implements NameProducer {
	private String winProduct = "";

	private String depotProductDirectory;

	private JLabel depot;
	private JComboBox<String> comboChooseWinProduct;
	private JTextField fieldTargetPath;

	private JButton buttonCallSelectFolderWinPE;
	private JButton buttonCallSelectFolderInstallFiles;
	private JTextField fieldProductKey;

	private JTextField fieldPathWinPE;
	private JTextField fieldPathInstallFiles;

	private JButton buttonCallExecute;

	private JFileChooser chooserFolder;

	private JDialog dialog;

	private JLabel jLabelRetrievalText = new JLabel(
			Configed.getResourceValue("PanelDriverUpload.retrievingWinProducts"));

	private WebDAVClient webDAVClient;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public CompleteWinProductsDialog() {
		defineChoosers();
		initComponentsForNameProducer();

		String webDavPath = persistenceController.getDataServices().hostInfoCollections.getConfigServerWebDavPath();
		depotProductDirectory = webDavPath != null && !webDavPath.isEmpty() ? webDavPath : "depot/";
		if (!depotProductDirectory.endsWith("/")) {
			depotProductDirectory += "/";
		}

		webDAVClient = new WebDAVClient();

		initComponents();

		JPanel panel = initLayout();

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { buttonCallExecute, Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("FProductAction.title"));
		dialog.setModal(false);

		evaluateWinProducts();

		persistenceController.registerPanelCompleteWinProducts(this);
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	public final void evaluateWinProducts() {
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

	private void defineChoosers() {
		chooserFolder = new JFileChooser();
		chooserFolder.setFileHidingEnabled(false);
		chooserFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		SwingUtilities.updateComponentTreeUI(chooserFolder);

		chooserFolder.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserFolder.setDialogTitle(Configed.getResourceValue("CompleteWinProducts.chooser"));

		depot = new JLabel(persistenceController.getDataServices().hostInfoCollections.getConfigServer());

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.addActionListener((ActionEvent actionEvent) -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			produceTarget();
		});
	}

	private void checkButtonCallExecute() {
		if (buttonCallExecute == null) {
			return;
		}

		buttonCallExecute.setEnabled(webDAVClient.existsAndIsDirectory(fieldTargetPath.getText()));
	}

	private void produceTarget() {
		if (fieldTargetPath != null) {
			winProduct = winProduct == null || "null".equals(winProduct) ? "" : winProduct;
			String targetPath = depotProductDirectory + winProduct;
			fieldTargetPath.setText(targetPath.endsWith("/") ? targetPath : (targetPath + "/"));
			checkButtonCallExecute();
		}
	}

	// implements NameProducer
	@Override
	public String produceName() {
		Logging.info(this, "produceName ? fieldTargetPath , depotProductDirectory ", fieldTargetPath, " , ",
				depotProductDirectory);
		if (fieldTargetPath == null || fieldTargetPath.getText().isEmpty()
				|| fieldTargetPath.getText().startsWith(depotProductDirectory)) {
			return depotProductDirectory;
		}

		return fieldTargetPath.getText();
	}

	@Override
	public String getDefaultName() {
		return depotProductDirectory;
	}

	private void initComponentsForNameProducer() {
		fieldTargetPath = new JTextField();
		fieldTargetPath.getDocument().addDocumentListener(Utils.onDocumentChange(this::checkButtonCallExecute));

		fieldPathWinPE = new JTextField();
	}

	private void initComponents() {
		fieldProductKey = new JTextField();

		buttonCallSelectFolderWinPE = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectFolderWinPE.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(dialog);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathWinPE = chooserFolder.getSelectedFile().getPath();
				fieldPathWinPE.setText(pathWinPE);
				fieldPathWinPE.setCaretPosition(pathWinPE.length());
			} else {
				fieldPathWinPE.setText("");
			}
		});

		buttonCallSelectFolderInstallFiles = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectFolderInstallFiles
				.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderInstallFiles"));

		fieldPathInstallFiles = new JTextField();

		buttonCallSelectFolderInstallFiles.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = chooserFolder.showOpenDialog(dialog);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserFolder.getSelectedFile().getPath();
				fieldPathInstallFiles.setText(pathInstallFiles);
				fieldPathInstallFiles.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldPathInstallFiles.setText("");
			}
		});

		buttonCallExecute = new JButton(Configed.getResourceValue("CompleteWinProducts.execute"));

		buttonCallExecute.setEnabled(false);

		buttonCallExecute.addActionListener(actionEvent -> execute());
	}

	private void execute() {
		WinProductUploadWorker.Context ctx = new WinProductUploadWorker.Context();
		ctx.owner = dialog;
		ctx.executeButton = buttonCallExecute;
		ctx.targetDirectory = fieldTargetPath.getText().trim();
		ctx.pathWinPE = fieldPathWinPE.getText().trim();
		ctx.pathInstallFiles = fieldPathInstallFiles.getText().trim();
		ctx.productKey = fieldProductKey.getText().trim();
		ctx.depot = depot.getText();
		ctx.winProduct = winProduct;
		ctx.webDAVClient = webDAVClient;
		new WinProductUploadWorker(ctx).execute();
	}

	private JPanel initLayout() {
		JLabel topicLabel = Utils.createBoldLabel("CompleteWinProducts.topic");
		JLabel labelServer = Utils.createBoldLabel("CompleteWinProducts.labelServer");
		JLabel labelWinProduct = Utils.createBoldLabel("CompleteWinProducts.labelWinProduct");
		JLabel labelFolderWinPE = Utils.createBoldLabel("CompleteWinProducts.labelFolderWinPE");
		JLabel labelFolderInstallFiles = Utils.createBoldLabel("CompleteWinProducts.labelFolderInstallFiles");
		JLabel labelTargetPath = Utils.createBoldLabel("CompleteWinProducts.labelTargetPath");
		JLabel labelProductKey = Utils.createBoldLabel("CompleteWinProducts.labelProductKey");

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		panel.add(topicLabel, "gapbottom " + Globals.GAP_SIZE);

		panel.add(labelServer);
		panel.add(depot, "gapbottom " + Globals.GAP_SIZE);

		panel.add(labelWinProduct);
		panel.add(comboChooseWinProduct, "growx, gapbottom " + Globals.GAP_SIZE);

		panel.add(jLabelRetrievalText, "hidemode 3");

		panel.add(labelFolderWinPE);
		panel.add(fieldPathWinPE, "growx, split 2");
		panel.add(buttonCallSelectFolderWinPE, "align center, gapbottom " + Globals.GAP_SIZE + ", wrap");

		panel.add(labelFolderInstallFiles);
		panel.add(fieldPathInstallFiles, "growx, split 2");
		panel.add(buttonCallSelectFolderInstallFiles, "align center, gapbottom " + Globals.GAP_SIZE + ", wrap");

		panel.add(labelTargetPath);
		panel.add(fieldTargetPath, "growx, gapbottom " + Globals.GAP_SIZE);

		panel.add(labelProductKey);
		panel.add(fieldProductKey, "growx");

		return panel;
	}
}
