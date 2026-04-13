/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.share.logging.Logging;

/**
*/
public class ProductpropertiesUpdateCollection extends DefaultUpdateCollection {
	private List<String> clients;
	private String productname;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductpropertiesUpdateCollection(List<String> clients, String productname) {
		super();
		if (clients == null) {
			this.clients = new ArrayList<>();
		} else {
			this.clients = clients;
		}
		this.productname = productname;
	}

	@Override
	public boolean addMap(Map<String, Object> map) {
		boolean result = true;
		for (String client : clients) {
			result = add(new ProductpropertiesUpdate(client, productname, map));
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
		persistenceController.getDataServices().product.setProductProperties();
	}

	@Override
	public void revert() {
		for (UpdateCommand updateCommand : implementor) {
			if (updateCommand instanceof ProductpropertiesUpdate productpropertiesUpdate) {
				productpropertiesUpdate.revert();
			} else {
				Logging.info(this, "revert: not a ProductpropertiesUpdate : ", updateCommand);
			}
		}
	}
}
