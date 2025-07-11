/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.messageoftheday;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import de.uib.configed.share.logging.Logging;
import javafx.util.StringConverter;

class InternalConverter extends StringConverter<LocalDate> {
	private DateTimePicker dateTimePicker;

	public InternalConverter(DateTimePicker dateTimePicker) {
		this.dateTimePicker = dateTimePicker;
		Logging.debug("DateTimePicker InternalConverter constructor");
	}

	public String toString(LocalDate object) {
		Logging.trace("DateTimePicker InternalConverter toString was: ", object);
		LocalDateTime value = object == null ? null : dateTimePicker.getDateTimeValue();
		String s = (value != null) ? value.format(dateTimePicker.getFormatter()) : "";
		Logging.trace("DateTimePicker InternalConverter toString is: ", s);
		return s;
	}

	public LocalDate fromString(String value) {
		Logging.trace("DateTimePicker InternalConverter fromString: ", value);
		if (value == null || "0".equals(value) || "".equals(value)) {
			return null;
		}
		LocalDateTime currValue = dateTimePicker.getDateTimeValue();
		try {
			LocalDateTime currValue2 = LocalDateTime.parse(value, dateTimePicker.getFormatter());
			if (currValue2.compareTo(DateTimePicker.datetimeNow()) <= 0) {
				Logging.error("DateTime Error: Date is in the past. Set datetime to now.");
				return DateTimePicker.datetimeNow().toLocalDate();
			}
			currValue = currValue2;
		} catch (DateTimeParseException e) {
			Logging.error(e, "DateTime InternalConverter Error. Set previous value.");
		}

		return currValue.toLocalDate();
	}
}
