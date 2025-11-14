/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.Map;

import lombok.Value;
import lombok.With;

@Value
@With
public class HealthCheckModel {
	private final Map<String, Map<String, Object>> healthData;
}
