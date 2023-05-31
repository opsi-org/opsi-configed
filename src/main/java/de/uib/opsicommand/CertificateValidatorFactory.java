/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

public final class CertificateValidatorFactory {
	private CertificateValidatorFactory() {
	}

	public static CertificateValidator createSecure() {
		return new SecureCertificateValidator();
	}

	public static CertificateValidator createInsecure() {
		return new InsecureCertificateValidator();
	}
}
