package de.uib.opsidatamodel.datachanges;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

/**
*/
public class AdditionalconfigurationUpdateCollection extends UpdateCollection {
	String[] objectIds;
	PersistenceController persis;
	boolean determineConfigOptions = false;
	boolean masterConfig = false;

	public AdditionalconfigurationUpdateCollection(Object persis, String[] objectIds) {
		super(new Vector(0));
		this.objectIds = objectIds;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (PersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean result = true;

		if (result && (c.size() != objectIds.length)) {
			result = false;
			logging.warning(this, "object ids (not fitting to edited item) " + Arrays.toString(objectIds));
			logging.error("list of data has size " + c.size() + " differs from  length of objectIds list  "
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
					result = false;
					logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map");
				}

				logging.debug(this, "addAll for one obj, map " + map);

				if (masterConfig) {
					logging.debug(this, "adding ConfigUpdate");
					result = add(new ConfigUpdate(persis, map));
				} else {
					logging.debug(this, "adding AdditionalconfigurationUpdate");
					result = add(new AdditionalconfigurationUpdate(persis, objectIds[i], map));
				}
				i++;
			}
		}

		return result;
	}

	@Override
	public void clearElements() {
		logging.debug(this, "clearElements()");
		clear();
	}

	@Override
	public void doCall() {
		super.doCall();
		logging.debug(this, "doCall, after recursion, element count: " + size());
		if (masterConfig)
			persis.setConfig();
		else
			persis.setAdditionalConfiguration(determineConfigOptions);
		clear();
	}

	@Override
	public boolean add(Object obj) {
		// logging.debug ("----------- adding " + obj + " of class " +
		// obj.getClass().getName());
		return super.add(obj);
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
