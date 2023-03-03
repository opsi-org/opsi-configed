package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

/**
*/
public class ProductpropertiesUpdateCollection extends UpdateCollection {
	List<String> clients;
	String productname;
	AbstractPersistenceController persis;
	ConfigedMain mainController;

	public ProductpropertiesUpdateCollection(ConfigedMain mainController, Object persis, String[] clients,
			String productname) {
		this(mainController, persis, Arrays.asList(clients), productname);
	}

	public ProductpropertiesUpdateCollection(ConfigedMain mainController, Object persis, List<String> clients,
			String productname) {
		super(new ArrayList<>(0));
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
		this.persis = (AbstractPersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean result = true;

		if (!c.isEmpty()) {
			Iterator it = c.iterator();
			Object ob = it.next();
			Logging.info(this, "addAll on collection of size " + c.size() + " of type " + ob.getClass()
					+ " should produce values for all " + clients.size() + " hosts");
		}

		if (c.size() != clients.size()) {
			result = false;

			Logging.error(
					"list of data has size " + c.size() + " differs from  length of clients list  " + clients.size());

		}

		if (result) {
			Iterator it = c.iterator();
			int i = 0;
			while (it.hasNext()) {
				Map map = null;
				Object obj = it.next();

				Logging.debug(this, "addAll, element of Collection: " + obj);

				try {
					map = (Map) obj;
				} catch (ClassCastException ccex) {
					Logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map");
				}

				result = add(new ProductpropertiesUpdate(persis, clients.get(i), productname, map));
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
		Logging.debug(this, "doCall, after recursion");
		persis.setProductProperties();

	}

	@Override
	public void revert() {
		for (Object ob : implementor) {
			if (ob instanceof ProductpropertiesUpdate) {
				((ProductpropertiesUpdate) ob).revert();
			} else {
				Logging.info(this, "revert: not a ProductpropertiesUpdate : " + ob);
			}
		}
	}
}
