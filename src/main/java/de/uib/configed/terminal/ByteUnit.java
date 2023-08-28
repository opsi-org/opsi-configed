/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

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
