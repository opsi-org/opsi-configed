/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package utils;

import java.io.File;
import java.lang.management.ManagementFactory;

import de.uib.utilities.logging.Logging;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public final class SevenZipLibraryInitializer {
	private SevenZipLibraryInitializer() {

	}

	public static synchronized void init() {
		String processId = getProcessIdentifier();
		File uniqueTempDir = createUniqueTempDir(processId);

		try {
			SevenZip.initSevenZipFromPlatformJAR(uniqueTempDir);
		} catch (SevenZipNativeInitializationException e) {
			Logging.error("unable to initialize 7-ZIP JBinding", e);
		}
	}

	private static String getProcessIdentifier() {
		String processName = ManagementFactory.getRuntimeMXBean().getName();
		return processName.split("@")[0];
	}

	private static File createUniqueTempDir(String prefix) {
		String tempDir = System.getProperty("java.io.tmpdir");
		String uniqueDirPath = tempDir + prefix + System.nanoTime();
		File uniqueDir = new File(uniqueDirPath);
		uniqueDir.mkdirs();
		return uniqueDir;
	}
}
