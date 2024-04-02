/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ActionEvent;
import java.nio.file.Paths;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.SingleCommandFileUpload;
import utils.Utils;

public class PMInstallLocalPanel extends PMInstallPanel {

	private JLabel jLabelUploadFrom;
	private JLabel jLabelUploadTo;
	private JTextField jTextFieldPath;
	private JButton jButtonFileChooser;

	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;
	private CompletionComboButton autocompletion;

	public PMInstallLocalPanel(ConfigedMain configedMain) {
		super();
		autocompletion = new CompletionComboButton(additionalDefaultPaths);
		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelUploadFrom = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalFrom"));
		jLabelUploadTo = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalTo"));
		jTextFieldPath = new JTextField();
		jTextFieldPath.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooser.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooser.setDialogTitle(Globals.APPNAME);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("opsi-paket (*.opsi) ", "opsi");
		jFileChooser.setFileFilter(filter);

		jButtonFileChooser = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);
		jButtonFileChooser.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.filechooser.tooltip"));
		jButtonFileChooser.addActionListener((ActionEvent actionEvent) -> {
			int returnVal = jFileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathModules = jFileChooser.getSelectedFile().getPath();
				jTextFieldPath.setText(pathModules);
			} else {
				jTextFieldPath.setText("");
			}
		});
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addGroup(layout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelUploadFrom, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(jTextFieldPath, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(jButtonFileChooser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelUploadTo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jButtonAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(jLabelUploadFrom, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelUploadTo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(jTextFieldPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(jButtonFileChooser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(2 * Globals.GAP_SIZE));
	}

	public SingleCommandFileUpload getCommand() {
		if (jTextFieldPath.getText() == null || jTextFieldPath.getText().isEmpty()) {
			return null;
		}

		SingleCommandFileUpload com1 = new SingleCommandFileUpload("PackegeUpload");
		com1.setCommand(Configed.getResourceValue("PMInstallLocalPanel.uploadingPackage"));
		com1.setSourceFilename(getFilename(jTextFieldPath.getText()));
		com1.setFullSourcePath(jTextFieldPath.getText());
		com1.setTargetPath((String) jComboBoxAutoCompletion.getSelectedItem());
		com1.setTargetFilename(getFilename(com1.getFullSourcePath()));
		return com1;
	}

	private static String getFilename(String fullpathname) {
		return Paths.get(fullpathname).getFileName().toString();
	}
}
