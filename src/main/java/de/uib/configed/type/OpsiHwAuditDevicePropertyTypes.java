/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uib.utils.logging.Logging;

public class OpsiHwAuditDevicePropertyTypes extends HashMap<String, OpsiHwAuditDeviceClass> {
	private Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses;

	public OpsiHwAuditDevicePropertyTypes(Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses) {
		super();
		this.hwAuditDeviceClasses = hwAuditDeviceClasses;
	}

	public void checkIn(String key, List<Object> valuesSet) {
		if (!key.startsWith(OpsiHwAuditDeviceClass.CONFIG_KEY) || valuesSet == null) {
			return;
		}

		Logging.info(this, "checkIn key " + key + " valuesSet " + valuesSet);

		String hwdevicePartOfKey = key.substring(OpsiHwAuditDeviceClass.CONFIG_KEY.length() + 1);

		String tableType;

		if (hwdevicePartOfKey.endsWith(OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE_LOWER)) {
			tableType = OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;
		} else if (hwdevicePartOfKey.endsWith(OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE_LOWER)) {
			tableType = OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE;
		} else {
			Logging.warning(this, "unexpected tableType ");
			return;
		}

		int i = hwdevicePartOfKey.lastIndexOf("_");

		String hwClass;
		if (i > 0) {
			hwClass = hwdevicePartOfKey.substring(0, i);
		} else {
			return;
		}

		Logging.info(this, "checkIn key " + key + " hwClass " + hwClass + " tableType " + tableType);

		OpsiHwAuditDeviceClass auditDeviceClass = hwAuditDeviceClasses.get(hwClass.toUpperCase(Locale.ROOT));

		Logging.info(this, "checkIn key " + key + " auditDeviceClass " + auditDeviceClass);
		Logging.info(this, "checkIn auditDeviceClasses for keys " + hwAuditDeviceClasses.keySet());
		put(key, auditDeviceClass);

		if (tableType.equals(OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE)) {
			for (OpsiHwAuditDevicePropertyType deviceProperty : auditDeviceClass.getDeviceHostProperties()) {
				deviceProperty.setDisplayed(valuesSet.contains(deviceProperty.getOpsiDbColumnName()));
			}
		} else {
			for (OpsiHwAuditDevicePropertyType deviceProperty : auditDeviceClass.getDeviceHwItemProperties()) {
				deviceProperty.setDisplayed(valuesSet.contains(deviceProperty.getOpsiDbColumnName()));
			}
		}
	}
}
