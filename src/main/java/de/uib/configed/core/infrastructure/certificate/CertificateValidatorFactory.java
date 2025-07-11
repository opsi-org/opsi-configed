/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure.certificate;

import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

/**
 * {@code CertificateValidatorFactory} creates secure/insecure certificate
 * validator based on the method executed.
 * <p>
 * It is based on Factory Method design pattern.
 */
public final class CertificateValidatorFactory {
	private static CertificateValidator insecureCertificateValidator;
	private static CertificateValidator secureCertificateValidator;

	private CertificateValidatorFactory() {
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
	public static CertificateValidator getInsecure() {
		if (insecureCertificateValidator == null) {
			insecureCertificateValidator = new InsecureCertificateValidator();
		}
		return insecureCertificateValidator;
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
	public static CertificateValidator getValidator() {
		Logging.info("certificate verification is disabled: ", Utils.isCertificateVerificationDisabled());
		if (Utils.isCertificateVerificationDisabled()) {
			Logging.info("using insecure certificate validator");
			return getInsecure();
		}
		Logging.info("using secure certificate validator");
		if (secureCertificateValidator == null) {
			secureCertificateValidator = new SecureCertificateValidator();
		}

		return secureCertificateValidator;
	}

	public static void resetCertificateValidators() {
		insecureCertificateValidator = null;
		secureCertificateValidator = null;
	}
}
