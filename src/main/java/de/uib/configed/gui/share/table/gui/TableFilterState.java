/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.io.Serializable;

public class TableFilterState implements Serializable {
	private String searchText;
	private int searchColumnIndex;
	private boolean regexActive;
	private boolean respectCase;

	public TableFilterState(String searchText, int searchColumnIndex, boolean regexActive, boolean respectCase) {
		this.searchText = searchText;
		this.searchColumnIndex = searchColumnIndex;
		this.regexActive = regexActive;
		this.respectCase = respectCase;
	}

	public String getSearchText() {
		return searchText;
	}

	public int getSearchColumnIndex() {
		return searchColumnIndex;
	}

	public boolean isRegexActive() {
		return regexActive;
	}

	public boolean isRespectCase() {
		return respectCase;
	}
}
