/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.Map;

public class ConfigName2ConfigValue extends RetrievedMap {
	private Map<String, ConfigOption> configOptions;

	public ConfigName2ConfigValue(Map<String, Object> retrieved, Map<String, ConfigOption> configOptions) {
		super(retrieved);
		this.configOptions = configOptions;

		buildX();
	}

	public ConfigName2ConfigValue(Map<String, Object> oldValues) {
		super(oldValues);
		super.build();
	}

	private void buildX() {
		// overwrite values by virtue of imported type informations

		if (retrieved == null) {
			// we should take default values even if we have not got any values
			retrieved = new HashMap<>();
		}

		if (configOptions != null) {

			// Fill up with default values
			for (Entry<String, ConfigOption> option : configOptions.entrySet()) {
				Object defaultValues = option.getValue().get("defaultValues");
				retrieved.putIfAbsent(option.getKey(), defaultValues);

				put(option.getKey(), defaultValues);
			}
		}
	}
}
