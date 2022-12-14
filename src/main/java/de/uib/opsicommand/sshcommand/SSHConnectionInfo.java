package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.logging;

public class SSHConnectionInfo {
	private boolean sshenabled = true;
	private String ssh_connection_user = "";
	private String ssh_connection_passw = "";
	private String ssh_connection_host = "";
	/** Port for server to connected as **/
	private String ssh_connection_port = SSHConnect.portSSH;
	private boolean useKeyfile;
	private String keyfilepath = "";
	private String keyfilepassphrase = "";
	private static SSHConnectionInfo instance = null;

	private SSHConnectionInfo() {
	}

	public static SSHConnectionInfo getInstance() {
		logging.info("SSHConnectionInfo getInstance, until now instance " + instance);
		if (instance == null)
			instance = new SSHConnectionInfo();
		return instance;
	}

	public String getHost() {

		return ssh_connection_host;
	}

	public String getUser() {
		return ssh_connection_user;
	}

	public String getPassw() {
		return ssh_connection_passw;
	}

	public String getShortPassw() {
		String shortened = "x";
		if (ssh_connection_passw.length() > 2)
			shortened = ssh_connection_passw.charAt(0) + "...";
		return shortened;
	}

	public String getPort() {
		return ssh_connection_port;
	}

	public boolean usesKeyfile() {
		return useKeyfile;
	}

	public String getKeyfilePath() {
		return keyfilepath;
	}

	public String getKeyfilePassphrase() {
		return keyfilepassphrase;
	}

	public boolean getSSHActivateStatus() {
		return sshenabled;
	}

	public void setSSHActivateStatus(boolean val) {
		sshenabled = val;
	}

	public void setHost(String host) {
		logging.info(this, "setHost, instance is " + instance);
		ssh_connection_host = getHostnameFromOpsihost(host);
	}

	public void setUser(String user) {
		ssh_connection_user = user;
	}

	public void setPassw(String pass) {
		ssh_connection_passw = pass;
	}

	public void setPort(String port) {
		ssh_connection_port = port;
	}

	public void useKeyfile(boolean v) {
		useKeyfile(v, null, null);
	}

	public void useKeyfile(boolean v, String k) {
		useKeyfile(v, k, null);
	}

	public void useKeyfile(boolean v, String k, String p) {
		useKeyfile = v;
		keyfilepath = (k == null) ? "" : k;
		keyfilepassphrase = (p == null) ? "" : p;
		logging.info("useKeyfile " + v + " now keyfilepath " + keyfilepath);
	}

	public void setUserData(String h, String u, String ps, String p) {
		logging.info(this, "setUserData " + h + ", " + u + ", password " + (ps != null && ps.equals("")));
		setHost(getHostnameFromOpsihost(h));
		setPort(p);
		setUser(u);
		setPassw(ps);
		checkUserData();
	}

	public void setDefaultPort() {
		setPort(SSHConnect.portSSH);
	}

	public void checkUserData() {
		if (getHost() == null)
			// setHost(allowedHostsContainsSubstring(
			// SSHCommandFactory.getInstance().getAllowedHosts(),
			// ConfigedMain.HOST));
			setHost(ConfigedMain.HOST);
		// for SSH ConfigedMain.HOST always allowed

		if (getPort() == null)
			setPort(SSHConnect.portSSH);
		if (getUser() == null)
			setUser(ConfigedMain.USER);
		if ((getPassw() == null) && (!usesKeyfile()))
			setPassw(ConfigedMain.PASSWORD);
		else if (getPassw() == null)
			setPassw("");

		logging.info(this, "checkUserData " + this.toString());
	}

	private static String getHostnameFromOpsihost(String host) {
		String result = host;
		logging.info("SSHConnectionInfo  getHostnameFromOpsihost " + host);
		if (host != null && host.indexOf(":") > -1)
			result = host.substring(0, host.indexOf(":"));
		logging.info("SSHConnectionInfo  getHostnameFromOpsihost result " + result);

		return result;
	}

	@Override
	public String toString() {
		String tmp_ssh = usesKeyfile() ? (keyfilepath + "-" + keyfilepassphrase) : "no sshkey";
		return getUser() + "@" + getHost() + ":" + getPort() + "|" + tmp_ssh;
	}
}