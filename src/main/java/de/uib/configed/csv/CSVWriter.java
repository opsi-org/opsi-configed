package de.uib.configed.csv;

import java.io.*;
import java.util.*;

import de.uib.utilities.table.updates.*;

public class CSVWriter
{
	private static final CSVFormat DEFAULT_FORMAT = new CSVFormat();

	private CSVFormat format;
	private BufferedWriter writer;

	public CSVWriter(Writer writer)
	{
        this(writer, DEFAULT_FORMAT);
	}

	public CSVWriter(Writer writer, CSVFormat format)
	{
		this.writer = (writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer));
		this.format = format;
	}

	public void insertFormatHint() throws IOException
	{
		writer.append(String.format("//- sep=%c -- quote=%c", format.getFieldSeparator(), format.getStringSeparator()));
		writer.newLine();
	}

	public <T> void write(Vector<T> line) throws IOException
	{
		char fieldSeparator = format.getFieldSeparator();
		char stringSeparator = format.getStringSeparator();
		Iterator<T> iter = line.iterator();

		while (iter.hasNext())
		{
			String field = (String) iter.next();

			if (!iter.hasNext())
			{
				writer.append(String.format("%c%s%c", stringSeparator, field, stringSeparator));
			}
			else
			{
				writer.append(String.format("%c%s%c%c", stringSeparator, field, stringSeparator, fieldSeparator));
			}
		}

		writer.newLine();
	}

	public void close() throws IOException
	{
		writer.close();
	}
}
