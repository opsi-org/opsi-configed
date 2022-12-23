package de.uib.utilities.script;

import java.util.List;

import de.uib.utilities.logging.logging;

//capsulates line spitting and putting the parts into a cmd array
public class CmdLauncher {
	String cmdPrefix;

	public CmdLauncher() {
		cmdPrefix = "";
	}

	public void setPrefix(String pre) {
		cmdPrefix = pre;
	}

	public void launch(final String s) {
		String cmd = cmdPrefix + " " + s;
		List<String> parts = de.uib.utilities.script.Interpreter.splitToList(cmd);

		
		

		try {
			logging.debug(this, "start OS call cmd: " + cmd + " splitted to " + parts);

			ProcessBuilder pb = new ProcessBuilder(parts);
			pb.redirectErrorStream(true);

			pb.start();

		} catch (Exception ex) {
			logging.error("Runtime error for command >>" + cmd + "<<, : " + ex, ex);
		}
	}
}
