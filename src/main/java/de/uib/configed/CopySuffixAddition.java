/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

/**
 * This class is responsible for adding a suffix to a client's name.
 */
public class CopySuffixAddition {
	private static final String COPY_SUFFIX = "-copy";
	private static OpsiserviceNOMPersistenceController persist = PersistenceControllerFactory
			.getPersistenceController();

	private String clientName;

	/**
	 * Creates CopySuffixAddition object with provided information.
	 * 
	 * @param clientName client's name for which copy suffix to add
	 */
	public CopySuffixAddition(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * Adds a copy suffix to provided client's name. The following suffixes can
	 * be added by this method:
	 * 
	 * <pre>
	 * [client name]-copy
	 * [client name]-copy[N] where N is the number of copies.
	 * </pre>
	 * 
	 * @return returns client name with added suffix (doesn't change the
	 *         original)
	 */
	public String add() {
		StringBuilder clientNameBuilder = new StringBuilder();
		String clientToCopyName = getNameFromClientName();

		if (clientToCopyName.contains(COPY_SUFFIX)) {
			clientNameBuilder.append(replaceNumberSuffix(clientToCopyName));
		} else if (clientHasCopy(clientToCopyName)) {
			clientNameBuilder.append(clientToCopyName);
			clientNameBuilder.append(COPY_SUFFIX);
			clientNameBuilder.append(generateNumberSuffix(clientNameBuilder.toString()).toString());
		} else {
			clientNameBuilder.append(clientToCopyName);
			clientNameBuilder.append(COPY_SUFFIX);
		}

		return clientNameBuilder.toString();
	}

	private String replaceNumberSuffix(String clientName) {
		if (Character.isDigit(clientName.charAt(clientName.length() - 1))) {
			Integer currentNumberSuffix = Integer
					.valueOf(Character.getNumericValue(clientName.charAt(clientName.length() - 1)));
			clientName = clientName.replace(currentNumberSuffix.toString(),
					generateNumberSuffix(clientName).toString());
		} else {
			clientName = clientName.concat(generateNumberSuffix(clientName).toString());
		}

		return clientName;
	}

	private Integer generateNumberSuffix(String clientName) {
		Integer numberSuffix = 1;

		if (Character.isDigit(clientName.charAt(clientName.length() - 1))) {
			Integer currentNumberSuffix = Integer
					.valueOf(Character.getNumericValue(clientName.charAt(clientName.length() - 1)));
			numberSuffix = currentNumberSuffix;
		} else {
			clientName = clientName.concat(numberSuffix.toString());
		}

		while (clientExists(clientName.concat("." + getDomainFromClientName()))) {
			numberSuffix += 1;
			StringBuilder sb = new StringBuilder(clientName);
			String strRepresentation = numberSuffix.toString();
			sb.setCharAt(sb.length() - 1, strRepresentation.charAt(0));
			if (strRepresentation.length() > 1) {
				for (int i = 1; i < strRepresentation.length(); i++) {
					sb.append(strRepresentation.charAt(i));
				}
			}
			clientName = sb.toString();
		}

		return numberSuffix;
	}

	private boolean clientHasCopy(String clientName) {
		return clientExists(clientName.concat(COPY_SUFFIX + "." + getDomainFromClientName()));
	}

	private String getNameFromClientName() {
		String[] splittedClientName = clientName.split("\\.");
		return splittedClientName[0];
	}

	private String getDomainFromClientName() {
		String[] splittedClientName = clientName.split("\\.");
		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < splittedClientName.length; i++) {
			sb.append(splittedClientName[i]);

			if (i != splittedClientName.length - 1) {
				sb.append(".");
			}
		}

		return sb.toString();
	}

	private static boolean clientExists(String clientName) {
		List<String> opsiHostNames = persist.getHostInfoCollections().getOpsiHostNames();
		return opsiHostNames.stream().anyMatch(opsiHostName -> opsiHostName.equals(clientName));
	}
}
