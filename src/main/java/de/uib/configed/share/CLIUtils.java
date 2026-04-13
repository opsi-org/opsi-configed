/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.share;

import java.io.Console;
import java.util.Scanner;

/**
 * Utility methods for command-line interaction.
 * <p>
 * Provides helpers for reading user input from the console, including
 * password-safe input.
 * </p>
 * <p>
 * If {@link System#console()} is unavailable, methods may return empty values.
 * </p>
 */
public final class CLIUtils {
	private CLIUtils() {
	}

	public static String getCLIPasswordParam(String question) {
		return getCLIParam(question, true);
	}

	public static String getCLIParam(String question) {
		return getCLIParam(question, false);
	}

	@SuppressWarnings({ "java:S106" })
	private static String getCLIParam(String question, boolean password) {
		Console con = System.console();
		if (con == null) {
			return "";
		}

		System.out.print(question);
		if (password) {
			return String.valueOf(con.readPassword()).trim();
		}

		try (Scanner sc = new Scanner(con.reader())) {
			return sc.nextLine();
		}
	}
}
