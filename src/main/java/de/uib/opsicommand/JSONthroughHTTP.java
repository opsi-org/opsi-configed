package de.uib.opsicommand;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.thread.WaitCursor;
import net.jpountz.lz4.LZ4FrameInputStream;
import utils.Base64OutputStream;

/*  Copyright (c) 2006-2016, 2021 uib.de
 
Usage of this portion of software is allowed unter the restrictions of the GPL
 
*/

/**
 * @author Rupert Roeder, Jan Schneider, Naglis Vidziunas
 */

public class JSONthroughHTTP extends JSONExecutioner {
	int[] serverVersion = { 0, 0, 0, 0 };
	public static boolean compressTransmission = false;
	public static boolean gzipTransmission = false;
	public static boolean lz4Transmission = false;
	protected static final int POST = 0;
	protected static final int GET = 1;
	protected static final String CODING_TABLE = "UTF8";
	protected String host;
	protected String username;
	protected String password;
	protected int portHTTP = 4444;
	public static final int DEFAULT_PORT = 4447;
	protected int portHTTPS = DEFAULT_PORT;
	protected boolean startConnecting = false;
	protected boolean endConnecting = false;
	protected URL serviceURL;
	protected String sessionId;
	protected String lastSessionId;
	protected int requestMethod = POST;
	public static final Charset UTF8DEFAULT = StandardCharsets.UTF_8;
	protected boolean certificateExists = false;
	protected boolean trustOnlyOnce = false;
	protected boolean trustAlways = false;

	class JSONCommunicationException extends Exception {
		JSONCommunicationException(String message) {
			super(message);
		}
	}

	public JSONthroughHTTP(String host, String username, String password) {
		this.host = host;
		int idx = -1;
		if (host.contains("[") && host.contains("]")) {
			idx = host.indexOf(":", host.indexOf("]"));
		} else {
			idx = host.indexOf(":");
		}

		if (idx > -1) {
			this.host = host.substring(0, idx);
			this.portHTTP = this.portHTTPS = Integer.parseInt(host.substring(idx + 1, host.length()));
		}
		this.username = username;
		this.password = password;
		conStat = new ConnectionState();
	}

	protected String makeRpcPath(OpsiMethodCall omc) {
		StringBuilder result = new StringBuilder("/rpc");

		if (omc.getRpcPath() != null && !(omc.getRpcPath().equals(""))) {
			result.append("/");
			result.append(omc.getRpcPath());
		}

		return result.toString();
	}

	/**
	 * This method takes hostname, port and the OpsiMethodCall rpcPath
	 * transformed to String in order to build an URL as expected by the
	 * opsiconfd.
	 * <p>
	 * The HTTPS subclass overwrites the method to modify "http" to "https".
	 * 
	 * @param omc
	 */
	protected String produceBaseURL(String rpcPath) {
		return "http://" + host + ":" + portHTTP + rpcPath;
	}

	private void appendGETParameter(String urlS, String json) {
		if (requestMethod == GET) {
			try {
				String urlEnc = URLEncoder.encode(json, "UTF8");
				urlS += "?" + urlEnc;
			} catch (UnsupportedEncodingException ux) {
				Logging.error(this, ux.toString());
			}
		}

		Logging.debug(this, "we shall try to connect to " + urlS);
		try {
			serviceURL = new URL(urlS);
		} catch (MalformedURLException ex) {
			Logging.error(urlS + " no legal URL, " + ex.toString());
		}
	}

	public void makeURL(OpsiMethodCall omc) {
		Logging.debug(this, "make url for " + omc);

		String urlS = produceBaseURL(makeRpcPath(omc));
		String json = omc.getJsonString();
		appendGETParameter(urlS, json);
	}

	protected String produceJSONstring(OpsiMethodCall omc) {
		return omc.getJsonString();
	}

