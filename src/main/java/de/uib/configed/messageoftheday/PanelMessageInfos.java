/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.logging.Logging;

public class PanelMessageInfos extends JPanel {
	public enum InfoType {
		DEVICE, USER
	}

	private FMessageOfTheDay caller;

	private InfoType type;

	private JTextArea textArea;
	private JScrollPane areaScrollPane;
	private JTextField dateChooserText;
	protected JButton dateChooserButton;
	protected JButton dateForeverButton;
	private Map<String, String> motdData;

	public PanelMessageInfos(FMessageOfTheDay caller, InfoType type, Map<String, String> msgdata) {
		Logging.debug("PanelMessageInfos type: " + type);
		this.type = type;
		this.motdData = msgdata;
		this.caller = caller;

		initComponents();
		defineLayout();
	}

	public String getText() {
		return textArea.getText();
	}

	public Integer getValidUntil() {
		return 0;
	}

	public void resetData() {
		String text = "";
		if (type == InfoType.USER) {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER);
		} else {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE);
		}

		String date = "";
		if (type == InfoType.USER) {
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL);
		} else {
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL);
		}
		textArea.setText(text);
		dateChooserText.setText(date);
	}

	private void initComponents() {

		textArea = new JTextArea();
		textArea.setRows(5);
		areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		dateChooserText = new JTextField();
		dateChooserText.setEditable(false);
		resetData();

		dateChooserButton = new JButton(Configed.getResourceValue("MessageOfTheDay.dateButton"));
		dateChooserButton.addActionListener(e -> btnChooserClicked());
		dateForeverButton = new JButton(Configed.getResourceValue("MessageOfTheDay.foreverButton"));
		dateForeverButton.addActionListener(e -> btnForeverActionListener());
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				enableSaveOnDataChange();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				enableSaveOnDataChange();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// Plain text components do not fire these events
			}
		});
	}

	private void btnForeverActionListener() {
		dateChooserText.setText("0");
		enableSaveOnDataChange();
	}

	private void btnChooserClicked() {
		dateChooserText.setText("12345");
		enableSaveOnDataChange();
	}

	private void enableSaveOnDataChange() {
		Boolean dataChanged = false;
		if (InfoType.USER == type) {
			dataChanged = !textArea.getText()
					.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER))
					|| !dateChooserText.getText().equals(
							motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL));
		} else {
			dataChanged = !textArea.getText()
					.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE))
					|| !dateChooserText.getText().equals(
							motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL));
		}
		caller.setSaveButtonEnable(dataChanged);
	}

	private void defineLayout() {
		String keyTextAreaLabel = "MessageOfTheDay.device.textAreaLabel";
		if (type == InfoType.USER) {
			keyTextAreaLabel = "MessageOfTheDay.user.textAreaLabel";
		}
		JLabel topicLabel = new JLabel(Configed.getResourceValue(keyTextAreaLabel));
		JLabel dateLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.device.dateLabel"));
		JLabel dateForeverLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.preForeverButtonLabel"));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(areaScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(dateLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(dateChooserText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(dateChooserButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(dateForeverLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(dateForeverButton, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(Globals.GAP_SIZE)
										.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(areaScrollPane, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGroup(layout.createSequentialGroup()
												.addComponent(dateLabel, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(dateChooserText, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(dateChooserButton, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(dateForeverLabel, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE).addComponent(dateForeverButton,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE)

										).addGap(Globals.GAP_SIZE)).addGap(Globals.GAP_SIZE));

		this.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
	}

	@Override
	public void requestFocus() {
		textArea.requestFocus();
	}
}
