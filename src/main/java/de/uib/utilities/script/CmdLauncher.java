package de.uib.utilities.script;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
		java.util.List<String> parts = de.uib.utilities.script.Interpreter.splitToList(cmd);

		// logging.info(this, "" + values);
		// System.exit(0);

		try {
			logging.debug(this, "start OS call cmd: " + cmd + " splitted to " + parts);

			ProcessBuilder pb = new ProcessBuilder(parts);
			pb.redirectErrorStream(true);

			Process proc = pb.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = null;
			while ((line = br.readLine()) != null) {
				// logging.debug(getSelectedClients()[J] + " >" + line);
				// appendLog( ">" + line + "\n");
			}
			// logging.debug(getSelectedClients()[J] + " process exitValue " +
			// proc.exitValue());
		} catch (Exception ex) {
			logging.error("Runtime error for command >>" + cmd + "<<, : " + ex, ex);
		}
	}
}
