/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;

public final class HealthCheckSettingsDialog {
	private static final int TEXT_LABE_WIDTH = 200;

	private static final String ZERO_HOUR_STRING = "00:00:00";

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private FSelectionList selectedHostList;
	private JTextField startTime;
	private JTextField endTime;
	private JCheckBox enabled;

	public void showHealthCheckSettings() {
		JOptionPane jOptionPane = new JOptionPane(createOptionsPanel());
		jOptionPane.setOptions(new Object[] { "save", "Cancel" });
		JDialog dialog = jOptionPane.createDialog(ConfigedMain.getMainFrame(), "title");
		dialog.setVisible(true);

		if (jOptionPane.getValue() == "save") {
			save();
		}
	}

	private JPanel createOptionsPanel() {
		JLabel label = new JLabel("Selected Hosts");

		JTextField selectedHosts = new JTextField();
		selectedHosts.setEditable(false);
		selectedHosts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				openSelectionDialog(selectedHosts);
			}
		});

		JLabel labelActive = new JLabel("Active");
		enabled = new JCheckBox((String) null, true);

		JLabel labelStart = new JLabel("Start Time");
		startTime = new JTextField();
		startTime.setEditable(false);
		startTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (enabled.isSelected()) {
					openDateSelectionDialog(startTime);
				}
			}
		});

		JLabel labelEnd = new JLabel("End Time");
		endTime = new JTextField();
		endTime.setEditable(false);
		endTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (enabled.isSelected()) {
					openDateSelectionDialog(endTime);
				}
			}
		});

		enabled.addItemListener((ItemEvent event) -> {
			labelStart.setEnabled(enabled.isSelected());
			startTime.setEnabled(enabled.isSelected());
			labelEnd.setEnabled(enabled.isSelected());
			endTime.setText(null);

			endTime.setEnabled(enabled.isSelected());
			if (!enabled.isSelected()) {
				startTime.setText(null);
				endTime.setText(null);
			}
		});

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(label).addComponent(selectedHosts))
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(layout.createParallelGroup().addComponent(labelActive).addComponent(enabled))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(labelStart).addComponent(startTime))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(labelEnd).addComponent(endTime)));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(label)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(selectedHosts, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelActive)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(enabled, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelStart)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(startTime, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelEnd)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(endTime, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH)));

		return panel;
	}

	private void openSelectionDialog(JTextField selectedHosts) {
		Logging.info(this, "openSelectionDialog for health check settings");

		if (selectedHostList == null) {
			selectedHostList = new FSelectionList(null, "title", true, new String[] { "ok", "cancel" }, 500, 300);
			selectedHostList.enableMultiSelection();
			List<String> hostNames = new ArrayList<>(
					persistenceController.getHostInfoCollections().getDepotNamesList());
			hostNames.addAll(persistenceController.getHostInfoCollections().getOpsiHostNames());

			selectedHostList.setListData(hostNames);
		}
		List<String> previousSelection = selectedHostList.getSelectedValues();
		selectedHostList.setVisible(true);

		if (selectedHostList.getResult() == 1) {
			selectedHosts.setText(Utils.getListStringRepresentation(selectedHostList.getSelectedValues(), 5));
		} else {
			selectedHostList.setPreviousSelectionValues(previousSelection);
		}
	}

	private void openDateSelectionDialog(JTextField startTime) {
		Logging.info(this, "openDateSelectionDialog for health check settings");

		JFXPanel jfxPanel = new JFXPanel();
		// We need to set implicit exit to false to prevent a Nullpointerexception
		// when opening a dialog for the second time
		Platform.setImplicitExit(false);

		DatePicker datePicker = createDatePicker(startTime.getText(), jfxPanel);
		showEditor(jfxPanel, datePicker, startTime);
	}

	public static DatePicker createDatePicker(String oldValue, JFXPanel jfxPanel) {
		DatePicker datePicker = new DatePicker();
		if (oldValue != null && !oldValue.isBlank()) {
			datePicker.setValue(LocalDate.parse(oldValue));
		}

		DatePickerSkin skin = new DatePickerSkin(datePicker);
		StackPane pane = new StackPane(skin.getPopupContent());
		Scene scene = new Scene(pane);
		if (FlatLaf.isLafDark()) {
			scene.getStylesheets()
					.add(HealthCheckSettingsDialog.class.getResource("/css/date-picker-dark.css").toExternalForm());
		} else {
			scene.getStylesheets()
					.add(HealthCheckSettingsDialog.class.getResource("/css/date-picker-light.css").toExternalForm());
		}
		jfxPanel.setScene(scene);

		return datePicker;
	}

	private static void showEditor(JFXPanel jfxPanel, DatePicker datePicker, JTextField startTime) {
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), jfxPanel, "title",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (answer == 0 && datePicker.getValue() != null) {
			startTime.setText(datePicker.getValue().toString());
		}
	}

	private void save() {
		if (selectedHostList == null || selectedHostList.getSelectedValues().isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), "Please select at least one host", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		persistenceController.getConfigDataService().writeDownTime(selectedHostList.getSelectedValues(),
				enabled.isSelected(), startTime.getText() + " " + ZERO_HOUR_STRING,
				endTime.getText() + " " + ZERO_HOUR_STRING);
	}
}
