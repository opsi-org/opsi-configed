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
		double size;
		switch (unit) {
		case KILO_BYTE:
			size = bytes / 1_000.0;
			break;
		case MEGA_BYTE:
			size = bytes / 1_000_000.0;
			break;
		case GIGA_BYTE:
			size = bytes / 1_000_000_000.0;
			break;
		case TERA_BYTE:
			size = bytes / 1_000_000_000_000.0;
			break;
		default:
			Logging.warning(this, "Unknown byte unit: ", unit);
			size = bytes;
			break;
		}
		return size;
	}

	public String asString(double value, ByteUnit unit) {
		return String.format(Locale.getDefault(), "%.2f %s", value, unit);
	}
}
