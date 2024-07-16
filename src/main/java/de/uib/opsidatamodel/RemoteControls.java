/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

import java.util.HashMap;

import de.uib.configed.type.RemoteControl;
import de.uib.utils.logging.Logging;

public class RemoteControls extends HashMap<String, RemoteControl> {
	public void checkIn(String key, Object value) {
		if (!key.startsWith(RemoteControl.CONFIG_KEY)) {
			return;
		}

		String rcPartOfKey = key.substring(RemoteControl.CONFIG_KEY.length());

		if (rcPartOfKey.length() < 2 || rcPartOfKey.charAt(0) != '.') {
			Logging.error("No remote control key given after '", RemoteControl.CONFIG_KEY, "'");
			return;
		}

		rcPartOfKey = rcPartOfKey.substring(1);

		int i = nextPartAt(rcPartOfKey);

		// first level key

		if (i == -1) {
			RemoteControl rc = retrieveRC(rcPartOfKey);
			if (rc.getCommand().isEmpty()) {
				rc.setCommand(value);
			}

			// if command is specified by an explicit command key, leave it
		} else {
			// second level key

			String name = rcPartOfKey.substring(0, i);

			RemoteControl rc = retrieveRC(name);

			String remainder = rcPartOfKey.substring(i + 1);

			i = nextPartAt(remainder);

			if (i != -1) {
				// there are no 3rd level keys
				Logging.error("Remote control key has too many parts");
			} else if (remainder.equals(RemoteControl.DESCRIPTION_KEY)) {
				rc.setDescription(value);
			} else if (remainder.equals(RemoteControl.COMMAND_KEY)) {
				rc.setCommand(value);
			} else if (remainder.equals(RemoteControl.EDITABLE_KEY)) {
				rc.setEditable(value);
			} else {
				Logging.warning(this, "unexpected remainder " + remainder);
			}
		}
	}

	private static int nextPartAt(String remainder) {
		int posDot = remainder.indexOf(".");
		if (posDot == -1 || remainder.length() == posDot + 1) {
			return -1;
		} else {
			return posDot;
		}
	}

	private RemoteControl retrieveRC(String name) {
		if (get(name) != null) {
			return get(name);
		} else {
			RemoteControl rc = new RemoteControl();
			rc.setName(name);
			put(name, rc);
			return rc;
		}
	}
}
