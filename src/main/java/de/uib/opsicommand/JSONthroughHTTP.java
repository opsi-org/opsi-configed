/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.swing.SwingUtilities;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.swing.FEditRecord;
import net.jpountz.lz4.LZ4FrameInputStream;

public class JSONthroughHTTP extends AbstractPOJOExecutioner {

	public static final Charset UTF8DEFAULT = StandardCharsets.UTF_8;
	public static final int DEFAULT_PORT = 4447;

	private static final int POST = 0;
	protected String host;
	public String username;
	public String password;
	private int portHTTP = 4444;
	protected int portHTTPS = DEFAULT_PORT;
	protected URL serviceURL;
	public String sessionId;
	private int requestMethod = POST;
	protected boolean certificateExists;
	protected boolean trustOnlyOnce;
	protected boolean trustAlways;
	private FEditRecord newPasswordDialog;

	private static class JSONCommunicationException extends Exception {
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

	public void makeURL() {
		if (serviceURL != null) {
			return;
		}

		Logging.debug(this, "make url ");

		String urlS = produceBaseURL("/rpc");

		try {
			serviceURL = new URL(urlS);
		} catch (MalformedURLException ex) {
			Logging.error("Malformed URL: " + urlS, ex);
		}
	}

	private static String produceJSONstring(OpsiMethodCall omc) {
		return omc.getJsonString();
	}

	/**
	 * Opening the connection. The method is prepared to be subclassed and take
	 * additional informations for the connection.
	 */
	protected HttpURLConnection produceConnection() throws IOException {
		return (HttpURLConnection) serviceURL.openConnection();
	}

