/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

interface UpdateCommand {
	void doCall();

	Object getController();

	void setController(Object cont);
}
