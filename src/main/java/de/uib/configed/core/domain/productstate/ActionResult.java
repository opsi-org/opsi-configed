/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.productstate;

import java.util.List;

import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;

public enum ActionResult {
	// conflicting entries from several clients
	CONFLICT(Globals.CONFLICT_STATE_STRING),

	// no valid entry from service
	INVALID(Globals.NO_VALID_STATE_STRING),

	// product offers no entry
	NOT_AVAILABLE("not_available"),

	// valid service states
	NONE("none"), FAILED("failed"), SUCCESSFUL("successful");

	public static final String KEY = "actionResult";

	private final String label;

	ActionResult(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

	public static ActionResult fromLabel(String label) {
		return Utils.fromLabel(values(), ActionResult::getLabel, label, NOT_AVAILABLE, INVALID);
	}

	public static List<String> getLabels() {
		return List.of(NOT_AVAILABLE.label, NONE.label, FAILED.label, SUCCESSFUL.label);
	}
}
