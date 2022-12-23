package de.uib.configed.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utilities.logging.logging;

public class ConfigName2ConfigValue extends RetrievedMap {
	Map<String, ConfigOption> configOptions;

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
			// return;
			// we should take default values even if we have not got any values
			retrieved = new HashMap<>();
		}

		if (configOptions == null) {
			
		} else {

			for (String key : configOptions.keySet()) {
				// fill up by default values
				if (retrieved.get(key) == null) {
					
					retrieved.put(key, configOptions.get(key).get("defaultValues"));

				}
			}
		}

		for (String key : retrieved.keySet()) {
			List list = null;
			// the retrieved object always are lists, we could correct this by observing the
			// config options

			if (!(retrieved.get(key) instanceof List)) {
				logging.warning(this, "list expected , for key " + key + " found " + retrieved.get(key));
				logging.error(this, "list expected , for key " + key);
				// logging.debug(this, "key " + key + ", retrieved.get(key) " +
				
				// logging.debug(this, "retrieved.get(key) has class " +
				

				continue;
			} else
				list = (List) retrieved.get(key);

			classnames.put(key, "List");

			if (configOptions != null && configOptions.get(key) != null) {
				ConfigOption configOption = (ConfigOption) configOptions.get(key);
				// logging.debug(this, "key " + key + " configOption class : " +
				
				if (configOption.get("classname").equals("java.lang.Boolean")) {
					put(key, list.get(0));
				} else
					put(key, list);
			} else {
				
				logging.debug(this, "no config (option) found for key " + key);

				put(key, list);
			}

		}

		
	}

}
