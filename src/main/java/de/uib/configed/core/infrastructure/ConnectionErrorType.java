/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure;

public enum ConnectionErrorType {
	FAILED_CERTIFICATE_VALIDATION_ERROR, FAILED_CERTIFICATE_DOWNLOAD_ERROR, INVALID_HOSTNAME_ERROR, MFA_ERROR,
	TIMEOUT_ERROR, GENERAL_ERROR
}
