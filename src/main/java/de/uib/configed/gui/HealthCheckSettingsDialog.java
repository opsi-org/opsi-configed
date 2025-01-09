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

import de.uib.configed.Configed;
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
	private static final String END_OF_DAY_STRING = "23:59:59";

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private FSelectionList selectedHostList;
	private JTextField startDowntime;
	private JTextField endDowntime;
	private JCheckBox checkBoxCheckActive;

	private JDialog dialog;

	public void showHealthCheckSettings() {
		Logging.info(this, "show health check settings dialog");

		JOptionPane jOptionPane = new JOptionPane(createOptionsPanel());
		jOptionPane.setOptions(
				new Object[] { Configed.getResourceValue("save"), Configed.getResourceValue("buttonCancel") });
		dialog = jOptionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("HealthCheckSettingsDialog.title"));
		dialog.setVisible(true);

		Logging.info(this, "User selected ", jOptionPane.getValue());
		if (jOptionPane.getValue() == Configed.getResourceValue("save")) {
			save();
		}
	}

	private JPanel createOptionsPanel() {
		JLabel labelSelectedHosts = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.selectedHosts"));

		JTextField selectedHosts = new JTextField();
		selectedHosts.setEditable(false);
		selectedHosts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				openHostSelectionDialog(selectedHosts);
			}
		});

		JLabel labelCheckActive = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.healthCheckActive"));
		checkBoxCheckActive = new JCheckBox((String) null, true);

		JLabel labelStartDowntime = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.startDowntime"));
		startDowntime = new JTextField();
		startDowntime.setEditable(false);
		startDowntime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (checkBoxCheckActive.isSelected()) {
					openDateSelectionDialog(startDowntime,
							Configed.getResourceValue("HealthCheckSettingsDialog.startDowntime"));
				}
			}
		});

		JLabel labelEndDowntime = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.endDowntime"));
		endDowntime = new JTextField();
		endDowntime.setEditable(false);
		endDowntime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (checkBoxCheckActive.isSelected()) {
					openDateSelectionDialog(endDowntime,
							Configed.getResourceValue("HealthCheckSettingsDialog.endDowntime"));
				}
			}
		});

		checkBoxCheckActive.addItemListener((ItemEvent event) -> {
			labelStartDowntime.setEnabled(checkBoxCheckActive.isSelected());
			startDowntime.setEnabled(checkBoxCheckActive.isSelected());
			labelEndDowntime.setEnabled(checkBoxCheckActive.isSelected());
			endDowntime.setEnabled(checkBoxCheckActive.isSelected());

			// We want to remove the text when the checkbox is unchecked
			// because there should be no time set anyways
			if (!checkBoxCheckActive.isSelected()) {
				startDowntime.setText(null);
				endDowntime.setText(null);
			}
		});

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(labelSelectedHosts).addComponent(selectedHosts))
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(layout.createParallelGroup().addComponent(labelCheckActive).addComponent(checkBoxCheckActive))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(labelStartDowntime).addComponent(startDowntime))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(labelEndDowntime).addComponent(endDowntime)));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(labelSelectedHosts)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(selectedHosts, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelCheckActive)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(checkBoxCheckActive, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelStartDowntime)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(startDowntime, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH))
				.addGroup(layout.createSequentialGroup().addComponent(labelEndDowntime)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(endDowntime, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH, TEXT_LABE_WIDTH)));

		return panel;
	}

	private void openHostSelectionDialog(JTextField selectedHosts) {
		Logging.info(this, "openSelectionDialog for health check settings");

		if (selectedHostList == null) {
			selectedHostList = new FSelectionList(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("HealthCheckSettingsDialog.selectedHosts"), true,
					new String[] { Configed.getResourceValue("buttonOK"), Configed.getResourceValue("buttonCancel") },
					300, 500);
			List<String> hostNames = new ArrayList<>(
					persistenceController.getHostInfoCollections().getDepotNamesList());
			hostNames.addAll(persistenceController.getHostInfoCollections().getOpsiHostNames());

			selectedHostList.setListData(hostNames);
		}

		// Get the selection to restore it if the user cancels the dialog
		List<String> previousSelection = selectedHostList.getSelectedValues();

		selectedHostList.setLocationRelativeTo(dialog);
		selectedHostList.setVisible(true);

		if (selectedHostList.getResult() == 1) {
			selectedHosts.setText(Utils.getListStringRepresentation(selectedHostList.getSelectedValues()));
		} else {
			selectedHostList.setPreviousSelectionValues(previousSelection);
		}
	}

	private void openDateSelectionDialog(JTextField startTime, String title) {
		Logging.info(this, "openDateSelectionDialog for health check settings");

		JFXPanel jfxPanel = new JFXPanel();
		// We need to set implicit exit to false to prevent a Nullpointerexception
		// when opening a dialog for the second time
		Platform.setImplicitExit(false);

		DatePicker datePicker = createDatePicker(startTime.getText(), jfxPanel);
		showEditor(jfxPanel, datePicker, startTime, title);
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

	private void showEditor(JFXPanel jfxPanel, DatePicker datePicker, JTextField startTime, String title) {
		int answer = JOptionPane.showConfirmDialog(dialog, jfxPanel, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (answer == 0 && datePicker.getValue() != null) {
			startTime.setText(datePicker.getValue().toString());
		}
	}

	private void save() {
		Logging.info(this, "save health check settings for selected hosts");

		if (selectedHostList == null || selectedHostList.getSelectedValues().isEmpty()) {
			Logging.info(this, "No host selected");
			JOptionPane.showMessageDialog(dialog,
					Configed.getResourceValue("HealthCheckSettingsDialog.warningNoHostSelected"),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		persistenceController.getConfigDataService().writeDownTime(selectedHostList.getSelectedValues(),
				checkBoxCheckActive.isSelected(), startDowntime.getText() + " " + ZERO_HOUR_STRING,
				endDowntime.getText() + " " + END_OF_DAY_STRING);
	}
}
