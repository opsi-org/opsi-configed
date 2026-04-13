/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import java.util.Locale;
import java.util.Map;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;
import de.uib.configed.share.logging.Logging;

public class OpsiDataStringEqualsOperation extends StringEqualsOperation implements ExecutableOperation {
	private String map;
	private String key;
	private String data;
	private String[] dataSplitted;
	private boolean startsWith;
	private boolean endsWith;

	public OpsiDataStringEqualsOperation(String map, String key, String data, AbstractSelectElement element) {
		super(element);
		Logging.debug(this, "OpsiDataStringEqualsOperation maptype, key, data: ", map, ", ", key, ", ", data);
		this.map = map;
		this.key = key;
		this.data = data.toLowerCase(Locale.ROOT);
		if (data.contains("*")) {
			dataSplitted = this.data.split("\\*");
			Logging.debug(this, "OpsiDataStringEqualsOperation ", dataSplitted.length);
		}
		startsWith = data.startsWith("*");
		endsWith = data.endsWith("*");
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Logging.debug(this, " (OpsiDataStringEqualsOperation) doesMatch client ", client);

		Map<String, Object> realMap = client.getMap(map);
		Logging.debug(this, "doesMatch,  we look into map for key ", key);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			return false;
		}

		String realData = realMap.get(key).toString().toLowerCase(Locale.ROOT);
		Logging.debug(this, " (OpsiDataStringEqualsOperation) doesMatch realData ", realData);
		return checkData(realData);
	}

	protected boolean checkData(final String realData) {
		String rData = realData.toLowerCase(Locale.ROOT);

		// simple case: no '*'
		if (dataSplitted == null) {
			return rData.equals(data);
		} else if (dataSplitted.length == 0) {
			// the only chars are '*'
			return rData.length() > 0;
		} else {
			return checkDataSplitted(rData);
		}
	}

	private boolean checkDataSplitted(String rData) {
		if (!startsWith && !rData.startsWith(dataSplitted[0])) {
			return false;
		}

		int index = 0;
		for (int i = 0; i < dataSplitted.length && index >= 0; i++) {
			if (!dataSplitted[i].isEmpty()) {
				index = rData.indexOf(dataSplitted[i], index);
				if (index >= 0) {
					index += dataSplitted[i].length();
				}
			}
		}
		return index >= 0 && (endsWith || rData.endsWith(dataSplitted[dataSplitted.length - 1]));
	}
}
