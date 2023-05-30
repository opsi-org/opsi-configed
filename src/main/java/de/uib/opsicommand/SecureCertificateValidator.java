/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

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
import java.util.stream.Collectors;

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
import de.uib.utilities.logging.Logging;

public class SecureCertificateValidator implements CertificateValidator {
	private boolean certificateExists;

	@Override
	public SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sslFactory = null;

		try {
			KeyStore ks = CertificateManager.initializeKeyStore();

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

	@Override
	public HostnameVerifier createHostnameVerifier() {
		return new MyHostnameVerifier();
	}

	@Override
	public boolean certificateLocallyAvailable() {
		return certificateExists;
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
				return true;
			} else {
				StringBuilder message = new StringBuilder();
				message.append(Configed.getResourceValue("JSONthroughHTTP.unvalidHostname") + " ");
				message.append(hostname);
				message.append("\n\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.validHostnames"));
				message.append("\n\n");
				message.append(certificateSubjectAlternativeNames.toString().replace("[", "").replace("]", ""));
				ConnectionErrorObserver.getInstance().notify(message.toString(),
						ConnectionErrorType.INVALID_HOSTNAME_ERROR);

				return false;
			}
		}
	}

	// Wrapper of systems X509TrustManager. This does not change how X509Certificates
	// are validated. It only wraps around the system's X509TrustManager to check if
	// the certificate is locally available before it verifies the certificate.
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
