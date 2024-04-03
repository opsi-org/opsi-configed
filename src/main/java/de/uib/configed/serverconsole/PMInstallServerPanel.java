/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.SingleCommandOpsiPackageManagerInstall;

public class PMInstallServerPanel extends PMInstallPanel {
	private JLabel jLabelServerDir = new JLabel();
	private JLabel jLabelOpsiProduct = new JLabel();
	private JTextField jTextFieldProduct;

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
		jLabelOpsiProduct.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOtherPath"));
		jTextFieldProduct = autocompletion.getTextField();

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.setToolTipText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage.combo.tooltip"));

		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();
		jButtonAutoCompletion.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage"));
		jButtonAutoCompletion.setToolTipText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage.tooltip"));
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addGroup(layout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelServerDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
						Globals.BUTTON_HEIGHT)
				.addComponent(jButtonAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
						Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelOpsiProduct, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jTextFieldProduct, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(jLabelServerDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup().addComponent(jLabelOpsiProduct,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup().addComponent(jTextFieldProduct, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
				.addGap(2 * Globals.GAP_SIZE));
	}

	public SingleCommandOpsiPackageManagerInstall getCommand() {
		return PMInstallServerPanel.getCommand(jTextFieldProduct.getText());
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
