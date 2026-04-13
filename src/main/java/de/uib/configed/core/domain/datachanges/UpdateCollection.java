/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */
package de.uib.configed.core.domain.datachanges;

import java.util.Collection;
import java.util.Map;

public interface UpdateCollection extends UpdateCommand, Collection<UpdateCommand> {
	boolean addMap(Map<String, Object> map);

	void clearElements();

	void revert();

	void cancel();
}
