package de.uib.opsicommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
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

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

/**
 * @author Rupert Roeder, Naglis Vidziunas
 */

public class JSONthroughHTTPS extends JSONthroughHTTP {
	private static final boolean DISABLE_CERTIFICATE_VERIFICATION = true;

	public JSONthroughHTTPS(String host, String username, String password) {
		super(host, username, password);
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
		logging.info(this, "produceConnection, url; " + serviceURL);
		HttpURLConnection connection = (HttpURLConnection) serviceURL.openConnection();
		SSLSocketFactory sslFactory = null;
		if (DISABLE_CERTIFICATE_VERIFICATION) {
			sslFactory = createDullSSLSocketFactory();
		} else {
			sslFactory = createSSLSocketFactory();
		}
		((HttpsURLConnection) connection).setSSLSocketFactory(sslFactory);
		return connection;
	}

	// http://stackoverflow.com/questions/27075678/get-ssl-version-used-in-httpsurlconnection-java
	private class SecureSSLSocketFactory extends SSLSocketFactory {
		private final SSLSocketFactory delegate;
		private HandshakeCompletedListener handshakeListener;

		public SecureSSLSocketFactory(SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
			this.delegate = delegate;
			this.handshakeListener = handshakeListener;
		}

		@Override
		public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket(s, host, port, autoClose);
			logging.debug(this, "createSocket host, port: " + host + "," + port + " autoClose " + autoClose
					+ " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket() throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket();
			logging.debug(this,
					"createSocket " + " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));
			// on some connections there is, after some time, a javax.net.ssl.SSLException:
			// SSL peer shut down incorrectl
			// the standard enabled cipher suite seems to be TLS_RSA_WITH_AES_256_CBC_SHA256

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port);
			logging.debug(this, "createSocket host, port: " + host + "," + port + " enabled cipher suites "
					+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket(address, port, localAddress, localPort);
			logging.debug(this,
					"createSocket adress, port, localAddress, localPort: " + address + "," + port + "," + localAddress
							+ "," + localPort + " enabled cipher suites "
							+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port);
			logging.debug(this, "createSocket host, port: " + host + "," + port + " enabled cipher suites "
					+ Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
			SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port, localHost, localPort);
			logging.debug(this, "createSocket host, port, localHost, localPort: " + host + "," + port + "," + localHost
					+ "," + localPort + " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));

			if (null != this.handshakeListener) {
				socket.addHandshakeCompletedListener(this.handshakeListener);
			}

			return socket;
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return this.delegate.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return this.delegate.getSupportedCipherSuites();
		}
	}

	public class MyHandshakeCompletedListener implements HandshakeCompletedListener {
		@Override
		public void handshakeCompleted(HandshakeCompletedEvent event) {
			SSLSession session = event.getSession();
			String protocol = session.getProtocol();
			String cipherSuite = session.getCipherSuite();
			String peerName = null;

			try {
				peerName = session.getPeerPrincipal().getName();
			} catch (SSLPeerUnverifiedException e) {
				logging.error(this, "peer's identity wasn't verified");
			}

			logging.info(this, "protocol " + protocol + "  peerName " + peerName);
			logging.info(this, "cipher suite " + cipherSuite);
		}
	}

	private SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sslFactory = null;

		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			loadCertificateToKeyStore(ks);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, new char[0]);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (CertificateException e) {
			logging.error(this, "something is wrong with the certificate");
		} catch (KeyStoreException e) {
			logging.error(this, "keystore wasn't initialized");
		} catch (NoSuchAlgorithmException e) {
			logging.error(this, "provider doesn't support algorithm");
		} catch (UnrecoverableKeyException e) {
			logging.error(this, "unable to provide key");
		} catch (KeyManagementException e) {
			logging.error(this, "failed to initialize SSL context");
		} catch (IOException e) {
			logging.error(this, "something is wrong with the keystore data");
		}

		return sslFactory;
	}

	private void loadCertificateToKeyStore(KeyStore ks) {
		List<File> certificates = getCertificates();

		File certificateFile = null;
		if (trustAlways) {
			certificateFile = downloadCertificateFile();
			saveCertificate(certificateFile);
		} else if (trustOnlyOnce) {
			certificateFile = downloadCertificateFile();
		}

		if ((trustAlways || trustOnlyOnce) && certificateFile == null) {
			return;
		}

		if (trustAlways || trustOnlyOnce) {
			try {
				X509Certificate certificate = instantiateCertificate(certificateFile);
				String alias = host;
				if (certificateFile.exists()) {
					alias = certificateFile.getName().substring(0, certificateFile.getName().indexOf("-"));
				}
				ks.setCertificateEntry(alias, certificate);
			} catch (KeyStoreException e) {
				logging.error(this, "unable to load certificate into a keystore");
			}
		}

		if (!certificates.isEmpty()) {
			certificates.forEach(certificate -> {
				try {
					String alias = host;
					if (certificate.exists()) {
						alias = certificate.getName().substring(0, certificate.getName().indexOf("-"));
					}
					ks.setCertificateEntry(alias, instantiateCertificate(certificate));
				} catch (KeyStoreException e) {
					logging.error(this, "unable to load certificate into a keystore");
				}
			});
		}
	}

	private List<File> getCertificates() {
		File certificateDir = new File(configed.savedStatesLocationName);
		File[] certificateFiles = certificateDir.listFiles((dir, filename) -> filename.endsWith(".pem"));

		if (certificateFiles.length == 0) {
			certificateExists = false;
			return new ArrayList<>();
		}

		return Arrays.asList(certificateFiles);
	}

	private void saveCertificate(File certificateFile) {
		try {
			Files.copy(certificateFile.toPath(),
					new File(configed.savedStatesLocationName, host + "-" + Globals.CERTIFICATE_FILE).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logging.error(this, "unable to save certificate");
		}
	}

	private X509Certificate instantiateCertificate(File certificateFile) {
		X509Certificate cert = null;

		try (FileInputStream is = new FileInputStream(certificateFile)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(is);
		} catch (CertificateException e) {
			logging.error(this, "unable to parse certificate (format is invalid)");
		} catch (FileNotFoundException e) {
			logging.error(this, "unable to find certificate");
		} catch (IOException e) {
			logging.error(this, "unable to close certificate");
		}

		return cert;
	}

	private File downloadCertificateFile() {
		SSLSocketFactory sslFactory = createDullSSLSocketFactory();
		HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);

		URL url = null;
		File tempCertFile = null;

		try {
			url = new URL(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE));
			tempCertFile = File.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION);
		} catch (MalformedURLException e) {
			logging.error(this, "url is malformed: " + url);
		} catch (IOException e) {
			logging.error(this, "unable to create tmp certificate file");
		}

		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream fos = new FileOutputStream(tempCertFile)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			logging.error(this, "unable to download certificate's content from specified url: " + url.toString());
		}

		return tempCertFile;
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
			logging.error(this, "provider doesn't support algorithm");
		} catch (KeyManagementException e) {
			logging.error(this, "failed to initialize SSL context");
		}

		return sslFactory;
	}
}
