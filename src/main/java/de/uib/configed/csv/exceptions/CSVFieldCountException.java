/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv.exceptions;

public class CSVFieldCountException extends CSVException {
	public CSVFieldCountException(String errorMessage) {
		super(errorMessage);
	}
}
