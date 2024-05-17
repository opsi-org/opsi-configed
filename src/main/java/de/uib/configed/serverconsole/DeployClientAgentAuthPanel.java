/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.logging.Logging;

public class DeployClientAgentAuthPanel extends JPanel {
	private JLabel jLabelUser = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JTextField jTextFieldUser;
	private JPasswordField jPasswordField;

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
		jLabelUser.setText(Configed.getResourceValue("username"));
		jTextFieldUser = new JTextField(defaultUser);
		jTextFieldUser.setToolTipText(Configed.getResourceValue("DeployClientAgentAuthPanel.tooltip.tf_user"));
		jTextFieldUser.setEnabled(!isGlobalReadOnly);
		jTextFieldUser.setEditable(!isGlobalReadOnly);
		jTextFieldUser.getDocument().addDocumentListener(new DocumentListener() {
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

		jLabelPassword.setText(Configed.getResourceValue("password"));
		jPasswordField = new JPasswordField();
		jPasswordField.setEnabled(!isGlobalReadOnly);
		jPasswordField.setEditable(!isGlobalReadOnly);

		jPasswordField.getDocument().addDocumentListener(new DocumentListener() {
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
			Logging.info(this, "KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER " + ((String) resultConfigList.get(0)));
		}

		resultConfigList = (List<Object>) configs.get(UserRolesConfigDataService.KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW);
		if (resultConfigList == null || resultConfigList.isEmpty()) {
			Logging.info(this, "KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW not existing");

			// the config will be created in this run of configed
		} else {
			if (jPasswordField == null) {
				jPasswordField = new JPasswordField(15);
			}
			jPasswordField.setText((String) resultConfigList.get(0));
			Logging.info(this, "key_ssh_shell_active ***confidential***");
		}
	}

	public void changeUser() {
		if (!(jTextFieldUser.getText().equals(defaultUser))) {
			commandDeployClientAgent.setUser(jTextFieldUser.getText().trim());
		} else {
			commandDeployClientAgent.setUser("");
		}
	}

	public void changePassw() {
		commandDeployClientAgent.setPassw(new String(jPasswordField.getPassword()).trim());
	}

	private void initLayout() {
		GroupLayout winAuthPanelLayout = new GroupLayout(this);
		setLayout(winAuthPanelLayout);

		winAuthPanelLayout.setHorizontalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
								jTextFieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
						.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
								jPasswordField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
				.addGap(Globals.GAP_SIZE));

		winAuthPanelLayout.setVerticalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jPasswordField, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE));
	}
}
