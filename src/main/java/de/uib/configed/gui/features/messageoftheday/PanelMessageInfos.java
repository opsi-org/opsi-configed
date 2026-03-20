/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/**
 * Represents the panel for the message of the day configuration. This panel
 * contains a text area for the message and a date chooser for the "valid
 * until" date as well as a button to set the valid date to "forever". This
 * panel is used in the {@link MessageOfTheDayDialog} dialog.
 */
package de.uib.configed.gui.features.messageoftheday;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.TextMarkdownPane;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelMessageInfos extends JPanel implements IDateTimePickerCaller {
	public enum InfoType {
		DEVICE, USER
	}

	private InfoType type;
	private String date;
	private JTextField dateChooserText = new JTextField();
	private JTextArea textArea;
	private TextMarkdownPane markdownPreview;
	private JScrollPane areaScrollPane;
	private JScrollPane areaScrollPaneMD;
	private DateTimePickerWrapper dateTimePicker;
	private Map<String, String> motdData;
	private JRadioButton dateChooserButton;
	private JRadioButton infiniteDateChooserButton;
	private JLabel topicLabel;

	public PanelMessageInfos(InfoType type, Map<String, String> msgdata, boolean disabled) {
		Logging.debug("PanelMessageInfos type: ", type);
		this.type = type;
		this.motdData = msgdata;

		initComponents();
		defineLayout();
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			disableComponents(true);
		} else {
			disableComponents(disabled);
		}
	}

	public void setShowPreview(boolean showPreview) {
		if (areaScrollPaneMD != null) {
			areaScrollPaneMD.setVisible(showPreview);
			defineLayout();
			this.repaint();
		}
	}

	public void setDataMap(Map<String, String> data) {
		Logging.debug("PanelMessageInfos setDataMap");
		this.motdData = data;
	}

	public String getText() {
		Logging.debug("PanelMessageInfos", this.type, "getText", textArea.getText());
		return textArea.getText();
	}

	public String getValidUntil() {
		Logging.debug("PanelMessageInfos", this.type, "getValidUntil", date);
		return date;
	}

	private void disableComponents(boolean disabled) {
		Logging.debug("PanelMessageInfos setDisabled: ", disabled);
		textArea.setEnabled(!disabled);
		dateChooserButton.setEnabled(!disabled);
		infiniteDateChooserButton.setEnabled(!disabled);
		dateTimePicker.setEnabled(!disabled);
		dateChooserText.setEnabled(!disabled);

		String keyTextAreaLabel = "MessageOfTheDay.device.textAreaLabel";
		if (type == InfoType.USER) {
			keyTextAreaLabel = "MessageOfTheDay.user.textAreaLabel";
		}
		if (disabled) {
			topicLabel.setText(String.format("%s %s", Configed.getResourceValue(keyTextAreaLabel),
					Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden")));
		} else {
			topicLabel.setText(Configed.getResourceValue(keyTextAreaLabel));
		}
	}

	public void resetData() {
		String text;
		if (type == InfoType.USER) {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER);
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL);
		} else {
			text = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE);
			date = motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL);
		}
		textArea.setText(text);
		markdownPreview.setText(text != null ? text : "");
		if (date == null) {
			date = "0";
		}
		boolean emptyOrZero = date.isEmpty() || "0".equals(date);
		Logging.debug("PanelMessageInfos resetData ", type, " date '", date, "' emptyOrZero: ", emptyOrZero);
		if (emptyOrZero) {
			Logging.debug("PanelMessageInfos resetData 0: ", 0);
			selectInfiniteDateOption();
		} else {
			selectDateOption();
		}
	}

	private void selectDateOption() {
		Logging.debug("PanelMessageInfos selectDateOption: get date from dateTimePicker");
		dateChooserButton.setSelected(true);
		dateTimePicker.setEnabled(true);
		infiniteDateChooserButton.setSelected(false);
		// get date from datetimepicker
		if (date == null) {
			date = "0";
		}
		dateTimePicker.setDateTimeValue(Long.valueOf(date));
	}

	private void selectInfiniteDateOption() {
		Logging.debug("PanelMessageInfos selectInfiniteDateOption");
		dateChooserButton.setSelected(false);
		dateTimePicker.setEnabled(false);
		infiniteDateChooserButton.setSelected(true);
		dateTimePicker.setDateTimeValue(0);
		date = "0";
		dateChooserText.setText("0");
	}

	private void initComponents() {
		Logging.debug("PanelMessageInfos initComponents");
		Dimension minWidthTxtDate = new Dimension(200, 20);
		this.dateChooserText.setMinimumSize(minWidthTxtDate);
		textArea = new JTextArea();
		textArea.setRows(5);
		areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setMinimumSize(new Dimension(200, 100));

		markdownPreview = new TextMarkdownPane();
		markdownPreview.setText(textArea.getText());
		areaScrollPaneMD = new JScrollPane(markdownPreview);
		areaScrollPaneMD.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPaneMD.setMinimumSize(new Dimension(200, 100));

		dateTimePicker = new DateTimePickerWrapper(this);
		dateChooserButton = new JRadioButton();
		infiniteDateChooserButton = new JRadioButton();

		dateChooserButton.setText(Configed.getResourceValue("MessageOfTheDay.specificDateOption"));
		infiniteDateChooserButton.setText(Configed.getResourceValue("MessageOfTheDay.infiniteDateOption"));

		ButtonGroup group = new ButtonGroup();
		group.add(dateChooserButton);
		group.add(infiniteDateChooserButton);

		// default // init datetimepicker
		selectDateOption();
		resetData();
		dateChooserButton.addItemListener((ItemEvent e) -> {
			if (dateChooserButton.isSelected()) {
				selectDateOption();
			} else {
				selectInfiniteDateOption();
			}
		});

		textArea.getDocument()
				.addDocumentListener(SwingUtils.onDocumentChange(() -> markdownPreview.setText(textArea.getText())));
	}

	private void defineLayout() {
		Logging.debug("PanelMessageInfos defineLayout");
		String keyTextAreaLabel = "MessageOfTheDay.device.textAreaLabel";
		if (type == InfoType.USER) {
			keyTextAreaLabel = "MessageOfTheDay.user.textAreaLabel";
		}
		topicLabel = new JLabel(Configed.getResourceValue(keyTextAreaLabel));
		JLabel dateLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.device.dateLabel"));
		JLabel dateForeverLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.preForeverButtonLabel"));

		setLayout(new MigLayout("insets " + Globals.GAP_SIZE + ", fill", "", "[pref!][grow, fill][pref!]0"));
		add(topicLabel, "wrap");
		add(areaScrollPane, "split 2, grow");
		add(areaScrollPaneMD, "grow, wrap");
		add(dateLabel, "split 5");
		add(dateChooserButton);
		add(dateTimePicker);
		add(dateForeverLabel, "gapleft " + Globals.GAP_SIZE);
		add(infiniteDateChooserButton, "wrap, gapbottom " + Globals.GAP_SIZE);

		this.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
	}

	public void dataChanged(LocalDateTime datetime) {
		if (datetime == null) {
			Logging.debug("PanelMessageInfos dataChanged: null");
			this.date = "0";
			return;
		}
		Logging.debug("PanelMessageInfos dataChanged: ", datetime);
		long unixTime = datetime.atZone(DateTimePicker.ZONEID).toEpochSecond();

		// sets the date in data
		this.date = Long.toString(unixTime);
	}

	@Override
	public void requestFocus() {
		Logging.debug("PanelMessageInfos requestFocus");
		textArea.requestFocus();
	}
}
