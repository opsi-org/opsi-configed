/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

/**
 * {@code CertificateValidatorFactory} creates secure/insecure certificate
 * validator based on the method executed.
 * <p>
 * It is based on Factory Method design pattern.
 */
public final class CertificateValidatorFactory {
	private CertificateValidatorFactory() {
	}

	public static CertificateValidator getValidator(boolean useInsecure) {
		// used for first request/s (in login form) to read connection header to allow or disable SSO
		if (useInsecure) {
			Logging.info("using insecure certificate validator (temporary)");
			return createInsecure();
		}
		Logging.debug("using default validator");
		return createValidator();
	}

	/**
	 * Creates insecure certificate validator.
	 * <p>
	 * This method is intended for cases where certificate verification should
	 * be deliberately bypassed.
	 * </p>
	 * 
	 * @return An instance of {@link InsecureCertificateValidator}.
	 */
	public static CertificateValidator createInsecure() {
		return new InsecureCertificateValidator();
	}

	/**
	 * Creates certificate validator based on the state of the
	 * {@code --disable-certificate-verification} flag.
	 * 
	 * @return An instance of {@link CertificateValidator}. If the
	 *         {@code --disable-certificate-verification} flag is enabled, an
	 *         {@link InsecureCertificateValidator} is returned; otherwise, a
	 *         {@link SecureCertificateValidator} is returned.
	 */
	public static CertificateValidator createValidator() {
		Logging.info("certificate verification is disabled: " + Utils.isCertificateVerificationDisabled());
		if (Utils.isCertificateVerificationDisabled()) {
			Logging.info("using insecure certificate validator");
			return new InsecureCertificateValidator();
		}
		Logging.info("using secure certificate validator");
		return new SecureCertificateValidator();
	}
}
