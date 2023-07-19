/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import de.uib.utilities.logging.Logging;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public final class ExtractorUtil {

	// Private constructor to hide public one, 
	// Must not be instanciated
	private ExtractorUtil() {
	}

	public static StringBuilder unzip(File file) {

		Logging.info("ExtractorUtil: starting extract");
		final StringBuilder sb = new StringBuilder();

		try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
				// Autodetect archiveFormat
				IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile))) {

			Logging.info("\n   Archiv Format" + inArchive.getArchiveFormat());
			// Getting simple interface of the archive inArchive
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			Logging.info("   Hash   |    Size    | Filename");
			Logging.info("----------+------------+---------");

			for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				final int[] hash = new int[] { 0 };
				if (!item.isFolder()) {

					final long[] sizeArray = new long[1];
					ExtractOperationResult result = item.extractSlow(new ISequentialOutStream() {
						@Override
						public int write(byte[] data) throws SevenZipException {
							sb.append(new String(data, StandardCharsets.UTF_8));
							// Consume data
							hash[0] ^= Arrays.hashCode(data);
							sizeArray[0] += data.length;

							// Return amount of consumed data
							return data.length;
						}
					});

					if (result == ExtractOperationResult.OK) {
						Logging.info(String.format("%9X | %10s | %s", hash[0], sizeArray[0], item.getPath()));
					} else {
						Logging.error("Error extracting item: " + result);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			Logging.error("Could not find file....", ex);
		} catch (IOException ex) {
			if (ex instanceof SevenZipException) {
				return null;
			}

			Logging.warning("Could not close file, exception thrown...", ex);
		}

		return sb;
	}
}
