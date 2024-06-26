/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.script;

import java.io.IOException;
import java.util.List;

import de.uib.utils.logging.Logging;

//capsulates line spitting and putting the parts into a cmd array
public class CmdLauncher {
	private String cmdPrefix;

	public CmdLauncher() {
		cmdPrefix = "";
	}

	public void setPrefix(String pre) {
		cmdPrefix = pre;
	}

	public void launch(final String s) {
		String cmd = cmdPrefix + " " + s;
		List<String> parts = Interpreter.splitToList(cmd);

		try {
			Logging.debug(this, "start OS call cmd: " + cmd + " splitted to " + parts);

			ProcessBuilder pb = new ProcessBuilder(parts);
			pb.redirectErrorStream(true);

			pb.start();
		} catch (IOException ex) {
			Logging.error("Cannot start ProcessBuilder for command >>" + cmd + "<<", ex);
		}
	}
}
