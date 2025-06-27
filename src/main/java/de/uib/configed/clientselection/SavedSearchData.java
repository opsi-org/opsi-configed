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
public class SavedSearchData {
	private int version;
	private OperationNode data;

	public SavedSearchData() {
	}

	public SavedSearchData(int version, OperationNode node) {
		this.version = version;
		this.data = node;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public OperationNode getData() {
		return data;
	}

	public void setData(OperationNode node) {
		this.data = node;
	}
}