	public boolean showNewPasswordDialog() {
		Logging.info("Unauthorized, show password dialog");
		if (newPasswordDialog != null) {
			return false;
		}

		Map<String, String> groupData = new LinkedHashMap<>();
		groupData.put("password", "");
		Map<String, String> labels = new HashMap<>();
		labels.put("password", Configed.getResourceValue("DPassword.jLabelPassword"));
		Map<String, Boolean> editable = new HashMap<>();
		editable.put("password", true);
		Map<String, Boolean> secrets = new HashMap<>();
		secrets.put("password", true);

		newPasswordDialog = new FEditRecord(Configed.getResourceValue("JSONthroughHTTP.provideNewPassword"));
		newPasswordDialog.setRecord(groupData, labels, null, editable, secrets);
		newPasswordDialog
				.setTitle(Configed.getResourceValue("JSONthroughHTTP.enterNewPassword") + " (" + Globals.APPNAME + ")");
		newPasswordDialog.init();
		newPasswordDialog.setSize(420, 210);
		newPasswordDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		newPasswordDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent event) {
				// For some unknown reason the paint method isn't
				// called, when dialog is initialized in Windows
				// OS. To fix that we call paint method manually
				// by requesting the dialog to be repainted, when
				// it is opened.
				newPasswordDialog.repaint();
				newPasswordDialog.setDataChanged(true);
			}
		});

		newPasswordDialog.setModal(true);
		newPasswordDialog.setAlwaysOnTop(true);
		newPasswordDialog.setVisible(true);

		boolean cancelled = newPasswordDialog.isCancelled();
		ConfigedMain.password = newPasswordDialog.getData().get("password");
		password = newPasswordDialog.getData().get("password");

		newPasswordDialog = null;

		return !cancelled;
	}

	/**
	 * This method receives response via HTTP in MessagePack format.
	 */
	@Override
	public synchronized Map<String, Object> retrieveResponse(OpsiMethodCall omc) {
		boolean background = false;
		Logging.info(this, "retrieveResponse started");
		//WaitCursor waitCursor = null;

		if (omc != null && !omc.isBackgroundDefault()) {
			//waitCursor = new WaitCursor(null, new Cursor(Cursor.DEFAULT_CURSOR), this.getClass().getName());
		} else {
			background = true;
		}

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);

		makeURL();

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + omc);
		timeCheck.start();

		HttpURLConnection connection = null;
		try {
			connection = produceConnection();
			// the underlying network connection can be shared,
			// only disconnect() may close the underlying socket

			if (requestMethod == POST) {
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setUseCaches(false);
			} else {
				connection.setRequestMethod("GET");
			}

			Logging.debug(this, "https protocols given by system " + Configed.SYSTEM_SSL_VERSION);
			Logging.info(this,
					"retrieveResponse method=" + connection.getRequestMethod() + ", headers="
							+ connection.getRequestProperties() + ", cookie="
							+ (sessionId == null ? "null" : (sessionId.substring(0, 26) + "...")));

			if (sessionId != null) {
				connection.setRequestProperty("Cookie", sessionId);
			}

			try {
				connection.connect();
			} catch (Exception ex) {
				String s = "" + ex;
				int i = s.indexOf("Unsupported ciphersuite");
				if (i > -1) {
					s = "\n\n" + s.substring(i) + "\nIn this SSL configuration, a connection is not possible";

					Logging.error(s);
					Logging.checkErrorList(null);
				}

				throw ex;
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
			Logging.debug(this, "SSLException encountered: " + ex);
			if (!background) {
				//if (waitCursor != null) {
				//	waitCursor.stop();
				//}
				//WaitCursor.stopAll();
			}

			if (conStat.getState() == ConnectionState.INTERRUPTED) {
				return null;
			}

			final StringBuilder message = new StringBuilder();

			message.append(Configed.getResourceValue("JSONthroughHTTP.certificateWarning") + "\n\n");

			if (!certificateExists) {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsUnverified") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.noCertificateFound"));
			} else {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsInvalid") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.unableToVerify"));
			}

			message.append("\n\n");
			message.append(Configed.getResourceValue("JSONthroughHTTP.stillConnectToServer"));

			final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("JSONthroughHTTP.failedServerVerification"), true,
					new String[] { Configed.getResourceValue(Configed.getResourceValue("UIManager.cancelButtonText")),
							Configed.getResourceValue("JSONthroughHTTP.alwaysTrust"),
							Configed.getResourceValue("JSONthroughHTTP.trustOnlyOnce") },
					420, 260);

			fErrorMsg.setTooltipButtons(null, Configed.getResourceValue("JSONthroughHTTP.alwaysTrustTooltip"),
					Configed.getResourceValue("JSONthroughHTTP.trustOnlyOnceTooltip"));

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

					if (ConfigedMain.getMainFrame() == null && ConfigedMain.dPassword != null) {
						fErrorMsg.setLocationRelativeTo(ConfigedMain.dPassword);
					}

					fErrorMsg.setVisible(true);
				});
			} catch (InterruptedException e) {
				Logging.info("Thread was interrupted");
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				Logging.debug("exception thrown during doRun: " + e);
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
				//if (waitCursor != null) {
				//	waitCursor.stop();
				//}
				//WaitCursor.stopAll();
			}

			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, ", ex);

			return null;
		}

		Map<String, Object> result = new HashMap<>();

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				Logging.debug(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

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
				} else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());

					Logging.debug("Unauthorized: background=" + background + ", " + sessionId + ", mfa="
							+ Globals.isMultiFactorAuthenticationEnabled);
					if (Globals.isMultiFactorAuthenticationEnabled && ConfigedMain.getMainFrame() != null) {
						if (!background) {
							//	if (waitCursor != null) {
							//		waitCursor.stop();
							//	}
							//	WaitCursor.stopAll();
						}
						if (showNewPasswordDialog()) {
							return retrieveResponse(omc);
						}
					}
				} else {
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
					Logging.error(this, "Response " + connection.getResponseCode() + " "
							+ connection.getResponseMessage() + " " + errorInfo.toString());
				}

				if (conStat.getState() == ConnectionState.CONNECTED) {
					// Retrieve session ID from response.
					String cookieVal = connection.getHeaderField("Set-Cookie");

					if (cookieVal != null) {
						String lastSessionId = sessionId;
						sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));

						boolean gotNewSession = sessionId != null && !sessionId.equals(lastSessionId);

						if (gotNewSession) {
							Logging.info(this, "retrieveResponse got new session");
						}
					}

					boolean gzipped = false;
					boolean deflated = false;
					boolean lz4compressed = false;

					if (connection.getHeaderField("Content-Encoding") != null) {
						gzipped = "gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
						Logging.debug(this, "gzipped " + gzipped);
						deflated = "deflate".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
						Logging.debug(this, "deflated " + deflated);
						lz4compressed = "lz4".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
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

							// not working, if no GZIP
							stream = new GZIPInputStream(connection.getInputStream());
						}
					} else {
						Logging.info(this, "initiating plain input stream");
						stream = connection.getInputStream();
					}

					Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

					if (connection.getContentType().contains("application/json")) {
						ObjectMapper mapper = new ObjectMapper();
						result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
						});
					} else if (connection.getContentType().contains("application/msgpack")) {
						ObjectMapper mapper = new MessagePackMapper();
						result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
						});
					} else {
						Logging.error(this, "Unsupported Content-Type: " + connection.getContentType());
					}
				}
			} catch (Exception ex) {
				//if (waitCursor != null) {
				//	waitCursor.stop();
				//}
				//WaitCursor.stopAll();
				Logging.error(this, "Exception while data reading", ex);
			}
		}

		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");
		//if (waitCursor != null) {
		//	waitCursor.stop();
		//}
		return result;
	}
}
