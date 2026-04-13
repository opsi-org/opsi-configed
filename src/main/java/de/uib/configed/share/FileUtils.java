/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.share;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.share.logging.Logging;

/**
 * Utility methods for file system access and local persistence handling.
 * <p>
 * This includes helpers for managing saved state directories, file permissions,
 * path resolution, and file-related transformations.
 * </p>
 * <p>
 * No UI logic or user interaction should be added to this class.
 * </p>
 */
public final class FileUtils {

	private FileUtils() {
	}

	public static List<String> readLocallySavedServerNames() {
		File savedStatesLocation = createSavedStatesDir();

		if (savedStatesLocation == null) {
			return new ArrayList<>();
		}

		Logging.info("saved states location ", savedStatesLocation);

		TreeMap<Long, String> sortingMap = buidSavedStateTimestampMap(savedStatesLocation);
		List<String> result = sortingMap.descendingMap().values().stream().toList();

		Logging.info("readLocallySavedServerNames  result ", result);

		return result;
	}

	private static TreeMap<Long, String> buidSavedStateTimestampMap(File savedStatesLocation) {
		TreeMap<Long, String> sortingMap = new TreeMap<>();
		File[] subdirs = savedStatesLocation.listFiles(File::isDirectory);

		for (File folder : subdirs) {
			File checkFile = new File(folder + File.separator + Configed.SAVED_STATES_FILENAME);
			String folderPath = folder.getPath();
			String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

			if (elementname.lastIndexOf("_") > -1) {
				elementname = elementname.replace("_", ":");
			}

			sortingMap.put(checkFile.lastModified(), elementname);
		}

		return sortingMap;
	}

	private static File createSavedStatesDir() {
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.getSavedStatesLocationName() != null) {
			Logging.info("trying to find saved states in ", Configed.getSavedStatesLocationName());

			savedStatesLocation = new File(Configed.getSavedStatesLocationName());
			savedStatesLocation.mkdirs();
			success = savedStatesLocation.setReadable(true);
		}

		if (!success) {
			Logging.warning("cannot not find saved states in ", Configed.getSavedStatesLocationName());
		}

		if (Configed.getSavedStatesLocationName() == null || !success) {
			Logging.info("searching saved states in ", getSavedStatesDefaultLocation());
			savedStatesLocation = new File(getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}
		return savedStatesLocation;
	}

	public static String getSavedStatesDefaultLocation() {
		String result;

		if (System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) != null) {
			result = System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) + File.separator + "opsi.org"
					+ File.separator + "configed";
		} else {
			result = System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY) + File.separator + ".configed";
		}

		return result;
	}

	public static void restrictAccessToFile(File file) {
		try {
			Logging.info("Restricting file's ", file.getAbsolutePath(),
					" access to only allow owner to modify/read file");
			String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			Logging.info("Detected operating system ", osName);
			if (osName.contains("win")) {
				boolean readablePermissionChanged = file.setReadable(true, true);
				boolean writablePermissionChanged = file.setWritable(true, true);
				Logging.info("Readable permission for file ", file.getAbsolutePath(), " changed ",
						readablePermissionChanged);
				Logging.info("Writable permission for file ", file.getAbsolutePath(), " changed ",
						writablePermissionChanged);
			} else {
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
				Files.setPosixFilePermissions(file.toPath(), permissions);
				Logging.info("Changed file's ", file.getAbsolutePath(), " permission");
			}
		} catch (IOException e) {
			Logging.error(e, "Unable to set default permissions on temp file");
		}
	}

	public static String getServerPathFromWebDAVPath(String webDAVPath) {
		String dir = "";
		if (webDAVPath.startsWith("workbench")) {
			dir = PersistenceControllerFactory.getPersistenceController().getDataServices().config
					.getConfigedWorkbenchDefaultValuePD();
			if (dir.charAt(dir.length() - 1) != '/') {
				dir = dir + "/";
			}
			dir = dir + retrieveEverythingAfterDir(webDAVPath, "workbench/");
		} else if (webDAVPath.startsWith("repository")) {
			dir = CommandFactory.OPSI_PATH_VAR_REPOSITORY + retrieveEverythingAfterDir(webDAVPath, "repository/");
		} else if (webDAVPath.startsWith("depot")) {
			dir = CommandFactory.OPSI_PATH_VAR_DEPOT + retrieveEverythingAfterDir(webDAVPath, "depot/");
		} else {
			Logging.warning("expected repository or workbench");
		}
		return dir;
	}

	private static String retrieveEverythingAfterDir(String filePath, String dir) {
		return filePath.substring(filePath.indexOf(dir) + dir.length());
	}

}
