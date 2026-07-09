/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.productstate;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;

public enum InstallationStatus {
	// conflicting entries from several clients
	CONFLICT(Globals.CONFLICT_STATE_STRING, null),

	// no valid entry from service
	INVALID(Globals.NO_VALID_STATE_STRING, null),

	// does not matter
	UNDEFINED(InstallationStatus.KEY_UNDEFINED, null),

	// valid service states since 4.0
	INSTALLED(InstallationStatus.KEY_INSTALLED, Globals.INSTALLATION_STATUS_INSTALLED_COLOR),
	NOT_INSTALLED(InstallationStatus.KEY_NOT_INSTALLED, Globals.INSTALLATION_STATUS_NOT_INSTALLED_COLOR),
	UNKNOWN(InstallationStatus.KEY_UNKNOWN, Globals.INSTALLATION_STATUS_UNKNOWN_COLOR);

	public static final String KEY = "installationStatus";

	public static final String KEY_NOT_INSTALLED = "not_installed";
	public static final String KEY_INSTALLED = "installed";
	public static final String KEY_UNKNOWN = "unknown";
	public static final String KEY_UNDEFINED = "undefined";

	private final String label;
	private final Color textColor;

	InstallationStatus(String label, Color textColor) {
		this.label = label;
		this.textColor = textColor;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

	public static InstallationStatus fromLabel(String label) {
		// empty label may occur for pure action requests
		return Utils.fromLabel(values(), InstallationStatus::getLabel, label, UNKNOWN, INVALID);
	}

	public static String produceFromLabel(String label) {
		return label != null && !label.isEmpty() && fromLabel(label) != INVALID ? label : INVALID.getLabel();
	}

	public static Map<String, Color> getLabel2TextColor() {
		Map<String, Color> label2textColor = new HashMap<>();
		for (InstallationStatus status : values()) {
			if (status.textColor != null) {
				label2textColor.put(status.label, status.textColor);
			}
		}

		return label2textColor;
	}

	public static List<String> getLabels() {
		return List.of(KEY_UNDEFINED, KEY_INSTALLED, KEY_NOT_INSTALLED, KEY_UNKNOWN);
	}

	public static String[] getDisplayLabelsForChoice() {
		return new String[] { KEY_NOT_INSTALLED, KEY_INSTALLED, KEY_UNKNOWN };
	}
}
