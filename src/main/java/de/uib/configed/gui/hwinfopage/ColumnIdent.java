/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.utilities.logging.Logging;

public class ColumnIdent {
	private String dbColumnName;
	private String hwClass;
	private String tableType;
	private String configIdent;

	public ColumnIdent(String tableValue) {
		if (tableValue == null) {
			return;
		}

		int indexCurly = tableValue.indexOf('{');

		if (indexCurly == -1) {
			dbColumnName = tableValue.trim();
			return;
		}

		dbColumnName = tableValue.substring(0, indexCurly).trim();
		String tableIdent = tableValue.substring(indexCurly + 1);

		String checkType = OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE + "}";

		if (tableIdent.endsWith(checkType)) {
			tableType = OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;
		} else {
			checkType = OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE + "}";
			if (tableIdent.endsWith(checkType)) {
				tableType = OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE;
			}
		}

		int indexUnderline = tableIdent.lastIndexOf("_");
		hwClass = tableIdent.substring(0, indexUnderline);

		configIdent = hwClass + "_" + tableType;

		Logging.debug(this, "from '" + tableValue + "' we get " + " col name " + dbColumnName + " type " + tableType
				+ " hw class " + hwClass);

	}

	public ColumnIdent(String hwClass, String tableType, String colName) {
		this.dbColumnName = colName;
		this.hwClass = hwClass;
		this.tableType = tableType;
	}

	public String getConfigIdent() {
		return configIdent;
	}

	public String getDBColumnName() {
		return dbColumnName;
	}

	public String produceColumnCellValue() {
		String result = dbColumnName + " {" + hwClass + "_" + tableType + "}";
		Logging.debug(this, "produceColumnCellValue " + result);

		return result;
	}

	@Override
	public String toString() {
		return "dbColumnName " + dbColumnName + " " + "hwClass " + hwClass + " " + "tableType " + tableType;
	}
}
