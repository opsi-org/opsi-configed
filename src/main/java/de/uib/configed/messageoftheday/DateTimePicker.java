/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

/**
 * A DateTimePicker with configurable datetime format where both date and time
 * can be changed via the text field and the date can additionally be changed
 * via the JavaFX default date picker. Updated version of:
 * https://stackoverflow.com/questions/28493097/is-there-any-date-and-time-picker-available-for-javafx
 */
public class DateTimePicker extends DatePicker {
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm";
	public static final ZoneId ZONEID = ZoneId.systemDefault();

	private IDateTimePickerCaller caller;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
	private ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(
			LocalDateTime.of(LocalDate.now(), LocalTime.MAX));

	public DateTimePicker(IDateTimePickerCaller caller) {
		Logging.debug("DateTimePicker constructor");
		Logging.debug("DateTimePicker zoneid: ", ZONEID);
		Logging.debug("DateTimePicker datetimeNow: ", datetimeNow());
		this.caller = caller;
	}

	public void init() {
		getStyleClass().add("datetime-picker");
		setConverter(new InternalConverter());
		setDayCellFactory(param -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				setDisable(empty || date.compareTo(LocalDate.now()) < 0);
			}
		});
	}

	@SuppressWarnings({ "java:S4968" })
	public void initData() {
		// Syncronize changes to the underlying date value back to the dateTimeValue
		valueProperty().addListener(this::setDateTimeValueLambda);

		// Syncronize changes to dateTimeValue back to the underlying date value
		dateTimeValue.addListener((ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue,
				LocalDateTime newValue) -> {
			Logging.debug("DateTimePicker dateTimeValue listener newValue: ", newValue, " oldValue: ", oldValue);
			setValue(newValue == null ? null : newValue.toLocalDate());
		});

		// Persist changes onblur
		getEditor().focusedProperty()
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
					Logging.debug("DateTimePicker focusedProperty listener newValue: ", newValue, " oldValue: ",
							oldValue);
					if (!newValue.booleanValue()) {
						simulateEnterPressed();
					}
				});
	}

	@SuppressWarnings({ "java:S4968" })
	private void setDateTimeValueLambda(ObservableValue<? extends LocalDate> observable, LocalDate oldValue,
			LocalDate newValue) {
		Logging.debug("DateTimePicker valueProperty listener newValue: ", newValue, " oldValue: ", oldValue);
		if (newValue == null) {
			setDateTimeValue(null);
		} else {
			setDateTimeValue(LocalDateTime.of(newValue, LocalTime.MAX));
		}
	}

	private static LocalDateTime datetimeNow() {
		Logging.debug("DateTimePicker datetimeNow");
		Instant time = Instant.now();
		return time.atZone(DateTimePicker.ZONEID).toLocalDateTime();
	}

	private void simulateEnterPressed() {
		Logging.debug("DateTimePicker simulateEnterPressed");
		getEditor().fireEvent(new KeyEvent(getEditor(), getEditor(), KeyEvent.KEY_PRESSED, null, null, KeyCode.ENTER,
				false, false, false, false));
	}

	public LocalDateTime getDateTimeValue() {
		Logging.debug("DateTimePicker getDateTimeValueLDT: ", dateTimeValue.get());
		return dateTimeValue.get();
	}

	public void setDateTimeValue(long unixTime) {
		Logging.debug("DateTimePicker setDateTimeValueUnix1: ", unixTime);
		if (unixTime <= 0) {
			setDateTimeValue(null);
			return;
		}

		LocalDateTime value = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), DateTimePicker.ZONEID);
		setDateTimeValue(value, true);
	}

	private void setDateTimeValue(LocalDateTime dateTime) {
		Logging.debug("DateTimePicker setDateTimeValueLDT2: ", dateTime);
		setDateTimeValue(dateTime, true);
	}

	/**
	 * Set the date time value of the picker.
	 * 
	 * @param dateTime The date time value to set or null
	 * @param notify   If true, the gui will be updated and the caller will be
	 *                 notified
	 */
	private void setDateTimeValue(LocalDateTime dateTime, boolean notify) {
		if (dateTime == null) {
			Logging.debug("DateTimePicker setDateTimeValueLDT3: null");
			return;
		}
		Logging.debug("DateTimePicker setDateTimeValue3: ", dateTime);
		this.dateTimeValue.set(dateTime);
		if (getEditor() == null) {
			Logging.warning("DateTime Error: Editor is null.");
			return;
		}

		Platform.runLater(() -> {
			getEditor().setText(dateTime.format(formatter));
			Logging.debug("DateTimePicker setDateTimeValue new editor: ", getEditor().getText());
		});
		if (notify) {
			if (caller == null) {
				Logging.warning("Caller is null");
				return;
			}
			LocalDateTime value = getDateTimeValue();
			Logging.debug("DateTimePicker setDateTimeValue3 notify ", value, " editor: ", getEditor().getText());
			caller.dataChanged(value);
		}
	}

	class InternalConverter extends StringConverter<LocalDate> {
		public String toString(LocalDate object) {
			Logging.trace("DateTimePicker InternalConverter toString was: ", object);
			LocalDateTime value = object == null ? null : getDateTimeValue();
			String s = (value != null) ? value.format(formatter) : "";
			Logging.trace("DateTimePicker InternalConverter toString is: ", s);
			return s;
		}

		public LocalDate fromString(String value) {
			Logging.trace("DateTimePicker InternalConverter fromString: ", value);
			if (value == null || "0".equals(value) || "".equals(value)) {
				return null;
			}
			LocalDateTime currValue = getDateTimeValue();
			try {
				LocalDateTime currValue2 = LocalDateTime.parse(value, formatter);
				if (currValue2.compareTo(datetimeNow()) <= 0) {
					Logging.error("DateTime Error: Date is in the past. Set datetime to now.");
					return datetimeNow().toLocalDate();
				}
				currValue = currValue2;
			} catch (DateTimeParseException e) {
				Logging.error(e, "DateTime InternalConverter Error. Set previous value.");
			}

			return currValue.toLocalDate();
		}
	}
}
