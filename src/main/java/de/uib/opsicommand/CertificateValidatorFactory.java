/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

public class CertificateValidatorFactory {
	public static CertificateValidator create(boolean secure) {
		return secure ? new SecureCertificateValidator() : new InsecureCertificateValidator();
	}
}
