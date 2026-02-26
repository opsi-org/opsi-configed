/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.event.ActionEvent;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandFileUpload;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import net.miginfocom.swing.MigLayout;

public class PMInstallLocalPanel extends PMInstallPanel {
	private JLabel jLabelUploadFrom;
	private JLabel jLabelUploadTo;
	private JTextField jTextFieldPath;
	private JButton jButtonFileChooser;

	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;
	private CompletionComboButton autocompletion;

	public PMInstallLocalPanel() {
		super();
		autocompletion = new CompletionComboButton(additionalDefaultPaths);
		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelUploadFrom = Utils.createBoldLabel("PMInstallLocalPanel.jLabelLocalFrom");
		jLabelUploadTo = Utils.createBoldLabel("PMInstallLocalPanel.jLabelLocalTo");

		jTextFieldPath = new JTextField();

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();

		SystemFileChooser fileChooser = new SystemFileChooser();
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		fileChooser.setDialogType(SystemFileChooser.OPEN_DIALOG);
		fileChooser.setDialogTitle(Configed.getResourceValue("PMInstallLocalPanel.titleDialogLocalFrom"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("opsi-paket (*.opsi) ", "opsi"));

		jButtonFileChooser = new JButton(Icons.getIntellijIcon("open"));
		jButtonFileChooser.setToolTipText(Configed.getResourceValue("PMInstallLocalPanel.filechooser.tooltip"));
		jButtonFileChooser.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == SystemFileChooser.APPROVE_OPTION) {
				String pathModules = fileChooser.getSelectedFile().getPath();
				jTextFieldPath.setText(pathModules);
			} else {
				jTextFieldPath.setText("");
			}
		});
	}

	private void initLayout() {
		setLayout(new MigLayout("insets 0, fillx", "[grow, fill][]", "[]0[]" + Globals.GAP_SIZE + "[]0[]"));
		add(jLabelUploadFrom, "wrap");
		add(jTextFieldPath, "split 2, growx");
		add(jButtonFileChooser, "wrap");
		add(jLabelUploadTo, "wrap");
		add(jComboBoxAutoCompletion, "split2, growx");
		add(jButtonAutoCompletion, "wrap");
	}

	public SingleCommandFileUpload getCommand() {
		if (jTextFieldPath.getText() == null || jTextFieldPath.getText().isEmpty()) {
			return null;
		}

		SingleCommandFileUpload com1 = new SingleCommandFileUpload();
		com1.setCommand(Configed.getResourceValue("PMInstallLocalPanel.uploadingPackage"));
		com1.setSourceFileName(getFilename(jTextFieldPath.getText()));
		com1.setSourcePath(getPath(jTextFieldPath.getText()));
		com1.setTargetPath((String) jComboBoxAutoCompletion.getSelectedItem());
		com1.setTargetFileName(getFilename(com1.getFullSourcePath()));
		return com1;
	}

	private static String getFilename(String fullpathname) {
		return Paths.get(fullpathname).getFileName().toString();
	}

	private static String getPath(String fullpathname) {
		return Paths.get(fullpathname).getParent().toString() + "/";
	}
}
