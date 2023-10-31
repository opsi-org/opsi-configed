/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.pdf.OpenSaveDialog;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class ExporterToPDF extends AbstractExportTable {
	private static Document document;

	private static final String FILE_EXTENSION = ".pdf";

	private static final float M_LEFT = 36;
	private static final float M_RIGHT = 36;
	private static final float M_TOP = 74;
	private static final float M_BOTTOM = 54;

	private static Font catFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	private static Font small = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

	private OpenSaveDialog dialog;
	private Boolean saveAction;

	private String defaultFilename = "report.pdf";

	private float xHeaderTop = 803;
	private float headerWidth = 527;

	public ExporterToPDF(JTable table, List<String> classNames) {
		super(table, classNames);
		extension = FILE_EXTENSION;
		writeToFile = defaultFilename;
		document = new Document(PageSize.A4, M_LEFT, M_RIGHT, M_TOP, M_BOTTOM);
	}

	public ExporterToPDF(PanelGenEditTable table) {
		this(table.getTheTable(), null);
	}

	public ExporterToPDF(JTable table) {
		this(table, null);
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {
		setPageSizeA4Landscape();

		if (fileName != null) {
			saveAction = true;
		}

		String filePath = null;
		File temp = null;

		defaultFilename = "report_" + client + extension;

		if (saveAction == null) {
			if (dialog == null) {
				dialog = new OpenSaveDialog(Configed.getResourceValue("OpenSaveDialog.title"));
			} else {
				dialog.setVisible();
			}

			saveAction = dialog.getSaveAction();
			Logging.info(this, "saveAction was null, now has value " + saveAction);
		}

		if (saveAction != null) {
			if (Boolean.TRUE.equals(saveAction)) {
				if (fileName == null) {
					fileName = getFileLocation();
				}

				// FileName is null if nothing chosen, then we do nothing
				if (fileName == null) {
					return;
				} else {
					Logging.info(this, "filename for saving PDF: " + fileName);
					File file = new File(fileName);
					if (file.isDirectory()) {
						Logging.error("no valid filename " + fileName);
					} else {
						filePath = file.getAbsolutePath();
					}

					Logging.notice(this, "selected fileName is: " + fileName);
					fileName = checkExtension(fileName);
					Logging.notice(this, "after checkExtension(..), fileName is now: " + fileName);
					fileName = checkFile(fileName, extensionFilter);
				}
			} else {
				try {
					temp = Files.createTempFile(defaultFilename.substring(0, defaultFilename.indexOf(".")), ".pdf")
							.toFile();
					Utils.restrictAccessToFile(temp);
					filePath = temp.getAbsolutePath();
				} catch (IOException e) {
					Logging.error("Failed to create temp file", e);
				}
			}

			// Write file now
			try {
				PdfWriter writer;
				if (filePath == null) {
					writer = PdfWriter.getInstance(document, new FileOutputStream(defaultFilename));
				} else {
					writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
				}

				TableHeader event = new TableHeader();
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
				Logging.error("file not found: " + fileName, ex);
			} catch (DocumentException dex) {
				Logging.error("document exception, cannot get instance for " + document, dex);
			}

			// saveAction is not null here, open PDF if created only temp file
			if (Boolean.FALSE.equals(saveAction) && temp != null && temp.getAbsolutePath() != null) {
				try {
					Desktop.getDesktop().open(temp);
				} catch (IOException e) {
					Logging.error("cannot show: " + temp.getAbsolutePath(), e);
				}
			}
		}
	}

	private static void addMetaData(Map<String, String> metaData) {
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

	public void setPageSizeA4() {
		document.setPageSize(PageSize.A4);
		headerWidth = 527;
		xHeaderTop = 803;
	}

	public void setPageSizeA4Landscape() {
		document.setPageSize(PageSize.A4.rotate());
		headerWidth = 770;
		xHeaderTop = 555;
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
			content.add(new Paragraph(metaData.get("title"), catFont));
		}

		if (metaData.containsKey("subtitle")) {
			content.add(new Paragraph(metaData.get("subtitle"), smallBold));
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
		boolean onlySelectedRows = theTable.getSelectedRowCount() > 0;

		PdfPTable table = new PdfPTable(theTable.getColumnCount());
		PdfPCell h;
		PdfPCell value = null;

		table.setWidthPercentage(98);

		Font symbolFont;
		try {
			BaseFont bf = BaseFont.createFont(BaseFont.SYMBOL, BaseFont.SYMBOL, BaseFont.EMBEDDED);
			symbolFont = new Font(bf, 11);
		} catch (DocumentException | IOException e) {
			Logging.warning("ExporterToPDF::createTableDataElement", " BaseFont can't be created :", e);
			symbolFont = small;
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
			if (!onlySelectedRows || theTable.isRowSelected(j)) {
				for (int i = 0; i < theTable.getColumnCount(); i++) {
					value = new PdfPCell(new Phrase(" "));
					String s = theTable.getValueAt(j, i) != null ? theTable.getValueAt(j, i).toString() : "";

					switch (s) {
					case "∞":
						value = new PdfPCell(new Phrase("\u221e", symbolFont));
						break;
					case "true":
						value = new PdfPCell(new Phrase("\u221a", symbolFont)); // radic
						break;
					case "false":
						break;
					default:
						value = new PdfPCell(new Phrase(s, small));
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
		}

		return table;
	}

	/**
	 * Inner class to add a table as header.
	 */
	private class TableHeader extends PdfPageEventHelper {
		/** The header text. */
		String header = "";
		/** The template with the total number of pages. */
		PdfTemplate total;

		/**
		 * Allows us to change the content of the header.
		 * 
		 * @param header The new header String
		 */
		public void setHeader(String header) {
			this.header = header;
		}

		/**
		 * Creates the PdfTemplate that will hold the total number of pages.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
		 *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
		 */
		@Override
		public void onOpenDocument(PdfWriter writer, Document document) {
			total = writer.getDirectContent().createTemplate(30, 16);
		}

		/**
		 * Adds a header to every page
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
		 *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
		 */
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			PdfPTable table = new PdfPTable(3);
			// TODO: logo, create String from Globals

			URL opsiImageURL = getImageResourceURL("opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_kurz_quer.png");
			try {
				// add header table with page number
				table.setWidths(new int[] { 24, 24, 2 });
				table.setTotalWidth(headerWidth); // 527
				table.setLockedWidth(true);
				table.getDefaultCell().setFixedHeight(20);
				table.getDefaultCell().setBorder(Rectangle.BOTTOM);
				table.addCell(header);
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(String.format(" %d / ", writer.getPageNumber()));
				PdfPCell cell = new PdfPCell(Image.getInstance(total));
				cell.setBorder(Rectangle.BOTTOM);
				table.addCell(cell);
				table.writeSelectedRows(0, -1, 34, xHeaderTop, writer.getDirectContent());
				// add footer image
				document.add(createElement(opsiImageURL, 100, 100));
			} catch (DocumentException de) {
				throw new ExceptionConverter(de);
			}
		}

		private URL getImageResourceURL(String relPath) {
			String resourceS = Globals.IMAGE_BASE + relPath;

			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			URL imgURL = cl.getResource(resourceS);
			if (imgURL != null) {
				return imgURL;
			} else {
				Logging.warning("Couldn't find file  " + relPath);
				return null;
			}
		}

		/**
		 * Fills out the total number of pages before the document is closed.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
		 *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
		 */
		@Override
		public void onCloseDocument(PdfWriter writer, Document document) {
			ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
					new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
		}
	}

	// http://kievan.hubpages.com/hub/How-to-Create-a-Basic-iText-PDF-Document
	private static Image createElement(URL imageSource, float width, float height) throws DocumentException {
		Image img = null;

		try {
			img = Image.getInstance(imageSource);
			img.scaleToFit(width, height);
			img.setAbsolutePosition(20, 20);
		} catch (MalformedURLException ex) {
			Logging.error("malformed URL --- " + ex);
		} catch (IOException e) { // getInstannce
			Logging.error("Error document add footer image --- " + e);
		}

		return img;
	}
}
