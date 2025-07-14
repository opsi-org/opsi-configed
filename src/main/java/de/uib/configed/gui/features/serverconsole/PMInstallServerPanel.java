/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.configed.share.Utils;

public class PMInstallServerPanel extends PMInstallPanel {
	private JLabel jLabelServerDir = new JLabel();
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;
	private CompletionComboButton autocompletion;

	public PMInstallServerPanel(String fullPathToPackage) {
		super();
		autocompletion = new CompletionComboButton(additionalDefaultPaths, ".opsi", fullPathToPackage);

		initComponents();
		setPackagePath(fullPathToPackage);
		initLayout();

		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion.setEnabled(true);
		jComboBoxAutoCompletion.setSelectedItem(workbench);
	}

	public final void setPackagePath(String pPath) {
		if (!(pPath.isEmpty())) {
			jComboBoxAutoCompletion.addItem(pPath);
			jComboBoxAutoCompletion.setSelectedItem(pPath);
		}
	}

	private void initComponents() {
		jLabelServerDir.setText(Configed.getResourceValue("PMInstallServerPanel.jLabelOtherPath"));
		jLabelServerDir.setFont(jLabelServerDir.getFont().deriveFont(java.awt.Font.BOLD));

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.setToolTipText(
				Configed.getResourceValue("PMInstallServerPanel.autocompletion.button_andopsipackage.combo.tooltip"));
		jComboBoxAutoCompletion.setEnabled(true);

		jButtonAutoCompletion = autocompletion.getButton();
		jButtonAutoCompletion
				.setText(Configed.getResourceValue("PMInstallServerPanel.autocompletion.button_andopsipackage"));
		jButtonAutoCompletion.setToolTipText(
				Configed.getResourceValue("PMInstallServerPanel.autocompletion.button_andopsipackage.tooltip"));
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(jLabelServerDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(jLabelServerDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
	}

	public SingleCommandOpsiPackageManagerInstall getCommand() {
		return PMInstallServerPanel
				.getCommand(Utils.getServerPathFromWebDAVPath(autocompletion.getTextField().getText()));
	}

	public static SingleCommandOpsiPackageManagerInstall getCommand(String product) {
		if (product == null || product.isEmpty()) {
			return null;
		}

		SingleCommandOpsiPackageManagerInstall com = new SingleCommandOpsiPackageManagerInstall();
		com.setOpsiproduct(product.replace("\n", ""));

		if (com.checkCommand()) {
			return com;
		} else {
			return null;
		}
	}
}
