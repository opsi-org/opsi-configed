/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * Wrapper for the DateTimePicker. This class mainly allows to use the (javafx) DateTimePicker in a swing environment.
 */
package de.uib.configed.messageoftheday;

import com.formdev.flatlaf.FlatLaf;

import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DateTimePickerWrapper extends JFXPanel {
	private DateTimePicker dateTimePicker;
	private IDateTimePickerCaller caller;

	public DateTimePickerWrapper(IDateTimePickerCaller caller) {
		super();
		Logging.debug("MyDateTimePicker constructor");
		this.caller = caller;
		showDatePicker(this);
		initFX();
	}

	private void initFX() {
		Platform.setImplicitExit(false);
		Platform.runLater(() -> showDatePicker(this));

	}

	@Override
	public void setEnabled(boolean enabled) {
		Logging.debug("MyDateTimePicker setEnabled: ", enabled);
		dateTimePicker.setDisable(!enabled);
	}

	private void showDatePicker(JFXPanel jfxPanel) {
		Logging.debug("MyDateTimePicker stylePicker");
		dateTimePicker = new DateTimePicker(this.caller);
		dateTimePicker.init();
		toLayout(jfxPanel);
		dateTimePicker.initData();
	}

	private void toLayout(JFXPanel jfxPanel) {
		StackPane pane = new StackPane(new VBox(dateTimePicker));
		Scene scene = new Scene(pane);
		if (FlatLaf.isLafDark()) {
			scene.getStylesheets().add(getClass().getResource("/css/date-picker-dark.css").toExternalForm());
		} else {
			scene.getStylesheets().add(getClass().getResource("/css/date-picker-light.css").toExternalForm());
		}
		jfxPanel.setScene(scene);
	}

	public DateTimePicker getDateTimePicker() {
		return dateTimePicker;
	}

}
