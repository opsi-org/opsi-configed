/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.share;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javax.swing.JFrame;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.logging.Logging;

/**
 * Miscellaneous utility methods that do not clearly belong to a specific
 * domain.
 * <p>
 * Only truly generic, low-level helpers should live here. If a method gains a
 * clear responsibility, it should be moved to a more specific utility class.
 * </p>
 */
@SuppressWarnings({ "java:S1448" })
public final class Utils {
	private static final int KIBI_BYTE = 1024;

	private static JFrame masterFrame;

	private Utils() {
	}

	public static void threadSleep(Object caller, long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Logging.info(caller, "sleeping interrupted: ", ie);
			Thread.currentThread().interrupt();
		}
	}

	public static String usedMemory() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		return " " + (((total - free) / KIBI_BYTE) / KIBI_BYTE) + " MB ";
	}

	public static void setMasterFrame(JFrame frame) {
		masterFrame = frame;
	}

	public static JFrame getMasterFrame() {
		return masterFrame;
	}

	/**
	 * Resolves an enum constant by its label.
	 *
	 * @param values       all enum constants, i.e. {@code MyEnum.values()}
	 * @param labelGetter  accessor for the label of a constant
	 * @param label        the label to look up
	 * @param emptyDefault result for a null or empty label
	 * @param invalid      result for an unknown label
	 */
	public static <T extends Enum<T>> T fromLabel(T[] values, Function<T, String> labelGetter, String label,
			T emptyDefault, T invalid) {
		if (label == null || label.isEmpty()) {
			return emptyDefault;
		}

		for (T value : values) {
			if (labelGetter.apply(value).equals(label)) {
				return value;
			}
		}

		return invalid;
	}

	public static String getListStringRepresentation(List<String> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}

		final int MAX = 5;

		StringBuilder result = new StringBuilder();
		int stop = list.size();
		if (stop > MAX) {
			stop = MAX;
		}

		for (int i = 0; i < stop - 1; i++) {
			result.append(list.get(i));
			result.append(";\n");
		}

		result.append(list.get(stop - 1));

		if (list.size() > MAX) {
			result.append(" ... ");
		}

		return result.toString();
	}

	public static boolean hasPort(String host) {
		boolean result = false;
		if (host == null || host.isEmpty()) {
			return result;
		}
		if (host.contains("[") && host.contains("]")) {
			Logging.info("Host is IPv6: ", host);
			result = host.indexOf(":", host.indexOf("]")) != -1;
		} else {
			Logging.info("Host is either IPv4 or FQDN: ", host);
			result = host.contains(":");
		}

		return result;
	}

	public static String getDomainFromClientName(String clientName) {
		StringBuilder sb = new StringBuilder();
		String[] splittedClientName = clientName.split("\\.");
		for (int i = 1; i < splittedClientName.length; i++) {
			sb.append(splittedClientName[i]);
			if (i != splittedClientName.length - 1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	public static Boolean toBoolean(Object obj) {
		return switch (obj) {
		case Boolean bool -> bool;
		case String str -> Boolean.valueOf(str);
		default -> false;
		};
	}

	public static boolean compareNumeric(Object realData, long data, BiPredicate<Long, Long> comparison) {
		return switch (realData) {
		case Long longData -> comparison.test(longData, data);
		case Integer integerData -> comparison.test(integerData.longValue(), data);
		case Object o -> {
			Logging.error("data is no BigInteger!", o);
			yield false;
		}
		};
	}

	public static FlatSVGIcon determineIconBasedOnPlatform(String platform, int size) {
		return switch (platform) {
		case "macos" -> Icons.getThemeSVGRepoIcon("macos", size);
		case "windows" -> Icons.getThemeSVGRepoIcon("windows", size);
		case "linux" -> Icons.getThemeSVGRepoIcon("linux", size);
		case null, default -> Icons.getThemeIntellijIcon("questionMark", size);
		};
	}

	public static FlatSVGIcon determineIconBasedOnDeviceType(String device, int size) {
		return switch (device) {
		case "server" -> Icons.getThemeSVGRepoIcon("server", size);
		case "notebook" -> Icons.getThemeSVGRepoIcon("laptop", size);
		case "desktop" -> Icons.getThemeSVGRepoIcon("desktop", size);
		case "virtual_machine" -> Icons.getThemeSVGRepoIcon("virtualMachine", size);
		case "convertible" -> Icons.getThemeSVGRepoIcon("convertible", size);
		case "other" -> Icons.getThemeIntellijIcon("questionMark", size);
		case null, default -> Icons.getThemeIntellijIcon("questionMark", size);
		};
	}
}
