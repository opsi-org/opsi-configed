/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public class CurlAuthenticationPanel extends PMInstallPanel {
	private JCheckBox jCheckBoxNeedAuthentication;
	private FlatTextField flatTextFieldUser;
	private FlatPasswordField flatPasswordField;

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

		flatTextFieldUser = new FlatTextField();
		flatTextFieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		flatPasswordField = new FlatPasswordField();
		flatPasswordField.setPlaceholderText(Configed.getResourceValue("password"));
	}

	public JCheckBox getCheckBox() {
		return jCheckBoxNeedAuthentication;
	}

	public String getUser() {
		return flatTextFieldUser.getText();
	}

	public String getPassword() {
		return new String(flatPasswordField.getPassword());
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(flatTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(flatPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(flatTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(flatPasswordField, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}
}
