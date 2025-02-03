/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent;

public class DeployClientAgentAuthPanel extends JPanel {
	private FlatTextField flatTextFieldUser;
	private FlatPasswordField flatPasswordField;

	private SingleCommandDeployClientAgent commandDeployClientAgent;

	public DeployClientAgentAuthPanel(SingleCommandDeployClientAgent commandDeployClientAgent) {
		this.commandDeployClientAgent = commandDeployClientAgent;
		init();
	}

	private void init() {
		setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 2, true));
		flatTextFieldUser = new FlatTextField();
		flatTextFieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		flatTextFieldUser.getDocument().addDocumentListener(new DocumentListener() {
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

		flatPasswordField = new FlatPasswordField();
		flatPasswordField.setPlaceholderText(Configed.getResourceValue("password"));

		flatPasswordField.getDocument().addDocumentListener(new DocumentListener() {
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
		commandDeployClientAgent.setUser(flatTextFieldUser.getText().trim());
	}

	public void changePassw() {
		commandDeployClientAgent.setPassword(new String(flatPasswordField.getPassword()));
	}

	private void initLayout() {
		GroupLayout winAuthPanelLayout = new GroupLayout(this);
		setLayout(winAuthPanelLayout);

		winAuthPanelLayout.setHorizontalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
								flatTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
						.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
								flatPasswordField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
				.addGap(Globals.GAP_SIZE));

		winAuthPanelLayout.setVerticalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(flatTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(flatPasswordField, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE));
	}
}
