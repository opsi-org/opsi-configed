/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public interface CertificateValidator {
	SSLSocketFactory createSSLSocketFactory();

	HostnameVerifier createHostnameVerifier();

	boolean certificateLocallyAvailable();
}
