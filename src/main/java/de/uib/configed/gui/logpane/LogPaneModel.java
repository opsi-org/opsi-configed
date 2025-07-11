/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
public class LogPaneModel {
	@Builder.Default
	String logText = "";
	@Builder.Default
	boolean withPopup = true;
	@Builder.Default
	int showLevel = LogPaneComponent.MIN_LEVEL;
	@Builder.Default
	int maxExistingLevel = LogPaneComponent.MAX_LEVEL;
	@Builder.Default
	List<String> typesList = List.of();
	String selectedType;
	int caretPosition;
	@Builder.Default
	String info = "";
	@Builder.Default
	String title = "";
	boolean caseSensitive;
	@Builder.Default
	List<String> searchHistory = new ArrayList<>();
}
