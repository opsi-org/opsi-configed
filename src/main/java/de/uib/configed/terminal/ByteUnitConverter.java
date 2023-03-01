package de.uib.configed.messagebus;

import java.util.Locale;

import de.uib.utilities.logging.Logging;

public class ByteUnitConverter {
	private static final int ONE_MB_IN_BYTES = 1_000_000;
	private static final int ONE_GB_IN_BYTES = 1_000_000_000;
	private static final long ONE_TB_IN_BYTES = 1_000_000_000_000L;

	public enum ByteUnit {
		KILO_BYTE {
			@Override
			public String toString() {
				return "KB";
			}
		},
		MEGA_BYTE {
			@Override
			public String toString() {
				return "MB";
			}
		},
		GIGA_BYTE {
			@Override
			public String toString() {
				return "GB";
			}
		},
		TERA_BYTE {
			@Override
			public String toString() {
				return "TB";
			}
		}
	}

	private ByteUnit byteUnit;

	public ByteUnitConverter(ByteUnit byteUnit) {
		this.byteUnit = byteUnit;
	}

	public ByteUnitConverter() {

	}

	public ByteUnit detectByteUnit(int bytes) {
		ByteUnit result = null;

		if (bytes < ONE_MB_IN_BYTES) {
			result = ByteUnit.KILO_BYTE;
		} else if (bytes > ONE_MB_IN_BYTES && bytes < ONE_GB_IN_BYTES) {
			result = ByteUnit.MEGA_BYTE;
		} else if (bytes < ONE_GB_IN_BYTES && bytes < ONE_TB_IN_BYTES) {
			result = ByteUnit.GIGA_BYTE;
		} else {
			result = ByteUnit.TERA_BYTE;
		}

		return result;
	}

	public double convertByteUnit(int bytes) {
		return convertByteUnit(bytes, byteUnit);
	}

	public double convertByteUnit(int bytes, ByteUnit byteUnitToUse) {
		double size = 0.0;

		switch (byteUnitToUse) {
		case KILO_BYTE:
			size = bytes / 1024.0;
			break;
		case MEGA_BYTE:
			size = bytes / Math.pow(1024, 2);
			break;
		case GIGA_BYTE:
			size = bytes / Math.pow(1024, 3);
			break;
		case TERA_BYTE:
			size = bytes / Math.pow(1024, 4);
			break;
		default:
			Logging.warning(this, "unknown unit byte: " + byteUnitToUse);
		}

		return size;
	}

	public String asString(double bytes, ByteUnit byteUnitToUse) {
		return String.format(Locale.getDefault(), "%.2f %s", bytes, byteUnitToUse.toString());	
	}
}
