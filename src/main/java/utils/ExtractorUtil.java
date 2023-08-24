/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.StreamingNotSupportedException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import de.uib.utilities.logging.Logging;

public final class ExtractorUtil {
	private ExtractorUtil() {
	}

	public static Map<String, String> unzip(File file) {
		Logging.info("ExtractorUtil: starting extract");
		Map<String, String> files = new HashMap<>();
		String archiveFormat = detectArchiveFormat(file);
		try (ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(archiveFormat,
				retrieveInputStream(file))) {
			ArchiveEntry entry = null;
			while ((entry = ais.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					byte[] content = new byte[(int) entry.getSize()];
					int bytesRead = ais.read(content);
					files.put(entry.getName(), new String(content, 0, bytesRead));
				}
			}
		} catch (StreamingNotSupportedException e) {
			if (e.getFormat().equals(ArchiveStreamFactory.SEVEN_Z)) {
				files = extractSevenZIP(file);
			} else {
				Logging.error("Archive format " + archiveFormat + " does not support streaming", e);
			}
		} catch (ArchiveException e) {
			Logging.error("Archive format is unknown " + archiveFormat, e);
		} catch (IOException e) {
			Logging.error("Unable to read zip file " + file.getAbsolutePath(), e);
		}

		return files;
	}

	private static String detectArchiveFormat(File file) {
		String archiveFormat = "";
		try {
			archiveFormat = ArchiveStreamFactory.detect(retrieveInputStream(file));
		} catch (ArchiveException e) {
			if (file.getName().contains(".tar")) {
				archiveFormat = ArchiveStreamFactory.TAR;
			} else {
				Logging.error("Unable to detect archive format for file " + file.getAbsolutePath(), e);
			}
		}
		return archiveFormat;
	}

	private static InputStream retrieveInputStream(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			if (file.getName().contains(".gz")) {
				is = new GzipCompressorInputStream(is);
			}
			is = new BufferedInputStream(is);
		} catch (FileNotFoundException e) {
			Logging.error("File not found " + file.getAbsolutePath(), e);
			return is;
		} catch (IOException e) {
			Logging.error("Unable to retrieve input stream ", e);
		}
		return is;
	}

	private static Map<String, String> extractSevenZIP(File file) {
		Map<String, String> files = new HashMap<>();
		try (SevenZFile sevenZFile = new SevenZFile(file)) {
			SevenZArchiveEntry entry = null;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					byte[] content = new byte[(int) entry.getSize()];
					int bytesRead = sevenZFile.read(content);
					files.put(entry.getName(), new String(content, 0, bytesRead));
				}
			}
		} catch (IOException e) {
			Logging.error("Unable to read 7z file " + file.getAbsolutePath(), e);
		}
		return files;
	}
}
