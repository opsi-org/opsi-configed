package utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

public class ExtractorUtil {

	public static StringBuilder unzip(File file) throws Exception {

		RandomAccessFile randomAccessFile = null;

		Logging.info("ExtractorUtil: starting extract");
		IInArchive inArchive = null;
		final StringBuilder sb = new StringBuilder();

		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));
			Logging.info("\n   Archiv Format" + inArchive.getArchiveFormat());
			// Getting simple interface of the archive inArchive
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			Logging.info("   Hash   |    Size    | Filename");
			Logging.info("----------+------------+---------");

			for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				final int[] hash = new int[] { 0 };
				if (!item.isFolder()) {
					ExtractOperationResult result;

					final long[] sizeArray = new long[1];
					result = item.extractSlow(new ISequentialOutStream() {
						@Override
						public int write(byte[] data) throws SevenZipException {
							sb.append(new String(data));
							hash[0] ^= Arrays.hashCode(data); // Consume data
							sizeArray[0] += data.length;
							return data.length; // Return amount of consumed data
						}
					});
					if (result == ExtractOperationResult.OK) {
						Logging.info(String.format("%9X | %10s | %s", hash[0], sizeArray[0], item.getPath()));
					} else {
						Logging.error("Error extracting item: " + result);
					}
				}
			}
		} catch (Exception e) {
			throw (e);
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					Logging.error("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					Logging.error("Error closing file: " + e);
				}
			}
		}
		return sb;
	}

}
