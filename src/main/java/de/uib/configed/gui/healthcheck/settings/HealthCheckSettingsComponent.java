/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox.State;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;
import net.miginfocom.swing.MigLayout;

public final class HealthCheckSettingsComponent
		extends AbstractTeaComponent<HealthCheckSettingsModel, HealthCheckSettingsMsg, HealthCheckSettingsEffect> {
	private static final int TEXT_LABEL_WIDTH = 200;
	private static final String ZERO_HOUR_STRING = "00:00:00";
	private static final String END_OF_DAY_STRING = "23:59:59";

	private final OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ListSelectionDialog selectedHostList;
	private JTextField startDowntimeField;
	private JTextField endDowntimeField;
	private FlatTriStateCheckBox checkBoxCheckActive;
	private JTextField selectedHosts;
	private JLabel labelStartDowntime;
	private JLabel labelEndDowntime;
	private JButton saveButton;
	private JDialog dialog;
	private ConfigedMain configedMain;

	private List<String> initialSelection = new ArrayList<>();
	private State initialState = State.SELECTED;

	@Override
	protected HealthCheckSettingsModel initModel() {
		return HealthCheckSettingsModel.builder().selectedHosts(initialSelection).checkActiveState(initialState)
				.saveEnabled(initialState != State.INDETERMINATE).build();
	}

	@Override
	protected UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> updateModel(HealthCheckSettingsMsg msg,
			HealthCheckSettingsModel model) {
		return HealthCheckSettingsUpdate.update(model, msg);
	}

	@Override
	protected JComponent renderView(HealthCheckSettingsModel model, Consumer<HealthCheckSettingsMsg> dispatch) {
		JLabel labelSelectedHosts = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.selectedHosts"));
		labelSelectedHosts.setFont(labelSelectedHosts.getFont().deriveFont(Font.BOLD));

		selectedHosts = new JTextField();
		selectedHosts.setEditable(false);
		selectedHosts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispatch.accept(new HealthCheckSettingsMsg.HostsSelectionRequested());
			}
		});
		selectedHosts.setText(Utils.getListStringRepresentation(model.getSelectedHosts()));

		selectedHostList = new ListSelectionDialog(null,
				Configed.getResourceValue("HealthCheckSettingsDialog.selectedHosts"));
		List<String> hostNames = new ArrayList<>(persistenceController.getHostInfoCollections().getDepotNamesList());
		hostNames.addAll(persistenceController.getHostInfoCollections().getOpsiHostNames());
		selectedHostList.setListData(hostNames);
		selectedHostList.setMultiSelection();
		if (model.getSelectedHosts() != null && !model.getSelectedHosts().isEmpty()) {
			selectedHostList.setPreviousSelectionValues(model.getSelectedHosts());
		}

		checkBoxCheckActive = new FlatTriStateCheckBox(
				Configed.getResourceValue("HealthCheckSettingsDialog.healthCheckActive"), model.getCheckActiveState());
		checkBoxCheckActive.setAllowIndeterminate(false);
		checkBoxCheckActive.addItemListener(itemEvent -> dispatch
				.accept(new HealthCheckSettingsMsg.ToggleActivity(checkBoxCheckActive.getState())));

		labelStartDowntime = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.startDowntime"));
		labelStartDowntime.setFont(labelStartDowntime.getFont().deriveFont(Font.BOLD));

		startDowntimeField = new JTextField();
		startDowntimeField.setEditable(false);
		startDowntimeField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (checkBoxCheckActive.isSelected()) {
					dispatch.accept(new HealthCheckSettingsMsg.DowntimeSelectionRequested(DowntimeType.START));
				}
			}
		});

		labelEndDowntime = new JLabel(Configed.getResourceValue("HealthCheckSettingsDialog.endDowntime"));
		labelEndDowntime.setFont(labelEndDowntime.getFont().deriveFont(Font.BOLD));

		endDowntimeField = new JTextField();
		endDowntimeField.setEditable(false);
		endDowntimeField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (checkBoxCheckActive.isSelected()) {
					dispatch.accept(new HealthCheckSettingsMsg.DowntimeSelectionRequested(DowntimeType.END));
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		panel.add(labelSelectedHosts);
		panel.add(selectedHosts, "grow, gapbottom " + Globals.GAP_SIZE);

		panel.add(checkBoxCheckActive, "gapbottom " + Globals.GAP_SIZE);

		panel.add(labelStartDowntime);
		panel.add(startDowntimeField, "grow, gapbottom " + Globals.GAP_SIZE);

		panel.add(labelEndDowntime);
		panel.add(endDowntimeField, "grow");

		return panel;
	}

	public void showHealthCheckSettings(ConfigedMain configedMain) {
		showHealthCheckSettings(configedMain, new ArrayList<>(), State.SELECTED);
	}

	public void showHealthCheckSettings(ConfigedMain configedMain, List<String> defaultSelection, State state) {
		this.configedMain = configedMain;
		this.initialSelection = defaultSelection;
		this.initialState = state;
		Logging.info(this, "show health check settings dialog");

		// Create the dialog and show it
		JOptionPane jOptionPane = new JOptionPane(initUI());
		jOptionPane.setOptions(new Object[] { createSaveButton(), Configed.getResourceValue("buttonCancel") });
		dialog = jOptionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("HealthCheckSettingsDialog.title"));
		dialog.setVisible(true);
	}

	private JButton createSaveButton() {
		saveButton = new JButton(Configed.getResourceValue("save"));
		saveButton.addActionListener(actionEvent -> dispatch(HealthCheckSettingsMsg.SimpleMsg.SAVE_SETTINGS));
		saveButton.setEnabled(model.isSaveEnabled());
		return saveButton;
	}

	@Override
	protected void handleEffect(HealthCheckSettingsEffect effect) {
		switch (effect) {
		case HealthCheckSettingsEffect.SimpleEffect e -> handleSimpleEffect(e);
		case HealthCheckSettingsEffect.SelectDownTime(DowntimeType downtimeType) -> handleSelectDownTimeEffect(
				downtimeType);
		}
	}

	private void handleSelectDownTimeEffect(DowntimeType downtimeType) {
		String downTime = "";
		if (DowntimeType.START == downtimeType) {
			downTime = openDateSelectionDialog(startDowntimeField.getText(),
					Configed.getResourceValue("HealthCheckSettingsDialog.startDowntime"));
		} else {
			downTime = openDateSelectionDialog(endDowntimeField.getText(),
					Configed.getResourceValue("HealthCheckSettingsDialog.endDowntime"));
		}
		dispatch(new HealthCheckSettingsMsg.DowntimeSelected(downtimeType, downTime));
	}

	private void handleSimpleEffect(HealthCheckSettingsEffect.SimpleEffect effect) {
		switch (effect) {
		case SAVE_SETTINGS -> handleSaveSettingsEffect();
		case DISMISS_SETTINGS -> handleDismissSettingsEffect();
		case OPEN_HOST_SELECTION_DIALOG -> handleOpenHostSelectionDialogEffect();
		}
	}

	private void handleSaveSettingsEffect() {
		save();
		if (configedMain != null) {
			configedMain.reloadHosts();
		}
	}

	private void handleDismissSettingsEffect() {
		if (dialog != null) {
			dialog.setVisible(false);
		}
	}

	private void handleOpenHostSelectionDialogEffect() {
		List<String> hostsSelected = openHostSelectionDialog();
		dispatch(new HealthCheckSettingsMsg.HostsSelected(hostsSelected));
	}

	@Override
	protected void refreshView() {
		if (selectedHosts != null) {
			selectedHosts.setText(Utils.getListStringRepresentation(model.getSelectedHosts()));
		}
		if (checkBoxCheckActive != null) {
			checkBoxCheckActive.setState(model.getCheckActiveState());
		}
		if (startDowntimeField != null) {
			startDowntimeField.setText(model.getStartDowntime());
			startDowntimeField.setEnabled(model.getCheckActiveState() == State.SELECTED);
		}
		if (endDowntimeField != null) {
			endDowntimeField.setText(model.getEndDowntime());
			endDowntimeField.setEnabled(model.getCheckActiveState() == State.SELECTED);
		}
		if (labelStartDowntime != null) {
			labelStartDowntime.setEnabled(model.getCheckActiveState() == State.SELECTED);
		}
		if (labelEndDowntime != null) {
			labelEndDowntime.setEnabled(model.getCheckActiveState() == State.SELECTED);
		}
		if (saveButton != null) {
			saveButton.setEnabled(model.isSaveEnabled());
		}
	}

	private List<String> openHostSelectionDialog() {
		Logging.info(this, "openSelectionDialog for health check settings");
		List<String> previousSelection = selectedHostList.getSelectedValues();
		selectedHostList.show(dialog);
		if (selectedHostList.wasAccepted()) {
			return selectedHostList.getSelectedValues();
		} else {
			selectedHostList.setPreviousSelectionValues(previousSelection);
			return previousSelection;
		}
	}

	private String openDateSelectionDialog(String oldValue, String title) {
		Logging.info(this, "openDateSelectionDialog for health check settings");
		JFXPanel jfxPanel = new JFXPanel();
		Platform.setImplicitExit(false);
		DatePicker datePicker = createDatePicker(oldValue, jfxPanel);
		int answer = JOptionPane.showConfirmDialog(dialog, jfxPanel, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (answer == 0 && datePicker.getValue() != null) {
			return datePicker.getValue().toString();
		}
		return oldValue;
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
					.add(HealthCheckSettingsComponent.class.getResource("/css/date-picker-dark.css").toExternalForm());
		} else {
			scene.getStylesheets()
					.add(HealthCheckSettingsComponent.class.getResource("/css/date-picker-light.css").toExternalForm());
		}
		jfxPanel.setScene(scene);
		return datePicker;
	}

	private void save() {
		Logging.info(this, "save health check settings for selected hosts");
		if (model.getSelectedHosts() == null || model.getSelectedHosts().isEmpty()) {
			Logging.info(this, "No host selected");
			JOptionPane.showMessageDialog(dialog,
					Configed.getResourceValue("HealthCheckSettingsDialog.warningNoHostSelected"),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		String startDownTime = model.getStartDowntime();
		startDownTime = (startDownTime == null || startDownTime.isBlank()) ? "" : (startDownTime + ZERO_HOUR_STRING);
		String endDownTime = model.getEndDowntime();
		endDownTime = (endDownTime == null || endDownTime.isBlank()) ? "" : (endDownTime + END_OF_DAY_STRING);

		persistenceController.getConfigDataService().writeDownTime(model.getSelectedHosts(),
				model.getCheckActiveState() == State.SELECTED, startDownTime, endDownTime);
	}
}
