package de.uib.configed.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utilities.logging.Logging;

public class ConfigName2ConfigValue extends RetrievedMap {
	private Map<String, ConfigOption> configOptions;

	public ConfigName2ConfigValue(Map<String, Object> retrieved, Map<String, ConfigOption> configOptions) {
		super(retrieved);
		this.configOptions = configOptions;

		buildX();
	}

	public ConfigName2ConfigValue(Map<String, Object> oldValues) {
		super(oldValues);
		build();
	}

	protected void buildX() {
		// overwrite values by virtue of imported type informations

		if (retrieved == null) {
			// we should take default values even if we have not got any values
			retrieved = new HashMap<>();
		}

		if (configOptions != null) {
			for (Entry<String, ConfigOption> option : configOptions.entrySet()) {
				// fill up by default values
				if (retrieved.get(option.getKey()) == null) {

					retrieved.put(option.getKey(), option.getValue().get("defaultValues"));

				}
			}
		} else {
			Logging.warning(this, "configOptions is null, cannot execute buildX()");
		}

		for (Entry<String, Object> entry : retrieved.entrySet()) {
			List list = null;
			// the retrieved object always are lists, we could correct this by observing the
			// config options

			if (!(entry.getValue() instanceof List)) {
				Logging.warning(this, "list expected , for key " + entry.getKey() + " found " + entry.getValue());
				Logging.error(this, "list expected , for key " + entry.getKey());

				continue;
			} else {
				list = (List) entry.getValue();
			}

			classnames.put(entry.getKey(), "List");

			if (configOptions != null && configOptions.get(entry.getKey()) != null) {
				ConfigOption configOption = configOptions.get(entry.getKey());

				if (configOption.get("classname").equals("java.lang.Boolean")) {
					put(entry.getKey(), list.get(0));
				} else {
					put(entry.getKey(), list);
				}
			} else {
				Logging.debug(this, "no config (option) found for key " + entry.getKey());
				put(entry.getKey(), list);
			}
		}
	}
}
