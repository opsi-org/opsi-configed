/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

/**
 * {@code CertificateValidatorFactory} creates secure/insecure certificate
 * validator based on the method executed.
 * <p>
 * It is based on Factory Method design pattern.
 */
public final class CertificateValidatorFactory {
	private CertificateValidatorFactory() {
	}

	/**
	 * Creates secure certificate validator.
	 * 
	 * @return secure certificate validator.
	 */
	public static CertificateValidator createSecure() {
		return new SecureCertificateValidator();
	}

	/**
	 * Creates insecure certificate validator.
	 * 
	 * @return insecure certificate validator.
	 */
	public static CertificateValidator createInsecure() {
		return new InsecureCertificateValidator();
	}
}
