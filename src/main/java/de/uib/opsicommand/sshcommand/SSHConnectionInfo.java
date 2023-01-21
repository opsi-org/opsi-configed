package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

public class SSHConnectionInfo {
	private boolean sshenabled = true;
	private String sshConnectionUser = "";
	private String sshConnectionPassword = "";
	private String sshConnectionHost = "";
	/** Port for server to connected as **/
	private String sshConnectionPost = SSHConnect.PORT_SSH;
	private boolean useKeyfile;
	private String keyfilepath = "";
	private String keyfilepassphrase = "";
	private static SSHConnectionInfo instance = null;

	private SSHConnectionInfo() {
	}

	public static SSHConnectionInfo getInstance() {
		Logging.info("SSHConnectionInfo getInstance, until now instance " + instance);
		if (instance == null)
			instance = new SSHConnectionInfo();
		return instance;
	}

	public String getHost() {

		return sshConnectionHost;
	}

	public String getUser() {
		return sshConnectionUser;
	}

	public String getPassw() {
		return sshConnectionPassword;
	}

	public String getShortPassw() {
		String shortened = "x";
		if (sshConnectionPassword.length() > 2)
			shortened = sshConnectionPassword.charAt(0) + "...";
		return shortened;
	}

	public String getPort() {
		return sshConnectionPost;
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
		Logging.info(this, "setHost, instance is " + instance);
		sshConnectionHost = getHostnameFromOpsihost(host);
	}

	public void setUser(String user) {
		sshConnectionUser = user;
	}

	public void setPassw(String pass) {
		sshConnectionPassword = pass;
	}

	public void setPort(String port) {
		sshConnectionPost = port;
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
		Logging.info("useKeyfile " + v + " now keyfilepath " + keyfilepath);
	}

	public void setUserData(String h, String u, String ps, String p) {
		Logging.info(this, "setUserData " + h + ", " + u + ", password " + (ps != null && ps.equals("")));
		setHost(getHostnameFromOpsihost(h));
		setPort(p);
		setUser(u);
		setPassw(ps);
		checkUserData();
	}

	public void setDefaultPort() {
		setPort(SSHConnect.PORT_SSH);
	}

	public void checkUserData() {
		if (getHost() == null)
			setHost(ConfigedMain.host);

		if (getPort() == null)
			setPort(SSHConnect.PORT_SSH);

		if (getUser() == null)
			setUser(ConfigedMain.user);

		if ((getPassw() == null) && (!usesKeyfile()))
			setPassw(ConfigedMain.password);

		else if (getPassw() == null)
			setPassw("");

		Logging.info(this, "checkUserData " + this.toString());
	}

	private static String getHostnameFromOpsihost(String host) {
		String result = host;
		Logging.info("SSHConnectionInfo  getHostnameFromOpsihost " + host);
		if (host != null && host.indexOf(":") > -1)
			result = host.substring(0, host.indexOf(":"));
		Logging.info("SSHConnectionInfo  getHostnameFromOpsihost result " + result);

		return result;
	}

	@Override
	public String toString() {
		String tempSSH = usesKeyfile() ? (keyfilepath + "-" + keyfilepassphrase) : "no sshkey";
		return getUser() + "@" + getHost() + ":" + getPort() + "|" + tempSSH;
	}
}