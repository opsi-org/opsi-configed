package de.uib.opsicommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

/**
 * @author Rupert Roeder, Naglis Vidziunas
 */

public class JSONthroughHTTPS extends JSONthroughHTTP {
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
	protected HttpURLConnection produceConnection() throws java.io.IOException {
		logging.info(this, "produceConnection, url; " + serviceURL);
		HttpURLConnection connection = (HttpURLConnection) serviceURL.openConnection();
		SSLSocketFactory sslFactory = createSSLSocketFactory();
		((HttpsURLConnection) connection).setSSLSocketFactory(sslFactory);
		return connection;
	}

	private class SecureSSLSocketFactory extends SSLSocketFactory
	// http://stackoverflow.com/questions/27075678/get-ssl-version-used-in-httpsurlconnection-java
	{
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
					"createSocke " + " enabled cipher suites " + Arrays.toString(socket.getEnabledCipherSuites()));
			// on some connections there is, after some time, a javax.net.ssl.SSLException:
			// SSL peer shut down incorrectl
			// the standard enabled cipher suite seems to be TLS_RSA_WITH_AES_256_CBC_SHA256

			if (!configed.TLS_CIPHER_SUITE.equals(""))
				socket.setEnabledCipherSuites(new String[] { configed.TLS_CIPHER_SUITE });
			// TLS_RSA_WITH_AES_128_GCM_SHA256"}); // with this suit it seems to work

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
			File certificate = getLocalCertificate();
			X509Certificate cer = instantiateCertificate(certificate);

			if (cer == null) {
				System.out.println("Couldn't instantiate certificate");
				return null;
			}

			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry(host, cer);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, new char[0]);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			logging.error(this, "keystore wasn't initialized");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logging.error(this, "provider doesn't support algorithm");
		} catch (UnrecoverableKeyException e) {
			logging.error(this, "unable to provide key");
		} catch (KeyManagementException e) {
			logging.error(this, "failed to initialize SSL context");
		} catch (IOException e) {
			logging.error(this, "something is wrong with keystore data");
		}

		return sslFactory;
	}

	private X509Certificate instantiateCertificate(File certificate) {
		X509Certificate cert = null;

		try (FileInputStream is = new FileInputStream(certificate)) {
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

	private File getLocalCertificate() {
		String configPath = getConfigPath();
		File certificate = new File(String.format("%s/opsi-ca-cert.pem", configPath));

		if (!certificate.exists()) {
			return null;
		}

		return certificate;
	}

	private String getConfigPath() {
		String result = "";

		if (System.getenv(logging.windowsEnvVariableAppDataDirectory) != null) {
			result = System.getenv(logging.windowsEnvVariableAppDataDirectory) + File.separator + "opsi.org"
					+ File.separator + "configed";
		} else {
			result = System.getProperty(logging.envVariableForUserDirectory) + File.separator + ".configed";
		}

		return result;
	}
}
