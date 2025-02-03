/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public class CurlAuthenticationPanel extends PMInstallPanel {
	private JCheckBox jCheckBoxNeedAuthentication;
	private JLabel labelUser;
	private JTextField jTextFieldUser;
	private JLabel labelPassword;
	private JPasswordField jPasswordField;

	public CurlAuthenticationPanel() {
		super();
		initComponents();
		initLayout();
	}

	private void initComponents() {
		jCheckBoxNeedAuthentication = new JCheckBox(
				Configed.getResourceValue("CurlAuthenticationPanel.needAuthentication"));
		jCheckBoxNeedAuthentication
				.setToolTipText(Configed.getResourceValue("CurlAuthenticationPanel.needAuthentication.tooltip"));
		jCheckBoxNeedAuthentication.addItemListener((ItemEvent itemEvent) -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				open();
			} else {
				close();
			}
		});

		labelUser = new JLabel(Configed.getResourceValue("username"));
		labelUser.setFont(labelUser.getFont().deriveFont(Font.BOLD));
		jTextFieldUser = new JTextField();

		labelPassword = new JLabel(Configed.getResourceValue("password"));
		labelPassword.setFont(labelPassword.getFont().deriveFont(Font.BOLD));
		jPasswordField = new JPasswordField();
	}

	public JCheckBox getCheckBox() {
		return jCheckBoxNeedAuthentication;
	}

	public String getUser() {
		return jTextFieldUser.getText();
	}

	public String getPassword() {
		return new String(jPasswordField.getPassword());
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(labelUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(labelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(labelUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}
}
