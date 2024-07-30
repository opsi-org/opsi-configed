/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import java.io.File;
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
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.uib.configed.Configed;
import de.uib.opsicommand.ConnectionErrorReporter;
import de.uib.opsicommand.ConnectionErrorType;
import de.uib.opsicommand.MyHandshakeCompletedListener;
import de.uib.opsicommand.SecureSSLSocketFactory;
import de.uib.utils.logging.Logging;

/**
 * {@code SecureCertificateValidator} is a secure implementation of
 * {@link CertificateValidator}.
 * <p>
 * {@code SecureCertificateValidator} verifies all locally available
 * certificates. However, if the connection with the server is established for
 * the first time only the downloaded client certificate from the server is
 * verified. It also verifies hostname with the server's certificate subject
 * alternative names (SAN).
 */
@SuppressWarnings("java:S1258")
public class SecureCertificateValidator implements CertificateValidator {
	private boolean certificateExists;
	private KeyStore ks;

	SecureCertificateValidator() {
		ks = CertificateManager.initializeKeyStore();
	}

	@Override
	public SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sslFactory = null;

		try {
			if (CertificateDownloader.getDownloadedCertificateFile() != null) {
				CertificateManager.loadCertificateToKeyStore(CertificateDownloader.getDownloadedCertificateFile());
			} else {
				CertificateManager.loadCertificatesToKeyStore();
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, new char[0]);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			X509TrustManager systemTrustManager = getSystemTrustManager(tmf);
			sslContext.init(kmf.getKeyManagers(),
					new X509TrustManager[] { new X509TrustManagerWrapper(systemTrustManager) }, new SecureRandom());

			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (KeyStoreException e) {
			Logging.error(this, e, "keystore wasn't initialized: ", e.toString());
		} catch (NoSuchAlgorithmException e) {
			Logging.error(this, e, "provider doesn't support algorithm");
		} catch (UnrecoverableKeyException e) {
			Logging.error(this, e, "unable to provide key");
		} catch (KeyManagementException e) {
			Logging.error(this, e, "failed to initialize SSL context: ", e.toString());
		}

		return sslFactory;
	}

	private static X509TrustManager getSystemTrustManager(TrustManagerFactory tmf) {
		TrustManager[] trustManagers = tmf.getTrustManagers();
		if (trustManagers.length != 1) {
			throw new IllegalStateException("Unexpected default trust ma gers: " + Arrays.toString(trustManagers));
		}
		TrustManager trustManager = trustManagers[0];
		if (trustManager instanceof X509TrustManager x509TrustManager) {
			return x509TrustManager;
		}
		throw new IllegalStateException("'" + trustManager + "' is not a X509TrustManager");
	}

	@Override
	public HostnameVerifier createHostnameVerifier() {
		return new MyHostnameVerifier();
	}

	@Override
	public boolean certificateLocallyAvailable() {
		return certificateExists;
	}

	/**
	 * {@code MyHostnameVerifier} is an additional hostname verifier to a
	 * standard hostname verifier. It only executes, when the standard hostname
	 * verifier determines that hostname is not verified.
	 * <p>
	 * This class is only here to inform user, that hostname verification failed
	 * and provide with valid hostnames (SAN), that server certificate accepts.
	 */
	@SuppressWarnings("java:S2972")
	private static class MyHostnameVerifier implements HostnameVerifier {
		private X509Certificate retrievePeerCertificate(SSLSession session) {
			try {
				Certificate[] peerCertificates = session.getPeerCertificates();

				if (peerCertificates.length > 0 && peerCertificates[0] instanceof X509Certificate x509Certificate) {
					return x509Certificate;
				} else {
					throw new IllegalStateException("Peer does not have any certificates or they aren't X.509");
				}
			} catch (SSLPeerUnverifiedException ex) {
				Logging.error(this, ex, "peer's identity wasn't verified");
			}

			return null;
		}

		private List<String> getSubjectAlternativeNames(X509Certificate certificate) {
			List<String> subjectAlternativeNames = new ArrayList<>();

			try {
				subjectAlternativeNames = certificate.getSubjectAlternativeNames().stream()
						.map(peerHostname -> (String) peerHostname.get(1)).toList();
			} catch (CertificateParsingException e) {
				Logging.warning(this, e, "problem in parsing certificate");
			}

			return subjectAlternativeNames;
		}

		@SuppressWarnings("java:S3516")
		@Override
		public boolean verify(String hostname, SSLSession session) {
			X509Certificate peerCertificate = retrievePeerCertificate(session);

			if (peerCertificate == null) {
				Logging.warning(this, "peer's certificate could not be retrieved");
				return false;
			}

			List<String> subjectAlternativeNames = getSubjectAlternativeNames(peerCertificate);

			if (subjectAlternativeNames == null || subjectAlternativeNames.isEmpty()) {
				Logging.warning(this, "no SAN found: ", subjectAlternativeNames);
				return false;
			}

			StringBuilder message = new StringBuilder();
			message.append(Configed.getResourceValue("SecureCertificateValidator.invalidHostname") + " ");
			message.append(hostname);
			message.append("\n\n");
			message.append(Configed.getResourceValue("SecureCertificateValidator.validHostnames"));
			message.append("\n\n");
			message.append(subjectAlternativeNames.toString().replace("[", "").replace("]", ""));
			ConnectionErrorReporter.getInstance().notify(message.toString(),
					ConnectionErrorType.INVALID_HOSTNAME_ERROR);

			return false;
		}
	}

	/**
	 * Wrapper of system's {@code X509TrustManager}.
	 * <p>
	 * This does not change how {@code X509Certificate} is validated. It only
	 * wraps around the system's {@code X509TrustManager} to check if the
	 * certificate is locally available before it verifies the certificate.
	 */
	@SuppressWarnings("java:S2972")
	private class X509TrustManagerWrapper implements X509TrustManager {
		X509TrustManager delegate;

		public X509TrustManagerWrapper(X509TrustManager delegate) {
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
