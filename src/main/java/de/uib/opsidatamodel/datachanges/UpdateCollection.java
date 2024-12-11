/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */
package de.uib.opsidatamodel.datachanges;

import java.util.Collection;
import java.util.Map;

public interface UpdateCollection extends UpdateCommand, Collection<UpdateCommand> {
	boolean addMap(Map<String, Object> map);

	void clearElements();

	void revert();

	void cancel();
}
