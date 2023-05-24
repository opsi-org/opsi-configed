/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;

/**
*/
public class HostUpdateCollection extends UpdateCollection {
	private OpsiserviceNOMPersistenceController persis;

	public HostUpdateCollection(Object persis) {
		super(new ArrayList<>(0));
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (OpsiserviceNOMPersistenceController) obj;
	}

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> c) {
		boolean result = true;

		Iterator<? extends UpdateCommand> it = c.iterator();
		while (it.hasNext()) {
			Map map = null;
			Object obj = it.next();

			try {
				map = (Map<?, ?>) obj;
			} catch (ClassCastException ccex) {
				Logging.error("Wrong element type, found " + obj.getClass().getName() + ", expected a Map", ccex);
			}

			result = add(new HostUpdate(persis, map));
		}
		return result;
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}
}
