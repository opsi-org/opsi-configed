/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.infrastructure.webdav.WebDAVClient;
import de.uib.configed.share.logging.Logging;

public final class WinProductUtils {
	private WinProductUtils() {
	}

	/**
	 * Retrieves the set of Windows products from the given depot directory. A
	 * product is considered a Windows product if it contains either a 'winpe'
	 * or 'i368' subdirectory.
	 *
	 * @param webDAVClient          The WebDAV client to use for directory
	 *                              operations.
	 * @param depotProductDirectory The depot directory to search in (should end
	 *                              with '/').
	 * @return List of product names.
	 */
	public static List<String> getWinProductsPD(WebDAVClient webDAVClient, String depotProductDirectory) {
		@SuppressWarnings("unchecked")
		List<String> cachedWinProducts = CacheManager.getInstance().getCachedData(CacheIdentifier.WIN_PRODUCTS,
				List.class);
		if (cachedWinProducts != null) {
			return cachedWinProducts;
		}

		if (depotProductDirectory == null) {
			return List.of();
		}
		Set<String> allNetbootProducts = webDAVClient.getDirectoriesIn(depotProductDirectory, false);
		List<String> winProducts = allNetbootProducts.parallelStream().filter((String product) -> {
			boolean hasWinpe = webDAVClient.exists(depotProductDirectory + product + "winpe");
			boolean hasI368 = webDAVClient.exists(depotProductDirectory + product + "i368");
			return hasWinpe || hasI368;
		}).sorted().toList();
		CacheManager.getInstance().setCachedData(CacheIdentifier.WIN_PRODUCTS, winProducts);
		return winProducts;
	}

	/**
	 * Uploads a file or directory to the target path on the server using
	 * WebDAV.
	 *
	 * @param webDAVClient The WebDAV client to use for upload.
	 * @param source       The local file or directory to upload.
	 * @param targetPath   The target path on the server (should use '/' as
	 *                     separator).
	 * @throws IOException If an error occurs during upload.
	 */
	public static void uploadFileOrDirectory(WebDAVClient webDAVClient, File source, String targetPath)
			throws IOException {
		if (source.isDirectory()) {
			webDAVClient.uploadDirectory(source, targetPath.endsWith("/") ? targetPath : (targetPath + "/"));
		} else {
			byte[] fileBytes = Files.readAllBytes(source.toPath());
			webDAVClient.uploadFile((targetPath.endsWith("/") ? targetPath : (targetPath + "/")) + source.getName(),
					new ByteArrayInputStream(fileBytes));
		}
	}

	/**
	 * Ensures the target directory exists on the server, optionally prompting
	 * the user to create it.
	 *
	 * @param parentComponent The parent component for dialogs.
	 * @param webDAVClient    The WebDAV client.
	 * @param targetPath      The target directory path.
	 * @return true if the directory exists or was created, false otherwise.
	 */
	public static boolean ensureServerDirectoryExists(java.awt.Component parentComponent, WebDAVClient webDAVClient,
			File targetPath, String promptText, String promptTitle) {
		boolean result = true;

		if (targetPath != null && !webDAVClient.existsAndIsDirectory(targetPath.getPath().replace("\\", "/"))) {
			int returnedOption = JOptionPane.showConfirmDialog(parentComponent, promptText, promptTitle,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (returnedOption == JOptionPane.YES_OPTION) {
				try {
					webDAVClient.createDirectories(targetPath.getPath().replace("\\", "/"));
				} catch (IOException e) {
					Logging.warning(WinProductUtils.class, "Failed to create directory ", e);
					result = false;
				}
			} else {
				result = false;
			}
		}

		return result;
	}
}
