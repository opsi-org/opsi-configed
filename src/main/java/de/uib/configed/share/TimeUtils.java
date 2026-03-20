/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.share.logging.Logging;

/**
 * Utility methods for date and time handling.
 * <p>
 * Provides helpers for formatting timestamps, converting between time zones,
 * and working with application-specific date/time representations.
 * </p>
 * <p>
 * All methods are stateless and thread-safe.
 * </p>
 */
public final class TimeUtils {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private TimeUtils() {
	}

	public static List<Object> getNowTimeListValue() {
		List<Object> result = new ArrayList<>();
		String now = new Timestamp(System.currentTimeMillis()).toString();
		now = now.substring(0, now.indexOf("."));
		result.add(now);
		Logging.info("getNowTimeListValue", result);
		return result;
	}

	public static String getSeconds() {
		String sqlNow = new Timestamp(System.currentTimeMillis()).toString();
		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i + 1);
		time = time.substring(0, time.indexOf('.'));
		return date + "_" + time;
	}

	public static String getDate() {
		String sqlNow = new Timestamp(System.currentTimeMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));
		return sqlNow;
	}

	public static String formatDateTimeStringToLocal(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
				.format(DATE_TIME_FORMATTER);
	}

	public static String formatDateTimeStringToLocal(String dateTimeString) {
		try {
			return formatDateTimeStringToLocal(LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER));
		} catch (DateTimeParseException e) {
			Logging.warning(e, "Could not parse date time string: ", dateTimeString);
			return dateTimeString;
		}
	}

	public static void formatDateTimeStringForMap(Map<String, Object> map, String key) {
		if (map.get(key) instanceof String timestampString && !timestampString.isEmpty()) {
			map.put(key, formatDateTimeStringToLocal(timestampString));
		}
	}
}
