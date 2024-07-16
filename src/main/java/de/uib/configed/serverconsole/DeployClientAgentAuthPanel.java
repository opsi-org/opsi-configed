/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.util.List;
import java.util.Map;

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
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.logging.Logging;

public class DeployClientAgentAuthPanel extends JPanel {
	private FlatTextField flatTextFieldUser;
	private FlatPasswordField flatPasswordField;

	private String defaultUser;

	private SingleCommandDeployClientAgent commandDeployClientAgent;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean isGlobalReadOnly = persistenceController.getUserRolesConfigDataService().isGlobalReadOnly();

	public DeployClientAgentAuthPanel(SingleCommandDeployClientAgent commandDeployClientAgent) {
		this.commandDeployClientAgent = commandDeployClientAgent;
		init();
	}

	private void init() {
		getDefaultAuthData();
		setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 2, true));
		flatTextFieldUser = new FlatTextField();
		flatTextFieldUser.setText(defaultUser);
		flatTextFieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		flatTextFieldUser.setEnabled(!isGlobalReadOnly);
		flatTextFieldUser.setEditable(!isGlobalReadOnly);
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
		flatPasswordField.setEnabled(!isGlobalReadOnly);
		flatPasswordField.setEditable(!isGlobalReadOnly);

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

	@SuppressWarnings("unchecked")
	private void getDefaultAuthData() {
		Map<String, Object> configs = persistenceController.getConfigDataService()
				.getHostConfig(persistenceController.getHostInfoCollections().getConfigServer());

		List<Object> resultConfigList = (List<Object>) configs
				.get(UserRolesConfigDataService.KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER);
		if (resultConfigList == null || resultConfigList.isEmpty()) {
			Logging.info(this, "KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER not existing");

			// the config will be created in this run of configed
		} else {
			defaultUser = (String) resultConfigList.get(0);
			Logging.info(this, "KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER ", ((String) resultConfigList.get(0)));
		}

		resultConfigList = (List<Object>) configs.get(UserRolesConfigDataService.KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW);
		if (resultConfigList == null || resultConfigList.isEmpty()) {
			Logging.info(this, "KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW not existing");

			// the config will be created in this run of configed
		} else {
			if (flatPasswordField == null) {
				flatPasswordField = new FlatPasswordField();
			}
			flatPasswordField.setText((String) resultConfigList.get(0));
			Logging.info(this, "key_ssh_shell_active ***confidential***");
		}
	}

	public void changeUser() {
		if (!(flatTextFieldUser.getText().equals(defaultUser))) {
			commandDeployClientAgent.setUser(flatTextFieldUser.getText().trim());
		} else {
			commandDeployClientAgent.setUser("");
		}
	}

	public void changePassw() {
		commandDeployClientAgent.setPassword(new String(flatPasswordField.getPassword()).trim());
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
