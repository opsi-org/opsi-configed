/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;
import net.miginfocom.swing.MigLayout;

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

		labelUser = Utils.createBoldLabel("username");
		jTextFieldUser = new JTextField();

		labelPassword = Utils.createBoldLabel("password");
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
		setLayout(new MigLayout("insets " + Globals.GAP_SIZE + ", fillx, wrap 1", "[grow, fill]", "[]0"));
		add(labelUser);
		add(jTextFieldUser, "growx, gapbottom " + Globals.GAP_SIZE);
		add(labelPassword);
		add(jPasswordField, "growx, gapbottom " + Globals.GAP_SIZE);
	}
}
