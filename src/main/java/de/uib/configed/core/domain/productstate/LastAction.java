/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.productstate;

import java.util.List;

import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;

public enum LastAction {
	// conflicting entries from several clients
	CONFLICT(Globals.CONFLICT_STATE_STRING),

	// no valid entry from service
	INVALID(Globals.NO_VALID_STATE_STRING),

	// product offers no actions
	NOT_AVAILABLE("not_available"),

	// valid service states
	NONE("none"), SETUP("setup"), UPDATE("update"), UNINSTALL("uninstall"), ALWAYS("always"), ONCE("once"),
	CUSTOM("custom");

	public static final String KEY = "lastAction";

	private final String label;

	LastAction(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

	/**
	 * true for a real action (setup, update, uninstall, always, once, custom)
	 */
	public boolean isRealAction() {
		return compareTo(NONE) > 0;
	}

	public static LastAction fromLabel(String label) {
		return Utils.fromLabel(values(), LastAction::getLabel, label, NOT_AVAILABLE, INVALID);
	}

	public static List<String> getLabels() {
		return List.of(NOT_AVAILABLE.label, NONE.label, SETUP.label, UPDATE.label, UNINSTALL.label, ALWAYS.label,
				ONCE.label, CUSTOM.label);
	}
}
