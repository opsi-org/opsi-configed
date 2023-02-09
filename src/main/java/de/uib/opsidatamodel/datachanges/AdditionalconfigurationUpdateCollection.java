package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

/**
*/
public class AdditionalconfigurationUpdateCollection extends UpdateCollection {
	String[] objectIds;
	AbstractPersistenceController persis;
	boolean determineConfigOptions = false;
	boolean masterConfig = false;

	public AdditionalconfigurationUpdateCollection(Object persis, String[] objectIds) {
		super(new ArrayList<>(0));
		this.objectIds = objectIds;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (AbstractPersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean result = true;

		if (c.size() != objectIds.length) {
			result = false;
			Logging.warning(this, "object ids (not fitting to edited item) " + Arrays.toString(objectIds));
			Logging.error("list of data has size " + c.size() + " differs from  length of objectIds list  "
					+ objectIds.length);
		}

		if (result) {
			Iterator it = c.iterator();
			int i = 0;
			while (it.hasNext()) {
				Map map = null;
				Object obj = it.next();

				try {
					map = (Map) obj;
				}

				catch (ClassCastException ccex) {
					Logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map");
				}

				Logging.debug(this, "addAll for one obj, map " + map);

				if (masterConfig) {
					Logging.debug(this, "adding ConfigUpdate");
					result = add(new ConfigUpdate(persis, map));
				} else {
					Logging.debug(this, "adding AdditionalconfigurationUpdate");
					result = add(new AdditionalconfigurationUpdate(persis, objectIds[i], map));
				}
				i++;
			}
		}

		return result;
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}

	@Override
	public void doCall() {
		super.doCall();
		Logging.debug(this, "doCall, after recursion, element count: " + size());
		if (masterConfig)
			persis.setConfig();
		else
			persis.setAdditionalConfiguration(determineConfigOptions);
		clear();
	}

	public void setDetermineConfigOptions(boolean b) {
		determineConfigOptions = b;
	}

	public void setMasterConfig(boolean b) {
		masterConfig = b;
	}

	public boolean isMasterConfig() {
		return masterConfig;
	}

}
