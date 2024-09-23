/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Collection;
import java.util.Iterator;

import de.uib.opsicommand.POJOReMapper;
import de.uib.utils.logging.Logging;

/**
*/
public class HostUpdateCollection extends UpdateCollection {
	public HostUpdateCollection() {
		super();
	}

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> c) {
		boolean result = true;

		Iterator<? extends UpdateCommand> it = c.iterator();
		while (it.hasNext()) {
			result = add(new HostUpdate(POJOReMapper.remap(it.next())));
		}

		return result;
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}
}
