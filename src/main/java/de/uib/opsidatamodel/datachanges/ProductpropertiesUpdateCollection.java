package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

/**
*/
public class ProductpropertiesUpdateCollection extends UpdateCollection {
	java.util.List<String> clients;
	String productname;
	PersistenceController persis;
	ConfigedMain mainController;

	public ProductpropertiesUpdateCollection(ConfigedMain mainController, Object persis, String[] clients,
			String productname) {
		this(mainController, persis, Arrays.asList(clients), productname);
	}

	public ProductpropertiesUpdateCollection(ConfigedMain mainController, Object persis, java.util.List<String> clients,
			String productname) {
		super(new Vector<Object>(0));
		if (clients == null) {
			this.clients = new ArrayList<>();
		} else {
			this.clients = clients;
		}
		this.productname = productname;
		setController(persis);
		this.mainController = mainController;
	}

	@Override
	public void setController(Object obj) {
		this.persis = (PersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean result = true;

		if (c.size() > 0) {
			Iterator it = c.iterator();
			Object ob = it.next();
			logging.info(this, "addAll on collection of size " + c.size() + " of type " + ob.getClass()
					+ " should produce values for all " + clients.size() + " hosts");
		}

		if (result && (c.size() != clients.size())) {
			result = false;

			logging.error(
					"list of data has size " + c.size() + " differs from  length of clients list  " + clients.size());

		}

		if (result) {
			Iterator it = c.iterator();
			int i = 0;
			while (it.hasNext()) {
				Map map = null;
				Object obj = it.next();

				logging.debug(this, "addAll, element of Collection: " + obj);

				try {
					map = (Map) obj;
				}

				catch (ClassCastException ccex) {
					result = false;
					logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map");
				}

				result = add(new ProductpropertiesUpdate(persis, clients.get(i), productname, map));
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
		logging.debug(this, "doCall, after recursion");
		persis.setProductproperties();

		// mainController.requestReloadStatesAndActions();
		// mainController.resetView(mainController.getViewIndex());
	}

	@Override
	public void revert() {
		for (Object ob : implementor) {
			if (ob instanceof ProductpropertiesUpdate) {
				((ProductpropertiesUpdate) ob).revert();
			} else {
				logging.info(this, "revert: not a ProductpropertiesUpdate : " + ob);
			}
		}
	}

	@Override
	public boolean add(Object obj) {
		// logging.debug ("----------- adding to ProductPropertiesCollection " +
		// obj + " of class " + obj.getClass().getName());
		return super.add(obj);
	}

}
