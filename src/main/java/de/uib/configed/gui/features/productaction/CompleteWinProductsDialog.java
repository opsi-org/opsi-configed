/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productaction;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.util.SystemFileChooser;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.webdav.WebDAVClient;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.WinProductsRetriever;
import de.uib.configed.gui.share.icons.Icons;
import net.miginfocom.swing.MigLayout;

public class CompleteWinProductsDialog {
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

	private SystemFileChooser chooserFolder;

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
		chooserFolder = new SystemFileChooser();
		chooserFolder.setFileHidingEnabled(false);
		chooserFolder.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
		chooserFolder.setDialogType(SystemFileChooser.OPEN_DIALOG);
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

	private void initComponentsForNameProducer() {
		fieldTargetPath = new JTextField();
		fieldTargetPath.getDocument().addDocumentListener(SwingUtils.onDocumentChange(this::checkButtonCallExecute));

		fieldPathWinPE = new JTextField();
	}

	private void initComponents() {
		fieldProductKey = new JTextField();

		buttonCallSelectFolderWinPE = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectFolderWinPE.setToolTipText(Configed.getResourceValue("CompleteWinProducts.chooserFolderPE"));

		buttonCallSelectFolderWinPE.addActionListener((ActionEvent actionEvent) -> {
			if (chooserFolder.showOpenDialog(dialog) == SystemFileChooser.APPROVE_OPTION) {
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
			if (chooserFolder.showOpenDialog(dialog) == SystemFileChooser.APPROVE_OPTION) {
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
		JLabel topicLabel = SwingUtils.createBoldLabel("CompleteWinProducts.topic");
		JLabel labelServer = SwingUtils.createBoldLabel("CompleteWinProducts.labelServer");
		JLabel labelWinProduct = SwingUtils.createBoldLabel("CompleteWinProducts.labelWinProduct");
		JLabel labelFolderWinPE = SwingUtils.createBoldLabel("CompleteWinProducts.labelFolderWinPE");
		JLabel labelFolderInstallFiles = SwingUtils.createBoldLabel("CompleteWinProducts.labelFolderInstallFiles");
		JLabel labelTargetPath = SwingUtils.createBoldLabel("CompleteWinProducts.labelTargetPath");
		JLabel labelProductKey = SwingUtils.createBoldLabel("CompleteWinProducts.labelProductKey");

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
		panel.add(buttonCallSelectFolderWinPE, "align center, wrap");

		panel.add(labelFolderInstallFiles, "gaptop " + Globals.GAP_SIZE);
		panel.add(fieldPathInstallFiles, "growx, split 2");
		panel.add(buttonCallSelectFolderInstallFiles, "align center, wrap");

		panel.add(labelTargetPath, "gaptop " + Globals.GAP_SIZE);
		panel.add(fieldTargetPath, "growx");

		panel.add(labelProductKey, "gaptop " + Globals.GAP_SIZE);
		panel.add(fieldProductKey, "growx");

		return panel;
	}
}
