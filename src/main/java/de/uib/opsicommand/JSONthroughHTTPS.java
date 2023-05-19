package de.uib.opsicommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;

import org.apache.maven.artifact.versioning.ComparableVersion;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;

/**
 * @author Rupert Roeder, Naglis Vidziunas
 */

public class JSONthroughHTTPS extends JSONthroughHTTP {
	private static final Pattern versionPattern = Pattern.compile("opsiconfd ([\\d\\.]+)");
	private static final int EXPECTED_SERVER_VERSION_LENGTH = 4;

	private static int[] serverVersion = { 0, 0, 0, 0 };
	private static boolean isOpsi43;
	private static String serverVersionString = "4.2";
	private static ComparableVersion serverComparableVersion = new ComparableVersion(serverVersionString);

	private static boolean gzipTransmission;
	private static boolean lz4Transmission;

	// By default we set hostnameVerified to true, because MyHostnameVerifier is
	// only used to check hostname verification, when default determines that
	// hostname is unverified.
	//
	// The MyHostnameVerifier class sets this variable to false, when it is
	// required. 
	private boolean hostnameVerified = true;

	public JSONthroughHTTPS(String host, String username, String password) {
		super(host, username, password);
	}

	private static void setServerVersion(int[] newServerVersion) {

		if (newServerVersion == null || newServerVersion.length == 0) {
			return;
		}

		serverVersion = Arrays.copyOf(newServerVersion, newServerVersion.length);

		// Produce String of server version 
		StringBuilder serverVersionBuilder = new StringBuilder(String.valueOf(serverVersion[0]));

		for (int i = 1; i < serverVersion.length; i++) {
			serverVersionBuilder.append(".");
			serverVersionBuilder.append(String.valueOf(serverVersion[i]));
		}

		setServerVersion(serverVersionBuilder.toString());
	}

	public static void setServerVersion(String newServerVersion) {

		serverVersionString = newServerVersion;
		serverComparableVersion = new ComparableVersion(serverVersionString);

		if (isServerVersionAtLeast("4.2")) {
			gzipTransmission = false;
			lz4Transmission = true;
		} else {
			gzipTransmission = true;
			lz4Transmission = false;

			// The way we check the certificate does not work before opsi server version 4.2
			Globals.disableCertificateVerification = true;
		}

		if (isServerVersionAtLeast("4.3")) {
			isOpsi43 = true;
		}

		Logging.info("we set the server version: " + serverVersionString);
		Logging.info("we use now gzip: " + gzipTransmission + " or lz4: " + lz4Transmission);
		Logging.info("is certificateVerification disabled? " + Globals.disableCertificateVerification);
	}

	/*
	 * returns true, if the server has a newer version (or same version)
	 * compared to the version in the argument
	 * 
	 * @arg compareVersion version to compare to of format x.y.z...
	 */
	private static boolean isServerVersionAtLeast(String compareVersion) {

		return serverComparableVersion.compareTo(new ComparableVersion(compareVersion)) >= 0;
	}

	public static boolean isOpsi43() {
		return isOpsi43;
	}

	public static String getServerVersion() {
		return serverVersionString;
	}

	// Checks if the Server version is already known, and loads it otherwise
	private void checkServerVersion() {
		if (serverVersion[0] != 0) {
			return;
		}

		HttpsURLConnection connection;

		try {
			connection = (HttpsURLConnection) new URL(produceBaseURL("/")).openConnection();
			String authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + authorization);
			connection.setSSLSocketFactory(createDullSSLSocketFactory());
			connection.setHostnameVerifier(new DullHostnameVerifier());
			connection.setRequestMethod("HEAD");

		} catch (IOException e) {
			Logging.warning(this, "error in testing connection to server for getting server opsi version", e);
			return;
		}

		String server = connection.getHeaderField("Server");

		if (server == null) {
			Logging.warning("error in getting server version, Headerfield is null");
			return;
		}

		int[] newServerVersion = new int[EXPECTED_SERVER_VERSION_LENGTH];

