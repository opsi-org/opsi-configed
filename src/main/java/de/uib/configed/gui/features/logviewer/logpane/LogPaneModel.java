/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.features.logviewer.logpane.view.LogTextPane;
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
	int minLevel = LogPaneComponent.MIN_LEVEL;
	@Builder.Default
	int maxLevel = LogPaneComponent.MAX_LEVEL;
	int maxExistingLevel;
	@Builder.Default
	List<String> typesList = List.of();
	@Builder.Default
	String selectedType = LogTextPane.DEFAULT_TYPE;
	int caretPosition;
	@Builder.Default
	String info = "";
	@Builder.Default
	String title = "";
	boolean caseSensitive;
	@Builder.Default
	List<String> searchHistory = new ArrayList<>();
	@Builder.Default
	int fontSize = 11;
	boolean needsRebuild;
}
