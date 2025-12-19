/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.terminal;

import java.util.Locale;

import de.uib.configed.share.logging.Logging;

public class ByteUnitConverter {
	private static final long ONE_KB = 1_000L;
	private static final long ONE_MB = ONE_KB * 1_000;
	private static final long ONE_GB = ONE_MB * 1_000;
	private static final long ONE_TB = ONE_GB * 1_000;

	public ByteUnit detectByteUnit(long bytes) {
		ByteUnit result;
		if (bytes < ONE_MB) {
			result = ByteUnit.KILO_BYTE;
		} else if (bytes < ONE_GB) {
			result = ByteUnit.MEGA_BYTE;
		} else if (bytes < ONE_TB) {
			result = ByteUnit.GIGA_BYTE;
		} else {
			result = ByteUnit.TERA_BYTE;
		}
		return result;
	}

	public double convertByteUnit(long bytes, ByteUnit unit) {
		return switch (unit) {
		case KILO_BYTE -> bytes / 1_000.0;
		case MEGA_BYTE -> bytes / 1_000_000.0;
		case GIGA_BYTE -> bytes / 1_000_000_000.0;
		case TERA_BYTE -> bytes / 1_000_000_000_000.0;
		default -> {
			Logging.warning(this, "Unknown byte unit: ", unit);
			yield bytes;
		}
		};
	}

	public String asString(double value, ByteUnit unit) {
		return String.format(Locale.getDefault(), "%.2f %s", value, unit);
	}
}
