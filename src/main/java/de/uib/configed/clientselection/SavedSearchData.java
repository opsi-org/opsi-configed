/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the data structure for a saved search, including version and the
 * operation data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SavedSearchData(int version, OperationNode data) {
}