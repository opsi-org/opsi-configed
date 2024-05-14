/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.logging.Logging;

public class PanelMessageInfos extends JPanel {
	public enum InfoType {
		DEVICE, USER
	}

	private InfoType type;

	private JTextArea textArea;
	private JTextField dateChooser;
	private Map<String, String> motdData;

	public PanelMessageInfos(InfoType type, Map<String, String> msgdata) {
		Logging.debug("PanelMessageInfos type: " + type);
		this.type = type;
		this.motdData = msgdata;

		initComponents();
		defineLayout();
	}

	public String getText() {
		return textArea.getText();
	}

	public Integer getValidUntil() {
		return 0;
	}

	private void initComponents() {
		textArea = new JTextArea();
		textArea.setRows(5);
		String text = "";
		if (type == InfoType.USER) {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER);
		} else {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE);
		}
		textArea.setText(text);

		String date = "";
		if (type == InfoType.USER) {
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL);
		} else {
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL);
		}

		// TODO: show date picker
		// datePicker = new DatePicker() 
		dateChooser = new JTextField(date);
	}

	private void defineLayout() {
		String keyTextAreaLabel = "MessageOfTheDay.device.textAreaLabel";
		String keyTextDate = "MessageOfTheDay.device.dateLabel";
		if (type == InfoType.USER) {
			keyTextAreaLabel = "MessageOfTheDay.user.textAreaLabel";
		}
		JLabel topicLabel = new JLabel(Configed.getResourceValue(keyTextAreaLabel));
		JLabel dateLabel = new JLabel(Configed.getResourceValue(keyTextDate));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(textArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(dateLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(dateChooser, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(Globals.GAP_SIZE)
						.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(textArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(dateLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE).addComponent(dateChooser, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

						).addGap(Globals.GAP_SIZE)).addGap(Globals.GAP_SIZE)

		// .addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE)
		// .addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 2, Short.MAX_VALUE)
		);

		this.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
	}
}
