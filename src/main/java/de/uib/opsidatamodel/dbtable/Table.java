/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.dbtable;

import java.io.File;
import java.sql.Timestamp;

public class Table {
	private String highTimestampS = new Timestamp(0).toString();

	private String localTablePath;

	public Table(String localTablePath) {
		this.localTablePath = localTablePath;
		new File(localTablePath).mkdirs();
	}

	public String getLocalFilePath() {
		return localTablePath;
	}

	public String getHighTimestamp() {

		return highTimestampS;
	}

	public void resetHighTimeStamp() {
		highTimestampS = new Timestamp(0).toString();
	}

	public void compareToHighTimestamp(String s) {
		if (s.compareTo(highTimestampS) > 0) {
			highTimestampS = s;
		}
	}
}
