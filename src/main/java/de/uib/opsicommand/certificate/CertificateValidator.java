/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public interface CertificateValidator {
	/**
	 * Creates {@code SSLSocketFactory} based on the
	 * {@link CertificateValidator} implementation (i.e. either secure or not).
	 * <p>
	 * Created {@code SSLSocketFactory} is then used to either verify the
	 * client's certificate with the server's certificate or not.
	 * 
	 * @return created {@code SSLSocketFactory} based on the
	 *         {@link CertificateValidator} implementation.
	 */
	SSLSocketFactory createSSLSocketFactory();

	/**
	 * Creates {@code HostnameVerifier} based on the
	 * {@link CertificateValidator} implmenetation (i.e. disabled or not).
	 * <p>
	 * Created {@code HostnameVerifier} is the nused to either verify hostnames
	 * or not.
	 * 
	 * @return created {@code HostnameVerifier} based on the
	 *         {@link CertificateValidator} implementation.
	 */
	HostnameVerifier createHostnameVerifier();

	/**
	 * Checks whether or not client's certificate is locally available or it was
	 * downloaded from the server.
	 * 
	 * @return whether or not client's certificate is available locally.
	 */
	boolean certificateLocallyAvailable();
}
