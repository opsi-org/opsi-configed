/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
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
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class DeployClientAgentAuthPanel extends JPanel {
	private JLabel jLabelUser = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JTextField jTextFieldUser;
	private JPasswordField jTextFieldPassword;
	private JButton jButtonShowPassword;

	private String defaultUser;

	private boolean showingPassowrd;

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
		jLabelUser.setText(Configed.getResourceValue("DeployClientAgentAuthPanel.jLabelUser"));
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

		jLabelPassword.setText(Configed.getResourceValue("DeployClientAgentAuthPanel.jLabelPassword"));
		jTextFieldPassword = new JPasswordField("nt123", 15);
		jTextFieldPassword.setEnabled(!isGlobalReadOnly);
		jTextFieldPassword.setEditable(!isGlobalReadOnly);
		jTextFieldPassword.setEchoChar('*');

		jButtonShowPassword = new JButton(Utils.createImageIcon("images/eye_blue_open.png", ""));

		jButtonShowPassword.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		jButtonShowPassword
				.setToolTipText(Configed.getResourceValue("DeployClientAgentAuthPanel.showPassword.tooltip"));
		jButtonShowPassword.setEnabled(!isGlobalReadOnly);
		jButtonShowPassword.addActionListener(actionEvent -> changeEchoChar());

		jTextFieldPassword.getDocument().addDocumentListener(new DocumentListener() {
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
			if (jTextFieldPassword == null) {
				jTextFieldPassword = new JPasswordField("", 15);
				jTextFieldPassword.setEchoChar('*');
			}
			jTextFieldPassword.setText((String) resultConfigList.get(0));
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
		commandDeployClientAgent.setPassw(new String(jTextFieldPassword.getPassword()).trim());
	}

	private void changeEchoChar() {
		if (showingPassowrd) {
			showingPassowrd = false;
			jTextFieldPassword.setEchoChar('*');
		} else {
			showingPassowrd = true;
			jTextFieldPassword.setEchoChar((char) 0);
		}
	}

	private void initLayout() {
		GroupLayout winAuthPanelLayout = new GroupLayout(this);
		setLayout(winAuthPanelLayout);

		winAuthPanelLayout
				.setHorizontalGroup(
						winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
								.addGroup(
										winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addGroup(winAuthPanelLayout.createSequentialGroup()
														.addGap(Globals.GAP_SIZE)
														.addComponent(jLabelUser, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jTextFieldUser, GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
														.addGap(Globals.GAP_SIZE).addGap(Globals.ICON_WIDTH)
														.addGap(Globals.GAP_SIZE))
												.addGroup(winAuthPanelLayout.createSequentialGroup()
														.addGap(Globals.GAP_SIZE)
														.addComponent(jLabelPassword, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jTextFieldPassword, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Short.MAX_VALUE)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jButtonShowPassword, Globals.ICON_WIDTH,
																Globals.ICON_WIDTH, Globals.ICON_WIDTH)
														.addGap(Globals.GAP_SIZE)))
								.addGap(Globals.GAP_SIZE));

		winAuthPanelLayout
				.setVerticalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jButtonShowPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE));

	}
}