		Matcher matcher = versionPattern.matcher(server);
		if (matcher.find()) {
			Logging.info(this, "opsi server version: " + matcher.group(1));
			String[] versionParts = matcher.group(1).split("\\.");
			for (int i = 0; i < versionParts.length && i < EXPECTED_SERVER_VERSION_LENGTH; i++) {
				try {
					newServerVersion[i] = Integer.parseInt(versionParts[i]);
				} catch (NumberFormatException nex) {
					Logging.error(this, "value is unparsable to int");
				}
			}
		} else {
			// Default is 4.1, if this query does not work

			Logging.info("we set opsi version 4.1 because we did not find opsiconfd version in header");
			newServerVersion[0] = 4;
			newServerVersion[1] = 1;
		}

		setServerVersion(newServerVersion);
	}

	private void setGeneralRequestProperties(HttpURLConnection connection) {
		String authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		connection.setRequestProperty("Authorization", "Basic " + authorization);

		// has to be value between 1 and 43300 [sec]
		connection.setRequestProperty("X-opsi-session-lifetime", "900");

		if (lz4Transmission) {
			connection.setRequestProperty("Accept-Encoding", "lz4");
		} else if (gzipTransmission) {
			connection.setRequestProperty("Accept-Encoding", "gzip");
		}

		connection.setRequestProperty("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		connection.setRequestProperty("Accept", "application/msgpack");
	}

	@Override
	protected String produceBaseURL(String rpcPath) {
		return "https://" + host + ":" + portHTTPS + rpcPath;
	}

	/**
	 * Opening the connection and set the SSL parameters
	 */
	@Override
	protected HttpURLConnection produceConnection() throws IOException {

		checkServerVersion();

		Logging.info(this, "produceConnection, url; " + serviceURL);
		HttpsURLConnection connection = (HttpsURLConnection) serviceURL.openConnection();

		SSLSocketFactory sslFactory = null;
		HostnameVerifier hostnameVerifier = null;
		if (Globals.disableCertificateVerification) {
			sslFactory = createDullSSLSocketFactory();
			hostnameVerifier = new DullHostnameVerifier();
		} else {
			sslFactory = createSSLSocketFactory();
			hostnameVerifier = new MyHostnameVerifier();
		}
		connection.setSSLSocketFactory(sslFactory);
		connection.setHostnameVerifier(hostnameVerifier);

		setGeneralRequestProperties(connection);

		return connection;
	}

	// http://stackoverflow.com/questions/27075678/get-ssl-version-used-in-httpsurlconnection-java
	private static class SecureSSLSocketFactory extends SSLSocketFactory {
		private final SSLSocketFactory delegate;
		private HandshakeCompletedListener handshakeListener;

		public SecureSSLSocketFactory(SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
			this.delegate = delegate;
			this.handshakeListener = handshakeListener;
		}

		@Override
		@SuppressWarnings("squid:S1192")
		public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
			Logging.debug(this, "createSocket host, port: " + host + "," + port + " autoClose " + autoClose
					+ " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket() throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket();
			Logging.debug(this,
					"createSocket " + " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));
			// on some connections there is, after some time, a javax.net.ssl.SSLException:
			// SSL peer shut down incorrect
			// the standard enabled cipher suite seems to be TLS_RSA_WITH_AES_256_CBC_SHA256

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
			Logging.debug(this, "createSocket host, port: " + host + "," + port + " enabled cipher suites "
					+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
			Logging.debug(this,
					"createSocket adress, port, localAddress, localPort: " + address + "," + port + "," + localAddress
							+ "," + localPort + " enabled cipher suites "
							+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
			Logging.debug(this, "createSocket host, port: " + host + "," + port + " enabled cipher suites "
					+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
			SSLSocket socket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
			Logging.debug(this, "createSocket host, port, localHost, localPort: " + host + "," + port + "," + localHost
					+ "," + localPort + " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != handshakeListener) {
				socket.addHandshakeCompletedListener(handshakeListener);
			}

			return socket;
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return delegate.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return delegate.getSupportedCipherSuites();
		}
	}

	public static class MyHandshakeCompletedListener implements HandshakeCompletedListener {
		@Override
		public void handshakeCompleted(HandshakeCompletedEvent event) {
			SSLSession session = event.getSession();
			String protocol = session.getProtocol();
			String cipherSuite = session.getCipherSuite();
			String peerName = null;

			try {
				peerName = session.getPeerPrincipal().getName();
			} catch (SSLPeerUnverifiedException e) {
				Logging.error(this, "peer's identity wasn't verified", e);
			}

			Logging.info(this, "protocol " + protocol + "  peerName " + peerName);
			Logging.info(this, "cipher suite " + cipherSuite);
		}
	}

	private SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sslFactory = null;

		try {
			KeyStore ks = CertificateManager.initializeKeyStore();
			loadCertificatesToKeyStore();

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, new char[0]);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			X509TrustManager systemTrustManager = getSystemTrustManager(tmf);
			sslContext.init(kmf.getKeyManagers(),
					new X509TrustManager[] { new DefaultX509TrustManagerWrapper(systemTrustManager) },
					new SecureRandom());

			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (KeyStoreException e) {
			Logging.error(this, "keystore wasn't initialized: " + e.toString(), e);
		} catch (NoSuchAlgorithmException e) {
			Logging.error(this, "provider doesn't support algorithm", e);
		} catch (UnrecoverableKeyException e) {
			Logging.error(this, "unable to provide key", e);
		} catch (KeyManagementException e) {
			Logging.error(this, "failed to initialize SSL context: " + e.toString(), e);
		}

		return sslFactory;
	}

	public static X509TrustManager getSystemTrustManager(TrustManagerFactory tmf) {
		TrustManager[] trustManagers = tmf.getTrustManagers();
		if (trustManagers.length != 1) {
			throw new IllegalStateException("Unexpected default trust managers: " + Arrays.toString(trustManagers));
		}
		TrustManager trustManager = trustManagers[0];
		if (trustManager instanceof X509TrustManager) {
			return (X509TrustManager) trustManager;
		}
		throw new IllegalStateException("'" + trustManager + "' is not a X509TrustManager");
	}

	private void loadCertificatesToKeyStore() {
		if (trustAlways || trustOnlyOnce) {
			String url = produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE);
			File certificateFile = downloadCertificateFile(url);

			if (certificateFile == null) {
				if (!hostnameVerified) {
					return;
				}

				StringBuilder message = new StringBuilder();
				message.append(Configed.getResourceValue("JSONthroughHTTP.unableToDownloadCertificate") + " " + url);
				displayErrorDialog(message.toString());

				conStat = new ConnectionState(ConnectionState.INTERRUPTED);
				return;
			}

			CertificateManager.loadCertificateToKeyStore(certificateFile);

			if (trustAlways) {
				trustAlways = false;
				CertificateManager.saveCertificate(certificateFile);
			}

			deleteFile(certificateFile);
		} else {
			CertificateManager.loadCertificatesToKeyStore();
		}
	}

	private File downloadCertificateFile(String urlPath) {
		SSLSocketFactory sslFactory = createDullSSLSocketFactory();
		HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);
		HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());

		URL url = null;

		try {
			url = new URL(urlPath);
		} catch (MalformedURLException e) {
			Logging.error(this, "url is malformed: " + url);
		}

		if (url == null) {
			return null;
		}

		File tmpCertFile = null;

		try {
			tmpCertFile = File.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION);
		} catch (IOException e) {
			Logging.error(this, "unable to create tmp certificate file", e);
		}

		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream fos = new FileOutputStream(tmpCertFile)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			Logging.error(this, "unable to download certificate from specified url: " + url.toString(), e);

			if (tmpCertFile != null && tmpCertFile.exists()) {
				deleteFile(tmpCertFile);
				tmpCertFile = null;
			}
		}

		return tmpCertFile;
	}

	private void deleteFile(File file) {
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			Logging.error(this, "unable to delete file", e);
		}
	}

	private SSLSocketFactory createDullSSLSocketFactory() {
		// Create a new trust manager that trust all certificates
		@SuppressWarnings("squid:S4830")
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// we skip certificate verification.
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// we skip certificate verification.
			}
		} };

		SSLSocketFactory sslFactory = null;

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (NoSuchAlgorithmException e) {
			Logging.error(this, "provider doesn't support algorithm", e);
		} catch (KeyManagementException e) {
			Logging.error(this, "failed to initialize SSL context", e);
		}

		return sslFactory;
	}

	private static void displayErrorDialog(String message) {
		SwingUtilities.invokeLater(() -> {
			FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("JSONthroughHTTP.failedServerVerification"), true,
					new String[] { Configed.getResourceValue(Configed.getResourceValue("FGeneralDialog.ok")) }, 420,
					200);

			fErrorMsg.setMessage(message);
			fErrorMsg.setAlwaysOnTop(true);

			if (ConfigedMain.getMainFrame() == null && ConfigedMain.dPassword != null) {
				fErrorMsg.setLocationRelativeTo(ConfigedMain.dPassword);
			}

			fErrorMsg.setVisible(true);
		});
	}

	// MyHostnameVerifier is an additional hostname verifier to a standard
	// hostname verifier. It only executes, when the standard hostname
	// verifier determines that hostname is not verified.
	//
	// This class is only here to inform user, that hostname verification
	// failed and provide with valid hostnames, that server certificate
	// accepts.
	private class MyHostnameVerifier implements HostnameVerifier {
		private List<String> certificateSubjectAlternativeNames;

		private boolean hostnameMatches(String hostname, SSLSession session) {
			boolean validCertificate = false;

			try {
				Certificate[] peerCertificates = session.getPeerCertificates();

				if (peerCertificates.length > 0 && peerCertificates[0] instanceof X509Certificate) {
					X509Certificate peerCertificate = (X509Certificate) peerCertificates[0];

					validCertificate = verifyCertificate(hostname, peerCertificate);

					if (!validCertificate) {
						certificateSubjectAlternativeNames = getSubjectAlternativeNames(peerCertificate);
					}
				} else {
					throw new IllegalStateException("Peer does not have any certificates or they aren't X.509");
				}
			} catch (SSLPeerUnverifiedException ex) {
				Logging.error(this, "peer's identity wasn't verified", ex);
			}

			return validCertificate;
		}

		private boolean verifyCertificate(String hostname, X509Certificate certificate) {
			boolean validCertificate = false;

			try {
				validCertificate = certificate.getSubjectAlternativeNames().stream()
						.anyMatch(peerHostname -> hostname.equals(peerHostname.get(1)));
			} catch (CertificateException ex) {
				Logging.warning(this, "certificate exception, could not validate certificate", ex);
				validCertificate = false;
			}

			return validCertificate;
		}

		private List<String> getSubjectAlternativeNames(X509Certificate certificate) {
			List<String> subjectAlternativeNames = new ArrayList<>();

			try {
				subjectAlternativeNames = certificate.getSubjectAlternativeNames().stream()
						.map(peerHostname -> (String) peerHostname.get(1)).collect(Collectors.toList());
			} catch (CertificateParsingException e) {
				Logging.warning(this, "problem in parsing certificate", e);
			}

			return subjectAlternativeNames;
		}

		@Override
		public boolean verify(String hostname, SSLSession session) {
			if (hostnameMatches(hostname, session)) {
				hostnameVerified = true;
				return true;
			} else {
				StringBuilder message = new StringBuilder();
				message.append(Configed.getResourceValue("JSONthroughHTTP.unvalidHostname") + " ");
				message.append(hostname);
				message.append("\n\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.validHostnames"));
				message.append("\n\n");
				message.append(certificateSubjectAlternativeNames.toString().replace("[", "").replace("]", ""));
				displayErrorDialog(message.toString());

				hostnameVerified = false;
				conStat = new ConnectionState(ConnectionState.INTERRUPTED);
				return false;
			}
		}
	}

	@SuppressWarnings("java:S5527")
	private static class DullHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			/* We disable hostname verification */
			return true;
		}
	}

	// Wrapper of systems X509TrustManager. This does not change how X509Certificates
	// are validated. It only wraps around the system's X509TrustManager to check if
	// the certificate is locally available before it verifies the certificate.
	private class DefaultX509TrustManagerWrapper implements X509TrustManager {
		X509TrustManager delegate;

		public DefaultX509TrustManagerWrapper(X509TrustManager delegate) {
			this.delegate = delegate;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
			delegate.checkClientTrusted(certificates, authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
			List<File> certificateFiles = CertificateManager.getCertificates();
			for (X509Certificate certificate : certificates) {
				certificateFiles.forEach((File certificateFile) -> {
					if (certificate.equals(CertificateManager.instantiateCertificate(certificateFile))) {
						certificateExists = true;
						return;
					} else {
						certificateExists = false;
					}
				});
			}
			delegate.checkServerTrusted(certificates, authType);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return delegate.getAcceptedIssuers();
		}
	}
}
