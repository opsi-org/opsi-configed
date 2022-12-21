package de.uib.configed.type;

import java.util.HashMap;

import de.uib.utilities.logging.logging;

public class OpsiHwAuditDevicePropertyTypes extends HashMap<String, OpsiHwAuditDeviceClass> {
	java.util.Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses;

	public OpsiHwAuditDevicePropertyTypes(java.util.Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses) {
		super();
		this.hwAuditDeviceClasses = hwAuditDeviceClasses;
	}

	public void checkIn(String key, java.util.List valuesSet) {
		if (!key.startsWith(OpsiHwAuditDeviceClass.CONFIG_KEY))
			return;

		logging.info(this, "checkIn key " + key + " valuesSet " + valuesSet);

		String hwdevicePartOfKey = key.substring(OpsiHwAuditDeviceClass.CONFIG_KEY.length() + 1);

		String tableType = null;

		String hwClass = null;

		if (hwdevicePartOfKey.endsWith(OpsiHwAuditDeviceClass.hostAssignedTableTypeLower)) {
			tableType = OpsiHwAuditDeviceClass.hostAssignedTableType;
		} else if (hwdevicePartOfKey.endsWith(OpsiHwAuditDeviceClass.hwItemAssignedTableTypeLower)) {
			tableType = OpsiHwAuditDeviceClass.hwItemAssignedTableType;
		}

		int i = hwdevicePartOfKey.lastIndexOf("_");

		if (i > 0)
			hwClass = hwdevicePartOfKey.substring(0, i);

		if (tableType == null || hwClass == null)
			return;

		logging.info(this, "checkIn key " + key + " hwClass " + hwClass + " tableType " + tableType);

		OpsiHwAuditDeviceClass auditDeviceClass = hwAuditDeviceClasses.get(hwClass.toUpperCase());

		logging.info(this, "checkIn key " + key + " auditDeviceClass " + auditDeviceClass);
		logging.info(this, "checkIn auditDeviceClasses for keys " + hwAuditDeviceClasses.keySet());
		put(key, auditDeviceClass);

		if (tableType == OpsiHwAuditDeviceClass.hostAssignedTableType) {
			/*
			 * Vector<String> usedHostColumns = new Vector<>();
			 * for (Object value : valuesSet )
			 * {
			 * usedHostColumns.add( (String) value );
			 * }
			 */

			// auditDeviceClass.setUsedHostColumns( usedHostColumns );

			for (OpsiHwAuditDevicePropertyType deviceProperty : auditDeviceClass.getDeviceHostProperties()) {
				if (valuesSet.contains(deviceProperty.getOpsiDbColumnName()))
					deviceProperty.setDisplayed(true);
				else
					deviceProperty.setDisplayed(false);
			}

		} else {
			/*
			 * Vector<String> usedHwItemColumns = new Vector<>();
			 * for (Object value : valuesSet )
			 * {
			 * usedHwItemColumns.add( (String) value );
			 * }
			 * 
			 * auditDeviceClass.setUsedHwItemColumns( usedHwItemColumns );
			 */

			for (OpsiHwAuditDevicePropertyType deviceProperty : auditDeviceClass.getDeviceHwItemProperties()) {
				// logging.info(this, "checkIn valuesSet contains? " +
				// deviceProperty.getOpsiDbColumnName());

				if (valuesSet.contains(deviceProperty.getOpsiDbColumnName()))
					deviceProperty.setDisplayed(true);
				else
					deviceProperty.setDisplayed(false);
			}
		}
		// System.exit(0);
	}

}