	protected String produceJSONstring(List<OpsiMethodCall> omcList) {
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < omcList.size(); i++) {
			json.append(omcList.get(i).getJsonString());
			if (i < omcList.size() - 1) {
				json.append(",");
			}
		}
		json.append("]");
		return json.toString();
	}

	public void makeURL(List<OpsiMethodCall> omcList) {
		Logging.debug(this, "make url for " + omcList);
		if (omcList == null || omcList.isEmpty()) {
			Logging.error("missing method call");
			return;
		}

		String rpcPath0 = makeRpcPath(omcList.get(0));

		for (OpsiMethodCall omc : omcList) {
			String rpcPath = makeRpcPath(omc);
			if (!rpcPath.equals(rpcPath0)) {
				Logging.error("no common RPC path:  " + rpcPath0 + " cf. " + omcList.get(0));
				return;
			}
		}

		String urlS = produceBaseURL(rpcPath0);
		String json = produceJSONstring(omcList);
		appendGETParameter(urlS, json);
	}

	/**
	 * Opening the connection. The method is prepared to be subclassed and take
	 * additional informations for the connection.
	 */
	protected HttpURLConnection produceConnection() throws IOException {
		return (HttpURLConnection) serviceURL.openConnection();
	}

	private void setGeneralRequestProperties(HttpURLConnection connection) {
		String authorization = Base64OutputStream.encode(username + ":" + password);
		connection.setRequestProperty("Authorization", "Basic " + authorization);
		connection.setRequestProperty("X-opsi-session-lifetime", "900"); // has to be value between 1 and 43300 [sec]

		if (lz4Transmission) {
			connection.setRequestProperty("Accept-Encoding", "lz4");
		} else if (gzipTransmission) {
			connection.setRequestProperty("Accept-Encoding", "gzip");
		}

		connection.setRequestProperty("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
	}

	/**
	 * This method receives the JSONObject via HTTP.
	 */
	@Override
	public JSONObject retrieveJSONObject(OpsiMethodCall omc) {
		boolean background = false;
		Logging.info(this, "retrieveJSONObjects started");
		WaitCursor waitCursor = null;

		if (omc != null && !omc.isBackground()) {
			waitCursor = new WaitCursor(null, new Cursor(Cursor.DEFAULT_CURSOR), this.getClass().getName());
		} else {
			background = true;
		}

		JSONObject result = null;

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);

		makeURL(omc);

		TimeCheck timeCheck = new TimeCheck(this, "retrieveJSONObject  FROM " + serviceURL + "  ++ " + omc);
		timeCheck.start();

		HttpURLConnection connection = null;
		try {
			connection = produceConnection();
			// the underlying network connection can be shared,
			// only disconnect() may close the underlying socket

			setGeneralRequestProperties(connection);

			if (requestMethod == POST) {
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setUseCaches(false);
			} else {
				connection.setRequestMethod("GET");
			}

			Logging.info(this, "retrieveJSONObject by connection " + connection);
			Logging.info(this, "retrieveJSONObject request properties " + connection.getRequestProperties());
			Logging.info(this, "retrieveJSONObject request method " + connection.getRequestMethod());
			Logging.info(this, "https protocols given by system " + Configed.SYSTEM_SSL_VERSION);

			if (sessionId != null) {
				connection.setRequestProperty("Cookie", sessionId);
			}
			Logging.info(this, "retrieveJSONObjects request old or " + " new session ");
			Logging.info(this, "retrieveJSONObjects connected " + " new session ");

			try {
				connection.connect();
			} catch (Exception ex) {
				String s = "" + ex;
				int i = s.indexOf("Unsupported ciphersuite");
				if (i > -1) {
					s = "\n\n" + s.substring(i) + "\n" + "In this SSL configuration, a connection is not possible";

					Logging.error(s);
					Logging.checkErrorList(null);
				}

				throw (ex);
			}

			if (connection instanceof HttpsURLConnection) {
				Logging.info(this, "connection cipher suite " + ((HttpsURLConnection) connection).getCipherSuite());
			}

			if (requestMethod == POST) {
				try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), UTF8DEFAULT);
						BufferedWriter out = new BufferedWriter(writer)) {
					String json = produceJSONstring(omc);
					Logging.debug(this, "(POST) sending: " + json);
					out.write(json);
					out.flush();
				} catch (IOException iox) {
					Logging.info(this, "exception on writing json request " + iox);
				}
			}
		} catch (SSLException ex) {
			if (!background) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
			}

			if (conStat.getState() == ConnectionState.INTERRUPTED) {
				return null;
			}


			final StringBuilder message = new StringBuilder();

			if (!certificateExists) {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsUnverified") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.stillConnectToServer"));
				message.append("\n\n\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.noCertificateFound"));
			} else {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsInvalid") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.stillConnectToServer"));
				message.append("\n\n\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.unableToVerify"));
			}

			final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("JSONthroughHTTP.failedServerVerification"),
					true,
					new String[] { Configed.getResourceValue(Configed.getResourceValue("UIManager.cancelButtonText")),
							Configed.getResourceValue("JSONthroughHTTP.alwaysTrust"),
							Configed.getResourceValue("JSONthroughHTTP.trustOnlyOnce") },
					420, 200);

			try {
				SwingUtilities.invokeAndWait(() -> {
					fErrorMsg.setMessage(message.toString());
					fErrorMsg.setAlwaysOnTop(true);
					fErrorMsg.addWindowListener(new WindowAdapter() {
						@Override
						public void windowOpened(WindowEvent event) {
							// For some unknown reason the paint method isn't
							// called, when dialog is initialized in Windows
							// OS. To fix that we call paint method manually
							// by requesting the dialog to be repainted, when
							// it is opened.
							fErrorMsg.repaint();
						}
					});


					if (ConfigedMain.getMainFrame() == null && ConfigedMain.dpass != null) {
						fErrorMsg.setLocationRelativeTo(ConfigedMain.dpass);
					}

					fErrorMsg.setVisible(true);
				});
			} catch (InterruptedException e) {
				Logging.info("Thread was interrupted");
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				Logging.debug("exception thrown during doRun");
			}

			int choice = fErrorMsg.getResult();

			if (choice == 1) {
				conStat = new ConnectionState(ConnectionState.INTERRUPTED);
			} else if (choice == 2) {
				trustAlways = true;
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			} else if (choice == 3) {
				trustOnlyOnce = true;
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			}

			return null;
		} catch (IOException ex) {
			if (!background) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
			}

			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, " + ex.toString());

			return null;
		}

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				Logging.debug(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

				if (serverVersion[0] == 0) {
					String server = connection.getHeaderField("Server");
					Pattern pattern = Pattern.compile("opsiconfd ([\\d\\.]+)");
					Matcher matcher = pattern.matcher(server);
					if (matcher.find()) {
						Logging.info(this, "opsi server version: " + matcher.group(1));
						String[] versionParts = matcher.group(1).split("\\.");
						for (int i = 0; i < versionParts.length && i < 4; i++) {
							try {
								serverVersion[i] = Integer.parseInt(versionParts[i]);
							} catch (NumberFormatException nex) {
								Logging.error(this, "value is unparsable to int");
							}
						}
					} else {
						serverVersion[0] = 4;
						serverVersion[1] = 1;
					}

					if (compressTransmission) {
						if ((serverVersion[0] > 4) || (serverVersion[0] == 4 && serverVersion[1] >= 2)) {
							gzipTransmission = false;
							lz4Transmission = true;
						} else {
							gzipTransmission = true;
							lz4Transmission = false;
						}
					}
				}

				StringBuilder errorInfo = new StringBuilder("");

				if (connection.getErrorStream() != null) {
					try (BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getErrorStream(), UTF8DEFAULT))) {
						while (in.ready()) {
							errorInfo.append(in.readLine());
							errorInfo.append("  ");
						}
					} catch (IOException iox) {
						Logging.warning(this, "exception on reading error stream " + iox);
						throw new JSONCommunicationException("error on reading error stream");
					}
				}

				Logging.debug(this, "response code: " + connection.getResponseCode());

				if (connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED
						|| connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					conStat = new ConnectionState(ConnectionState.CONNECTED, "ok");
				} else {
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
					if (connection.getResponseCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
						// this case is handled by the login routine
						Logging.error(this, "Response " + connection.getResponseCode() + " "
								+ connection.getResponseMessage() + " " + errorInfo.toString());
					}

				}

				if (conStat.getState() == ConnectionState.CONNECTED) {
					// Retrieve session ID from response.
					String cookieVal = connection.getHeaderField("Set-Cookie");

					if (cookieVal != null) {
						lastSessionId = sessionId;
						sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));

						boolean gotNewSession = sessionId != null && !sessionId.equals(lastSessionId);

						if (gotNewSession) {
							Logging.info(this, "retrieveJSONObjects " + " got new session ");
						}
					}

					boolean gzipped = false;
					boolean deflated = false;
					boolean lz4compressed = false;

					if (connection.getHeaderField("Content-Encoding") != null) {
						gzipped = connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip");
						Logging.debug(this, "gzipped " + gzipped);
						deflated = connection.getHeaderField("Content-Encoding").equalsIgnoreCase("deflate");
						Logging.debug(this, "deflated " + deflated);
						lz4compressed = connection.getHeaderField("Content-Encoding").equalsIgnoreCase("lz4");
						Logging.debug(this, "lz4compressed " + lz4compressed);
					}

					InputStream stream = null;
					Logging.info(this, "initiating input stream");

					if (lz4compressed) {
						Logging.info(this, "initiating LZ4FrameInputStream");
						stream = new LZ4FrameInputStream(connection.getInputStream());
					} else if (gzipped || deflated) {
						if (deflated || connection.getHeaderField("Content-Type").startsWith("gzip-application")) {
							// not valid gzippt, we take inflater
							Logging.info(this, "initiating InflaterInputStream");
							InputStream str = connection.getInputStream();
							stream = new InflaterInputStream(str);
						} else {
							Logging.info(this, "initiating GZIPInputStream");
							stream = new GZIPInputStream(connection.getInputStream()); // not working, if no GZIP
						}
					} else {
						Logging.info(this, "initiating plain input stream");
						stream = connection.getInputStream();
					}

					Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

					String line;
					try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, UTF8DEFAULT))) {
						line = in.readLine();

						Logging.info(this, "received line of length " + line.length());
						if (line != null) {
							result = new JSONObject(line);
						}

						line = in.readLine();
						if (line != null) {
							Logging.debug(this, "received second line of length " + line.length());
						}
					} catch (IOException iox) {
						Logging.warning(this, "exception on receiving json", iox);
						throw new JSONCommunicationException("receiving json");
					}
				}
			} catch (Exception ex) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
				Logging.error(this, "Exception while data reading, " + ex.toString());
			}
		}

		timeCheck.stop("retrieveJSONObject  got result " + (result != null) + " ");
		Logging.info(this, "retrieveJSONObject ready");
		if (waitCursor != null) {
			waitCursor.stop();
		}
		return result;
	}
}
