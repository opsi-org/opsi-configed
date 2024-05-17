/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Dimension;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public class CurlAuthenticationPanel extends PMInstallPanel {
	public static final String LBLUSER = "lbl_user";
	public static final String LBLNEEDAUTH = "lbl_needAuthentication";
	public static final String CBNEEDAUTH = "cb_needAuthentication";

	private JCheckBox jCheckBoxNeedAuthentication;
	private JLabel jLabeluser;
	private JLabel jLabelNeedAuthentication;
	private JTextField jTextFieldUser;
	private JPasswordField jPasswordField;
	private JLabel jLabelPassword;

	public CurlAuthenticationPanel() {
		super();
		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelNeedAuthentication = new JLabel(Configed.getResourceValue("CurlAuthenticationPanel.needAuthentication"));
		jLabelNeedAuthentication
				.setToolTipText(Configed.getResourceValue("CurlAuthenticationPanel.needAuthentication.tooltip"));
		jCheckBoxNeedAuthentication = new JCheckBox();
		jCheckBoxNeedAuthentication.addItemListener((ItemEvent itemEvent) -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				open();
			} else {
				close();
			}
		});
		jLabeluser = new JLabel(Configed.getResourceValue("CurlAuthenticationPanel.username"));
		jLabelPassword = new JLabel(Configed.getResourceValue("CurlAuthenticationPanel.password"));
		jTextFieldUser = new JTextField();
		jPasswordField = new JPasswordField();
		jPasswordField.setEchoChar('*');
	}

	public void setLabelSizes(int width, int height) {
		setLabelSizes(new Dimension(width, height));
	}

	private void setLabelSizes(Dimension size) {
		jLabeluser.setPreferredSize(size);
		jLabelPassword.setPreferredSize(size);
	}

	public JComponent get(String comp) {
		JComponent result;
		if (comp.equals(LBLNEEDAUTH)) {
			result = jLabelNeedAuthentication;
		} else if (comp.equals(CBNEEDAUTH)) {
			result = jCheckBoxNeedAuthentication;
		} else if (comp.equals(LBLUSER)) {
			result = jLabeluser;
		} else {
			result = null;
		}
		return result;
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
		layout.setHorizontalGroup(layout.createSequentialGroup()

				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(jLabeluser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE))
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE)))
				.addGroup(layout.createParallelGroup()
						.addComponent(jTextFieldUser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(jPasswordField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabeluser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE).addComponent(jPasswordField, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)));
	}
}
