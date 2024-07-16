/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class ExporterToPDF extends AbstractExportTable {
	private static final String FILE_EXTENSION = ".pdf";

	private static final float M_LEFT = 36;
	private static final float M_RIGHT = 36;
	private static final float M_TOP = 74;
	private static final float M_BOTTOM = 54;

	private static final Font CAT_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	private static final Font SMALL_BOLD = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	private static final Font SMALL = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

	private final Document document = new Document(PageSize.A4, M_LEFT, M_RIGHT, M_TOP, M_BOTTOM);

	private String defaultFilename = "report.pdf";

	private boolean isLandscape;

	public ExporterToPDF(JTable table) {
		super(table);
		extension = FILE_EXTENSION;
		writeToFile = defaultFilename;
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {
		setPageSizeA4Landscape();

		int result = 0;
		defaultFilename = "report_" + client + extension;

		if (fileName == null) {
			result = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(), null,
					Configed.getResourceValue("OpenSaveDialog.title"), JOptionPane.DEFAULT_OPTION, -1, null,
					new Object[] { Configed.getResourceValue("OpenSaveDialog.save"),
							Configed.getResourceValue("OpenSaveDialog.open") },
					null);

			Logging.info(this, "fileName was null, result now has value " + result);
		}

		if (result == JOptionPane.DEFAULT_OPTION) {
			return;
		}

		String filePath = null;

		if (result == 0) {
			// Save file
			if (fileName == null) {
				fileName = getFileLocation();
			}

			// FileName is null if nothing chosen, then we do nothing
			if (fileName != null) {
				Logging.info(this, "filename for saving PDF: " + fileName);
				File file = new File(fileName);
				if (file.isDirectory()) {
					Logging.error("no valid filename ", fileName);
				} else {
					filePath = file.getAbsolutePath();
				}

				Logging.notice(this, "selected fileName is: ", fileName);
				fileName = checkExtension(fileName);
				Logging.notice(this, "after checkExtension(..), fileName is now: ", fileName);
				fileName = checkFile(fileName, extensionFilter);

				writeFile(filePath, fileName);
			}
		} else {
			// Open file
			try {
				File temp = Files.createTempFile(defaultFilename.substring(0, defaultFilename.indexOf(".")), ".pdf")
						.toFile();
				Utils.restrictAccessToFile(temp);
				writeFile(temp.getAbsolutePath(), fileName);
				openFile(temp);
			} catch (IOException e) {
				Logging.error(e, "Failed to create temp file");
			}
		}
	}

	private void writeFile(String filePath, String fileName) {
		// Write file now
		try {
			PdfWriter writer;
			if (filePath == null) {
				writer = PdfWriter.getInstance(document, new FileOutputStream(defaultFilename));
			} else {
				writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
			}

			TableHeader event = new TableHeader(isLandscape);
			if (metaData.containsKey("header")) {
				event.setHeader(metaData.get("header"));
			} else if (metaData.containsKey("title")) {
				event.setHeader(metaData.get("title"));
			} else {
				Logging.warning(this, "metadata contain neither header nor title");
			}

			writer.setPageEvent(event);

			document.open();
			addMetaData(metaData);
			document.add(addTitleLines(metaData));

			// table data
			document.add(createTableDataElement(theTable));

			document.close();
		} catch (FileNotFoundException ex) {
			Logging.error(ex, "file not found: ", fileName);
		} catch (DocumentException dex) {
			Logging.error(dex, "document exception, cannot get instance for ", document);
		}
	}

	private void addMetaData(Map<String, String> metaData) {
		if (metaData == null) {
			document.addTitle("Document as PDF");
			document.addSubject("Using iText");
			document.addKeywords("Java, PDF, iText");
		} else {
			if (metaData.containsKey("title")) {
				document.addTitle(metaData.get("title"));
			}
			if (metaData.containsKey("subject")) {
				document.addSubject(metaData.get("subject"));
			}
			if (metaData.containsKey("keywords")) {
				document.addKeywords(metaData.get("keywords"));
			}
		}
		document.addAuthor(System.getProperty("user.name"));
		document.addCreator(Globals.APPNAME);
	}

	private static void openFile(File temp) {
		// saveAction is not null here, open PDF if created only temp file
		if (temp != null && temp.getAbsolutePath() != null) {
			try {
				Desktop.getDesktop().open(temp);
			} catch (IOException e) {
				Logging.error(e, "cannot show: ", temp.getAbsolutePath());
			}
		}
	}

	public void setPageSizeA4() {
		document.setPageSize(PageSize.A4);
		isLandscape = false;
	}

	public void setPageSizeA4Landscape() {
		document.setPageSize(PageSize.A4.rotate());
		isLandscape = true;
	}

	private static Paragraph addEmptyLines(int number) {
		Paragraph content = new Paragraph();
		for (int i = 0; i < number; i++) {
			content.add(new Paragraph(" "));
		}
		return content;
	}

	private static Paragraph addTitleLines(Map<String, String> metaData) {
		// Second parameter is the number of the chapter
		Paragraph content = new Paragraph();

		if (metaData.containsKey("title")) {
			content.add(new Paragraph(metaData.get("title"), CAT_FONT));
		}

		if (metaData.containsKey("subtitle")) {
			content.add(new Paragraph(metaData.get("subtitle"), SMALL_BOLD));
		}

		String userInitial = "";
		if (System.getProperty("user.name") != null) {
			userInitial = System.getProperty("user.name");
			int cutpos = 3;
			int nameLength = userInitial.length();

			if (nameLength >= cutpos) {
				userInitial = userInitial.substring(0, cutpos);
				if (System.getProperty("user.name").length() > cutpos) {
					userInitial = userInitial + "..";
				}
			}
		}

		content.add(new Paragraph(Configed.getResourceValue("DocumentExport.summonedBy") + ": " + userInitial + ", "
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd. MMM yyyy"))));
		content.add(addEmptyLines(1));
		return content;
	}

	private static PdfPTable createTableDataElement(JTable theTable) {
		PdfPTable table = new PdfPTable(theTable.getColumnCount());
		PdfPCell h;
		PdfPCell value = null;

		table.setWidthPercentage(98);

		Font symbolFont;
		try {
			BaseFont bf = BaseFont.createFont(BaseFont.SYMBOL, BaseFont.SYMBOL, BaseFont.EMBEDDED);
			symbolFont = new Font(bf, 11);
		} catch (DocumentException | IOException e) {
			Logging.warning(e, "ExporterToPDF::createTableDataElement", " BaseFont can't be created :");
			symbolFont = SMALL;
		}
		PdfPCell defaultCell = table.getDefaultCell();

		defaultCell.setBackgroundColor(new BaseColor(Globals.OPSI_BACKGROUND_LIGHT.getRed(),
				Globals.OPSI_BACKGROUND_LIGHT.getGreen(), Globals.OPSI_BACKGROUND_LIGHT.getBlue()));

		for (int i = 0; i < theTable.getColumnCount(); i++) {
			h = new PdfPCell(new Phrase(theTable.getColumnName(i)));
			h.setHorizontalAlignment(Element.ALIGN_CENTER);
			h.setBackgroundColor(new BaseColor(Globals.OPSI_GREY.getRed(), Globals.OPSI_GREY.getGreen(),
					Globals.OPSI_GREY.getBlue()));

			table.addCell(h);
		}

		table.setHeaderRows(1);

		for (int j = 0; j < theTable.getRowCount(); j++) {
			for (int i = 0; i < theTable.getColumnCount(); i++) {
				value = new PdfPCell(new Phrase(" "));
				String s = theTable.getValueAt(j, i) != null ? theTable.getValueAt(j, i).toString() : "";

				switch (s) {
				case "true":
					value = new PdfPCell(new Phrase("\u221a", symbolFont)); // radic
					break;
				case "false":
					break;
				default:
					value = new PdfPCell(new Phrase(s, SMALL));
					break;
				}

				if (j % 2 == 0) {
					value.setBackgroundColor(new BaseColor(Globals.OPSI_LIGHT_GREY.getRed(),
							Globals.OPSI_LIGHT_GREY.getGreen(), Globals.OPSI_LIGHT_GREY.getBlue()));
				}

				value.setHorizontalAlignment(Element.ALIGN_CENTER);

				value.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(value);
			}
		}

		return table;
	}
}
