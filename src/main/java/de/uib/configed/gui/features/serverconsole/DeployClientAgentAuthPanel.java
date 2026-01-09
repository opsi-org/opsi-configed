/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandDeployClientAgent;
import net.miginfocom.swing.MigLayout;

public class DeployClientAgentAuthPanel extends JPanel {
	private JLabel labelUser;
	private JTextField textFieldUser;

	private JLabel labelPassword;
	private JPasswordField passwordField;

	private SingleCommandDeployClientAgent commandDeployClientAgent;

	public DeployClientAgentAuthPanel(SingleCommandDeployClientAgent commandDeployClientAgent) {
		this.commandDeployClientAgent = commandDeployClientAgent;
		init();
	}

	private void init() {
		labelUser = new JLabel(Configed.getResourceValue("username"));
		labelUser.setFont(labelUser.getFont().deriveFont(Font.BOLD));

		textFieldUser = new FlatTextField();
		textFieldUser.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changeUser();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changeUser();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changeUser();
			}
		});

		labelPassword = new JLabel(Configed.getResourceValue("password"));
		labelPassword.setFont(labelPassword.getFont().deriveFont(Font.BOLD));
		passwordField = new FlatPasswordField();

		passwordField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changePassw();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changePassw();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changePassw();
			}
		});

		initLayout();
	}

	public void changeUser() {
		commandDeployClientAgent.setUser(textFieldUser.getText().trim());
	}

	public void changePassw() {
		commandDeployClientAgent.setPassword(new String(passwordField.getPassword()));
	}

	private void initLayout() {
		setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "[grow, fill]", "[]0"));
		add(labelUser);
		add(textFieldUser, "growx, gapbottom " + Globals.GAP_SIZE);
		add(labelPassword);
		add(passwordField, "growx, gapbottom " + Globals.GAP_SIZE);
	}
}
