package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;

/**
*/
public class ProductpropertiesUpdateCollection extends UpdateCollection {
	private List<String> clients;
	private String productname;
	private OpsiserviceNOMPersistenceController persis;

	public ProductpropertiesUpdateCollection(Object persis, String[] clients, String productname) {
		this(persis, Arrays.asList(clients), productname);
	}

	public ProductpropertiesUpdateCollection(Object persis, List<String> clients, String productname) {
		super(new ArrayList<>(0));
		if (clients == null) {
			this.clients = new ArrayList<>();
		} else {
			this.clients = clients;
		}
		this.productname = productname;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (OpsiserviceNOMPersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> collection) {
		boolean result = true;

		if (!collection.isEmpty()) {
			Iterator<? extends UpdateCommand> it = collection.iterator();
			Object obj = it.next();
			Logging.info(this, "addAll on collection of size " + collection.size() + " of type " + obj.getClass()
					+ " should produce values for all " + clients.size() + " hosts");
		}

		if (collection.size() != clients.size()) {
			result = false;

			Logging.error("list of data has size " + collection.size() + " differs from  length of clients list  "
					+ clients.size());

		}

		if (result) {
			Iterator<? extends UpdateCommand> it = collection.iterator();
			int i = 0;
			while (it.hasNext()) {
				Map map = null;
				Object obj = it.next();

				Logging.debug(this, "addAll, element of Collection: " + obj);

				try {
					map = (Map<?, ?>) obj;
				} catch (ClassCastException ccex) {
					Logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map", ccex);
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
		for (UpdateCommand updateCommand : implementor) {
			if (updateCommand instanceof ProductpropertiesUpdate) {
				((ProductpropertiesUpdate) updateCommand).revert();
			} else {
				Logging.info(this, "revert: not a ProductpropertiesUpdate : " + updateCommand);
			}
		}
	}
}
