/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.uib.opsicommand.MyHandshakeCompletedListener;
import de.uib.opsicommand.SecureSSLSocketFactory;
import de.uib.utilities.logging.Logging;

/**
 * {@code InsecureCeritifcateValidator} is an insecure implementation of
 * {@link CeritifcateValidator}.
 * <p>
 * {@code InsecureCertificateValidator} disables certificate and hostname
 * verification. The usage of this {@link CertificateValidator} is strongly
 * discouraged, since it includes multiple vulnerabilites. For validating
 * certificates it is best to use {@link SecureCertificateValidator}.
 * <p>
 * The {@code InsecureCertificateValidator} is only used for downloading a
 * certificate from the server for the first time.
 */
public class InsecureCertificateValidator implements CertificateValidator {
	InsecureCertificateValidator() {
	}

	@Override
	public SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sslFactory = null;

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { new InsecureX509TrustManager() }, new SecureRandom());
			sslFactory = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());
		} catch (NoSuchAlgorithmException e) {
			Logging.error(this, "provider doesn't support algorithm", e);
		} catch (KeyManagementException e) {
			Logging.error(this, "failed to initialize SSL context", e);
		}

		return sslFactory;
	}

	@Override
	public HostnameVerifier createHostnameVerifier() {
		return new InsecureHostnameVerifier();
	}

	@Override
	public boolean certificateLocallyAvailable() {
		return false;
	}

	@SuppressWarnings("java:S5527")
	private static class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			/* We disable hostname verification */
			return true;
		}
	}

	@SuppressWarnings("java:S4830")
	private static class InsecureX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// we skip certificate verification.
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// we skip certificate verification.
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}
