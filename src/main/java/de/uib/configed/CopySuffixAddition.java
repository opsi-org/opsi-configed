/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

/**
 * This class is responsible for adding a suffix to a client's name.
 */
public class CopySuffixAddition {
	private static final String COPY_SUFFIX = "-copy";
	private static final Pattern numberSuffixPattern = Pattern.compile("\\d+$");
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
			if (containsNumberSuffix(clientToCopyName)) {
				clientNameBuilder.append(replaceNumberSuffix(clientToCopyName, generateNumberSuffix(clientToCopyName)));
			} else {
				clientNameBuilder.append(clientToCopyName.concat(generateNumberSuffix(clientToCopyName).toString()));
			}
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

	private Integer generateNumberSuffix(String clientName) {
		Integer numberSuffix = retrieveNumberSuffix(clientName);

		if (!containsNumberSuffix(clientName)) {
			clientName = clientName.concat(numberSuffix.toString());
		}

		while (clientExists(clientName.concat("." + Globals.getDomainFromClientName(this.clientName)))) {
			numberSuffix += 1;
			clientName = replaceNumberSuffix(clientName, numberSuffix);
		}

		return numberSuffix;
	}

	private static String replaceNumberSuffix(String clientName, Integer numberSuffix) {
		Matcher matcher = numberSuffixPattern.matcher(clientName);
		if (matcher.find()) {
			StringBuilder sb = new StringBuilder(clientName);
			sb.replace(matcher.start(), matcher.end(), numberSuffix.toString());
			clientName = sb.toString();
		}
		return clientName;
	}

	private static boolean containsNumberSuffix(String clientName) {
		Matcher matcher = numberSuffixPattern.matcher(clientName);
		return matcher.find();
	}

	private static Integer retrieveNumberSuffix(String clientName) {
		Matcher matcher = numberSuffixPattern.matcher(clientName);
		return matcher.find() ? Integer.valueOf(matcher.group()) : 1;
	}

	private boolean clientHasCopy(String clientName) {
		return clientExists(clientName.concat(COPY_SUFFIX + "." + Globals.getDomainFromClientName(this.clientName)));
	}

	private String getNameFromClientName() {
		String[] splittedClientName = clientName.split("\\.");
		return splittedClientName[0];
	}

	private static boolean clientExists(String clientName) {
		List<String> opsiHostNames = persist.getHostInfoCollections().getOpsiHostNames();
		return opsiHostNames.stream().anyMatch(opsiHostName -> opsiHostName.equals(clientName));
	}
}
