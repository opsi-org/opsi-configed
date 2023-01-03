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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
import de.uib.utilities.logging.logging;

/**
 * @author Rupert Roeder, Naglis Vidziunas
 */

public class JSONthroughHTTPS extends JSONthroughHTTP {
	private static final boolean DISABLE_CERTIFICATE_VERIFICATION = false;

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
			loadCertificatesToKeyStore(ks);

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

	private void loadCertificatesToKeyStore(KeyStore ks) {
		if (trustAlways || trustOnlyOnce) {
			File certificateFile = downloadCertificateFile();

			if (certificateFile == null) {
				return;
			}

			loadCertificateToKeyStore(ks, certificateFile);

			if (trustAlways) {
				CertificateManager.saveCertificate(certificateFile);
			}
		} else {
			List<File> certificates = CertificateManager.getCertificates();

			if (!certificates.isEmpty()) {
				certificates.forEach(certificate -> loadCertificateToKeyStore(ks, certificate));
			}
		}
	}

	private void loadCertificateToKeyStore(KeyStore ks, File certificateFile) {
		try {
			X509Certificate certificate = CertificateManager.instantiateCertificate(certificateFile);
			String alias = host;
			if (certificateFile.exists()) {
				alias = certificateFile.getName().substring(0, certificateFile.getName().indexOf("-"));
			}
			ks.setCertificateEntry(alias, certificate);
		} catch (KeyStoreException e) {
			logging.error(this, "unable to load certificate into a keystore");
		}
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
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// we skip certificate verification.
			}

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
				certificateFiles.forEach(certificateFile -> {
					if (certificate.equals(CertificateManager.instantiateCertificate(certificateFile))) {
						certificateExists = true;
						return;
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
