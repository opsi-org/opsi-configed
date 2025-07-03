/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.Map;

/**
 * Msg represents all possible events/messages that can affect the Model.
 */
public sealed interface HealthCheckMsg permits HealthCheckMsg.ExpandAll, HealthCheckMsg.CollapseAll,
		HealthCheckMsg.ToggleDetails, HealthCheckMsg.CopyHealthInformation, HealthCheckMsg.DownloadDiagnosticData,
		HealthCheckMsg.HealthDataRefreshed {
	// Marker interface for all messages

	/**
	 * User clicked to expand all details.
	 */
	final class ExpandAll implements HealthCheckMsg {}

	/**
	 * User clicked to collapse all details.
	 */
	final class CollapseAll implements HealthCheckMsg {}

	/**
	 * User clicked to toggle details for a specific key.
	 */
	record ToggleDetails(String key) implements HealthCheckMsg {
	}

	/**
	 * User requested to copy health information.
	 */
	final class CopyHealthInformation implements HealthCheckMsg {}

	/**
	 * User requested to download diagnostic data.
	 */
	final class DownloadDiagnosticData implements HealthCheckMsg {}

	/**
	 * Health data was refreshed (e.g., after an update).
	 */
	record HealthDataRefreshed(Map<String, Map<String, Object>> newHealthData) implements HealthCheckMsg {
	}
}