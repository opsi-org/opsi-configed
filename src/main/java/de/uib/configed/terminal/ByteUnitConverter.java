/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.util.Locale;

import de.uib.utils.logging.Logging;

public class ByteUnitConverter {
	private static final int ONE_MB_IN_BYTES = 1_000_000;
	private static final int ONE_GB_IN_BYTES = 1_000_000_000;
	private static final long ONE_TB_IN_BYTES = 1_000_000_000_000L;

	public ByteUnit detectByteUnit(int bytes) {
		ByteUnit result = null;

		if (bytes < ONE_MB_IN_BYTES) {
			result = ByteUnit.KILO_BYTE;
		} else if (bytes > ONE_MB_IN_BYTES && bytes < ONE_GB_IN_BYTES) {
			result = ByteUnit.MEGA_BYTE;
		} else if (bytes > ONE_GB_IN_BYTES && bytes < ONE_TB_IN_BYTES) {
			result = ByteUnit.GIGA_BYTE;
		} else {
			result = ByteUnit.TERA_BYTE;
		}

		return result;
	}

	public double convertByteUnit(int bytes, ByteUnit byteUnitToUse) {
		double size = 0.0;

		switch (byteUnitToUse) {
		case KILO_BYTE:
			size = bytes / 1000.0;
			break;
		case MEGA_BYTE:
			size = bytes / Math.pow(1000, 2);
			break;
		case GIGA_BYTE:
			size = bytes / Math.pow(1000, 3);
			break;
		case TERA_BYTE:
			size = bytes / Math.pow(1000, 4);
			break;
		default:
			Logging.warning(this, "unknown unit byte: " + byteUnitToUse);
			break;
		}

		return size;
	}

	public String asString(double bytes, ByteUnit byteUnitToUse) {
		return String.format(Locale.getDefault(), "%.2f %s", bytes, byteUnitToUse.toString());
	}
}
