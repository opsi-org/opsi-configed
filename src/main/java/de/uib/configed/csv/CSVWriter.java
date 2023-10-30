/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public class CSVWriter {
	private CSVFormat format;
	private BufferedWriter writer;

	public CSVWriter(Writer writer, CSVFormat format) {
		if (writer instanceof BufferedWriter) {
			this.writer = (BufferedWriter) writer;
		} else {
			this.writer = new BufferedWriter(writer);
		}

		this.format = format;
	}

	public void insertFormatHint() throws IOException {
		writer.append(String.format("//- sep=%c -- quote=%c", format.getFieldSeparator(), format.getStringSeparator()));
		writer.newLine();
	}

	public <T> void write(Iterable<T> line) throws IOException {
		char fieldSeparator = format.getFieldSeparator();
		char stringSeparator = format.getStringSeparator();
		Iterator<T> iter = line.iterator();

		while (iter.hasNext()) {
			String field = (String) iter.next();

			if (!iter.hasNext()) {
				writer.append(String.format("%c%s%c", stringSeparator, field, stringSeparator));
			} else {
				writer.append(String.format("%c%s%c%c", stringSeparator, field, stringSeparator, fieldSeparator));
			}
		}

		writer.newLine();
	}

	public void close() throws IOException {
		writer.close();
	}
}
