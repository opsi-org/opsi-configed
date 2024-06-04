/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public class CurlAuthenticationPanel extends PMInstallPanel {
	public static final String LBLNEEDAUTH = "lbl_needAuthentication";
	public static final String CBNEEDAUTH = "cb_needAuthentication";

	private JCheckBox jCheckBoxNeedAuthentication;
	private JLabel jLabelNeedAuthentication;
	private FlatTextField flatTextFieldUser;
	private FlatPasswordField flatPasswordField;

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

		flatTextFieldUser = new FlatTextField();
		flatTextFieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		flatPasswordField = new FlatPasswordField();
		flatPasswordField.setPlaceholderText(Configed.getResourceValue("password"));
	}

	public JComponent get(String comp) {
		JComponent result;
		if (comp.equals(LBLNEEDAUTH)) {
			result = jLabelNeedAuthentication;
		} else if (comp.equals(CBNEEDAUTH)) {
			result = jCheckBoxNeedAuthentication;
		} else {
			result = null;
		}
		return result;
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
				.addComponent(flatTextFieldUser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2)
				.addComponent(flatPasswordField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(flatTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(flatPasswordField, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT));
	}
}
