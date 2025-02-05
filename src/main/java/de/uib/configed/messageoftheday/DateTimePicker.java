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
	public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
	public static final ZoneId ZONEID = ZoneId.systemDefault();

	private IDateTimePickerCaller caller;
	private DateTimeFormatter formatter;
	private ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(LocalDateTime.now());

	@java.lang.SuppressWarnings("squid:S110")
	private ObjectProperty<String> format = new SimpleObjectProperty<String>() {
		@Override
		public void set(String newValue) {
			super.set(newValue);
			formatter = DateTimeFormatter.ofPattern(newValue);
		}
	};

	public DateTimePicker(IDateTimePickerCaller caller) {
		Logging.debug("DateTimePicker constructor");
		Logging.debug("DateTimePicker zoneid: ", ZONEID);
		Logging.debug("DateTimePicker datetimeNow: ", datetimeNow());
		this.caller = caller;
	}

	public void init() {
		getStyleClass().add("datetime-picker");
		setFormat(DEFAULT_FORMAT);
		setConverter(new InternalConverter());
		setDayCellFactory(param -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				setDisable(empty || date.compareTo(LocalDate.now()) < 0);
			}
		});
	}

	// @java.lang.SuppressWarnings("squid:S4968")
	public void initData() {
		// Syncronize changes to the underlying date value back to the dateTimeValue
		valueProperty().addListener(
				(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
					if (newValue == null) {
						setDateTimeValue(null);
					} else if (dateTimeValue.get() == null) {
						setDateTimeValue(LocalDateTime.of(newValue, timeNow()));
					} else {
						LocalTime time = dateTimeValue.get().toLocalTime();
						setDateTimeValue(LocalDateTime.of(newValue, time));
					}
				});

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

	private static LocalTime timeNow() {
		Instant time = Instant.now();
		return time.atZone(DateTimePicker.ZONEID).toLocalTime();
	}

	private static LocalDateTime datetimeNow() {
		Instant time = Instant.now();
		return time.atZone(DateTimePicker.ZONEID).toLocalDateTime();
	}

	private void simulateEnterPressed() {
		Logging.debug("DateTimePicker simulateEnterPressed");
		getEditor().fireEvent(new KeyEvent(getEditor(), getEditor(), KeyEvent.KEY_PRESSED, null, null, KeyCode.ENTER,
				false, false, false, false));
	}

	public long getDateTimeValueUnix() {
		LocalDateTime ldt = dateTimeValue.get();
		long unixTime = ldt.atZone(DateTimePicker.ZONEID).toEpochSecond();
		Logging.debug("DateTimePicker getDateTimeValueUnix: ", unixTime);
		return unixTime;
	}

	public LocalDateTime getDateTimeValue() {
		Logging.debug("DateTimePicker getDateTimeValueLDT: ", dateTimeValue.get());
		return dateTimeValue.get();
	}

	public void setDateTimeValue(long unixTime) {
		Logging.debug("DateTimePicker setDateTimeValueUnix0: ", unixTime);
		setDateTimeValue(unixTime, true);
	}

	public void setDateTimeValue(long unixTime, boolean notify) {
		Logging.debug("DateTimePicker setDateTimeValueUnix1: ", unixTime);
		if (unixTime <= 0) {
			setDateTimeValue(null);
			return;
		}

		LocalDateTime value = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), DateTimePicker.ZONEID);
		setDateTimeValue(value, notify);
	}

	public void setDateTimeValue(LocalDateTime dateTime) {
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
	public void setDateTimeValue(LocalDateTime dateTime, boolean notify) {
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

		getEditor().setText(dateTime.format(formatter));
		if (notify) {
			if (caller == null) {
				Logging.warning("Caller is null");
				return;
			}
			caller.dataChanged(getDateTimeValue());
		}
	}

	public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
		Logging.debug("DateTimePicker dateTimeValueProperty");
		return dateTimeValue;
	}

	public String getFormat() {
		Logging.debug("DateTimePicker getFormat: ", format.get());
		return format.get();
	}

	public ObjectProperty<String> formatProperty() {
		Logging.debug("DateTimePicker formatProperty");
		return format;
	}

	public void setFormat(String format) {
		Logging.debug("DateTimePicker setFormat: ", format);
		this.format.set(format);
	}

	class InternalConverter extends StringConverter<LocalDate> {
		public String toString(LocalDate object) {
			Logging.trace("DateTimePicker InternalConverter toString was: ", object);
			LocalDateTime value = object == null ? null : getDateTimeValue();
			String s = (value != null) ? value.format(formatter) : "";
			Logging.trace("DateTimePicker InternalConverter toString is: ", s);
			setDateTimeValue(value);
			return s;
		}

		public LocalDate fromString(String value) {
			Logging.trace("DateTimePicker InternalConverter fromString: ", value);
			if (value == null || "0".equals(value) || "".equals(value)) {
				setDateTimeValue(null);
				return null;
			}
			LocalDateTime currValue = getDateTimeValue();
			try {
				LocalDateTime currValue2 = LocalDateTime.parse(value, formatter);
				if (currValue2.compareTo(datetimeNow()) <= 0) {
					Logging.error("DateTime Error: Date is in the past. Set previous value.");
					setDateTimeValue(currValue);
					return currValue.toLocalDate();
				}
				currValue = currValue2;
			} catch (DateTimeParseException e) {
				Logging.error(e, "DateTime InternalConverter Error. Set previous value.");
			}

			setDateTimeValue(currValue);
			return currValue.toLocalDate();
		}
	}
}
