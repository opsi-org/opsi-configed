/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVScanner {
	private CSVFormat format;
	private BufferedReader reader;

	private StringBuilder field = new StringBuilder();
	private List<List<CSVToken>> tokens = new ArrayList<>();
	private boolean inQuotes;

	public CSVScanner(Reader reader, CSVFormat format) {
		if (reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		} else {
			this.reader = new BufferedReader(reader);
		}
		this.format = format;
	}

	public void scan() throws IOException {
		boolean skip = false;
		String currentLine = null;
		boolean fieldBeginsWithEmbeddedQuote = false;

		while ((currentLine = reader.readLine()) != null) {
			List<CSVToken> lineOfTokens = new ArrayList<>();
			CharReader cReader = new CharReader(currentLine.toCharArray(), currentLine.length());

			for (int i = 0; i < cReader.size(); i++) {
				char c = cReader.consume();

				if (skip) {
					skip = false;
					continue;
				}

				if (cReader.peek() == format.getFieldSeparator() && !field.toString().trim().isEmpty()) {
					if (c != format.getStringSeparator() && c != format.getFieldSeparator()) {
						field.append(c);
						c = '\0';
					}
					CSVToken fieldToken = new CSVToken(CSVToken.FIELD, field.toString());
					lineOfTokens.add(fieldToken);
					field.setLength(0);
				}

				if (c == format.getStringSeparator()) {
					if ((cReader.peek() == format.getFieldSeparator() || i == currentLine.length() - 1)
							&& fieldBeginsWithEmbeddedQuote) {
						fieldBeginsWithEmbeddedQuote = false;
						CSVToken emptyFieldToken = new CSVToken(CSVToken.EMPTY_FIELD,
								String.format("%c%c", format.getStringSeparator(), format.getStringSeparator()));
						lineOfTokens.add(emptyFieldToken);
						continue;
					}

					if (((cReader.lastRead() == format.getStringSeparator() && i == 0)
							|| cReader.lastRead() == format.getFieldSeparator())
							&& cReader.peek() == format.getStringSeparator()) {
						fieldBeginsWithEmbeddedQuote = true;
						continue;
					}

					if (cReader.peek() == format.getStringSeparator() && i != currentLine.length() - 1) {
						if (fieldBeginsWithEmbeddedQuote) {
							CSVToken stringSeparatorToken = new CSVToken(CSVToken.STRING_SEPARATOR,
									String.valueOf(format.getStringSeparator()));
							lineOfTokens.add(stringSeparatorToken);
							CSVToken embeddedQuoteToken = new CSVToken(CSVToken.EMBEDDED_QUOTE,
									String.valueOf(format.getStringSeparator()));
							lineOfTokens.add(embeddedQuoteToken);
							fieldBeginsWithEmbeddedQuote = false;

							inQuotes = !inQuotes;
						} else {
							CSVToken fieldToken = new CSVToken(CSVToken.FIELD, field.toString());
							lineOfTokens.add(fieldToken);
							field.setLength(0);
							CSVToken embeddedQuoteToken = new CSVToken(CSVToken.EMBEDDED_QUOTE,
									String.valueOf(format.getStringSeparator()));
							lineOfTokens.add(embeddedQuoteToken);
						}

						skip = true;
						continue;
					}

					inQuotes = !inQuotes;

					if (!field.toString().trim().isEmpty()) {
						CSVToken fieldToken = new CSVToken(CSVToken.FIELD, field.toString());
						lineOfTokens.add(fieldToken);
						field.setLength(0);
					}

					CSVToken stringSeparatorToken = new CSVToken(CSVToken.STRING_SEPARATOR,
							String.valueOf(format.getStringSeparator()));
					lineOfTokens.add(stringSeparatorToken);
					continue;
				}

				if (c == format.getFieldSeparator() && !inQuotes) {
					CSVToken fieldSeparatorToken = new CSVToken(CSVToken.FIELD_SEPARATOR,
							String.valueOf(format.getFieldSeparator()));
					lineOfTokens.add(fieldSeparatorToken);
				} else {
					field.append(c);
				}
			}

			if (!field.toString().trim().isEmpty()) {
				CSVToken fieldToken = new CSVToken(CSVToken.FIELD, field.toString());
				lineOfTokens.add(fieldToken);
				field.setLength(0);
			}

			if (inQuotes) {
				CSVToken newLineCSVToken = new CSVToken(CSVToken.NEW_LINE, "\n");
				lineOfTokens.add(newLineCSVToken);
			} else {
				CSVToken lineEndCSVToken = new CSVToken(CSVToken.LINE_END, "");
				lineOfTokens.add(lineEndCSVToken);
			}

			tokens.add(lineOfTokens);
		}
	}

	public List<List<CSVToken>> getTokens() {
		return tokens;
	}
}
