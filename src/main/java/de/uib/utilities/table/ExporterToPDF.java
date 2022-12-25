package de.uib.utilities.table;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JTable;

import com.itextpdf.text.BadElementException;
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

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.pdf.OpenSaveDialog;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class ExporterToPDF extends ExportTable {
	protected static Document document;
	protected static PdfWriter writer;

	protected OpenSaveDialog dialog;
	protected Boolean saveAction;

	protected String defaultFilename = "report.pdf";
	protected boolean askForOverwrite = true;

	protected static final String thisExtension = ".pdf";

	private static float mLeft = 36;
	private static float mRight = 36;
	private static float mTop = 74; // with header
	private static float mBottom = 54;
	private float xHeaderTop = 803;
	private float headerWidth = 527;

	private static BaseFont bf;
	private static Font catFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	private static Font small = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
	private static ArrayList<Integer> leftAlignmentlist = new ArrayList<>();

	public ExporterToPDF(javax.swing.JTable table, Vector<String> classNames) {
		super(table, classNames);
		extension = thisExtension;
		writeToFile = defaultFilename;
		document = new Document(PageSize.A4, mLeft, mRight, mTop, mBottom);
	}

	public ExporterToPDF(PanelGenEditTable table, Vector<String> classNames) {
		this(table.getTheTable(), classNames);
	}

	public ExporterToPDF(PanelGenEditTable table) {
		this(table.getTheTable(), null);
	}

	public ExporterToPDF(javax.swing.JTable table) {
		this(table, null);
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {
		document = new Document(PageSize.A4, mLeft, mRight, mTop, mBottom);
		setPageSizeA4_Landscape();

		if (fileName != null)
			saveAction = true;

		String filePath = null;
		File temp = null;

		defaultFilename = "report_" + client + extension;

		if (saveAction == null) {
			if (dialog == null) {
				dialog = new OpenSaveDialog(configed.getResourceValue("OpenSaveDialog.title"));
			} else {
				dialog.setVisible();
			}

			saveAction = dialog.getSaveAction();
		}

		if (saveAction != null) {
			if (saveAction) {
				if (fileName != null) {
					try {
						File file = new File(fileName);
						if (file.isDirectory())
							logging.error("no valid filename " + fileName);
						else
							filePath = file.getAbsolutePath();
					} catch (Exception e) {
						logging.error("no valid filename " + fileName);
					}
				}

				if (fileName == null)
					fileName = getFileLocation();

				fileName = checkExtension(fileName);
				fileName = checkFile(fileName, extensionFilter);

			} else {
				try {
					temp = File.createTempFile(defaultFilename.substring(0, defaultFilename.indexOf(".")), ".pdf");
					filePath = temp.getAbsolutePath();
				} catch (IOException e) {
					logging.error("Failed to create temp file", e);
				}
			}
			try {
				if (filePath == null) {
					writer = PdfWriter.getInstance(document, new FileOutputStream(defaultFilename));
				} else {
					writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
				}

				try {
					TableHeader event = new TableHeader();
					if (metaData.containsKey("header"))
						event.setHeader(metaData.get("header"));
					else if (metaData.containsKey("title"))
						event.setHeader(metaData.get("title"));
					writer.setPageEvent(event);
				} catch (Exception ex) {
					logging.error("Error PdfWriter --- " + ex);
				}

				document.open();
				addMetaData(metaData);
				document.add(addTitleLines(metaData));
				document.add(createTableDataElement(theTable)); // table data

				document.close();

			} catch (FileNotFoundException e) {
				logging.error("file not found: " + fileName, e);
			} catch (Exception exp) {
				logging.error("file not found: " + fileName, exp);
			}

			if ((saveAction == false) && (temp.getAbsolutePath() != null)) {
				try {

					Desktop.getDesktop().open(temp);

				} catch (Exception e) {
					logging.error("cannot show: " + temp.getAbsolutePath() + " : " + e);
				}

			}

		}

	}

	public void addMetaData(HashMap<String, String> metaData) {
		if (metaData == null) {
			document.addTitle("Document as PDF");
			document.addSubject("Using iText");
			document.addKeywords("Java, PDF, iText");
		} else {
			if (metaData.containsKey("title"))
				document.addTitle(metaData.get("title").toString());
			if (metaData.containsKey("subject"))
				document.addSubject(metaData.get("subject").toString());
			if (metaData.containsKey("keywords"))
				document.addKeywords(metaData.get("keywords").toString());
		}
		document.addAuthor(System.getProperty("user.name"));
		document.addCreator(Globals.APPNAME);
	}

	public void setPageSizeA4() {
		document.setPageSize(PageSize.A4);
		headerWidth = 527;
		xHeaderTop = 803;
	}

	public void setPageSizeA4_Landscape() {
		document.setPageSize(PageSize.A4.rotate());
		headerWidth = 770;
		xHeaderTop = 555;
	}

	public void setSaveAction(boolean action) {
		saveAction = action;
	}

	public static Paragraph addEmptyLines(int number) {
		Paragraph content = new Paragraph();
		for (int i = 0; i < number; i++) {
			content.add(new Paragraph(" "));
		}
		return content;
	}

	public static Paragraph addTitleLines(HashMap<String, String> metaData) throws DocumentException {
		// TODO timezone
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd. MMMMM yyyy");
		// Second parameter is the number of the chapter
		Paragraph content = new Paragraph();

		if (metaData.containsKey("title"))
			content.add(new Paragraph(metaData.get("title"), catFont));
		if (metaData.containsKey("subtitle"))
			content.add(new Paragraph(metaData.get("subtitle"), smallBold));
		String userInitial = "";
		if (System.getProperty("user.name") != null) {
			userInitial = System.getProperty("user.name");
			int cutpos = 3;
			int nameLength = userInitial.length();

			if (nameLength >= cutpos) {
				userInitial = userInitial.substring(0, cutpos);
				if (System.getProperty("user.name").length() > cutpos)
					userInitial = userInitial + "..";
			}
		}

		content.add(new Paragraph(
				de.uib.configed.configed.getResourceValue("DocumentExport.summonedBy") + ": " + userInitial + ", " //$NON-NLS-3$
						+ dateFormatter.format(new Date()), //$NON-NLS-2$ //$NON-NLS-3$
				smallBold));
		content.add(addEmptyLines(1));
		return content;
	}

	protected PdfPTable createTableDataElement(JTable theTable) throws BadElementException {
		Boolean onlySelectedRows = false;

		if (theTable.getSelectedRowCount() > 0)
			onlySelectedRows = true;

		PdfPTable table = new PdfPTable(theTable.getColumnCount());
		PdfPCell h;
		PdfPCell value = null;

		table.setWidthPercentage(98);

		BaseColor headerBackground = new BaseColor(150, 150, 150);
		BaseColor evenBackground = new BaseColor(230, 230, 230);
		BaseColor oddBackground = new BaseColor(250, 250, 250);
		Font symbol_font;
		try {
			bf = BaseFont.createFont(BaseFont.SYMBOL, BaseFont.SYMBOL, BaseFont.EMBEDDED);
			symbol_font = new Font(bf, 11);
		} catch (Exception e) {
			logging.warning("ExporterToPDF::createTableDataElement", " BaseFont can't be created :" + e);
			symbol_font = small;
		}
		PdfPCell defaultCell = table.getDefaultCell();
		defaultCell.setBackgroundColor(new BaseColor(100, 100, 100));

		for (int i = 0; i < theTable.getColumnCount(); i++) {
			h = new PdfPCell(new Phrase(theTable.getColumnName(i)));
			h.setHorizontalAlignment(Element.ALIGN_CENTER);
			h.setBackgroundColor(headerBackground);
			table.addCell(h);
		}
		table.setHeaderRows(1);

		for (int j = 0; j < theTable.getRowCount(); j++)

			if (!onlySelectedRows | theTable.isRowSelected(j)) {

				for (int i = 0; i < theTable.getColumnCount(); i++) {
					value = new PdfPCell(new Phrase(" "));
					String s = "";
					try {
						s = theTable.getValueAt(j, i).toString();
					} catch (Exception ex) {
						s = "";
					}
					switch (s) {
					case "∞":
						value = new PdfPCell(new Phrase("\u221e", symbol_font));
						break;
					case "true":
						value = new PdfPCell(new Phrase("\u221a", symbol_font)); // radic
						break;
					case "false":
						break;
					default:
						value = new PdfPCell(new Phrase(s, small));
					}
					if (j % 2 == 0)
						value.setBackgroundColor(evenBackground);
					else
						value.setBackgroundColor(oddBackground);
					if (leftAlignmentlist.contains(i)) {
						value.setHorizontalAlignment(Element.ALIGN_LEFT);
					} else {
						value.setHorizontalAlignment(Element.ALIGN_CENTER);
					}
					value.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(value);
				}
			}

		return table;

	}

	/**
	 * Inner class to add a table as header.
	 */
	class TableHeader extends PdfPageEventHelper {
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

			java.net.URL opsi_image_URL = Globals.getImageResourceURL("images/opsi_full.png");
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
				document.add(createElement(opsi_image_URL, 25, 25));

			} catch (DocumentException de) {
				throw new ExceptionConverter(de);
			} catch (MalformedURLException ex) {
				logging.error("malformed URL --- " + ex);
			} catch (IOException e) { // getInstannce
				logging.error("Error document add footer image --- " + e);
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

	public static Image createElement(URL imageSource, float posx, float posy)
			// http://kievan.hubpages.com/hub/How-to-Create-a-Basic-iText-PDF-Document
			throws DocumentException, IOException {
		Image img = com.itextpdf.text.Image.getInstance(imageSource);
		// no scaling
		img.setAbsolutePosition(posx, posy);
		return img;
	}
}
