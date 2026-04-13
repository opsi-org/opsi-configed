/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
public class HealthCheckSettingsModel {
	List<String> selectedHosts;
	FlatTriStateCheckBox.State checkActiveState;
	String startDowntime;
	String endDowntime;
	boolean saveEnabled;
}
