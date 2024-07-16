/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import de.uib.configed.Globals;
import de.uib.utils.logging.Logging;

public class TableHeader extends PdfPageEventHelper {
	/** The header text. */
	private String header = "";

	// format of A4 page
	private boolean isLandscape;

	/** The template with the total number of pages. */
	private PdfTemplate total;

	public TableHeader(boolean isLandscape) {
		this.isLandscape = isLandscape;
	}

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

		URL opsiImageURL = getImageResourceURL("opsilogos/opsi_logo_wide.png");
		int headerWidth;
		int xHeaderTop;
		if (isLandscape) {
			headerWidth = 770;
			xHeaderTop = 555;
		} else {
			headerWidth = 527;
			xHeaderTop = 803;
		}
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

	private static URL getImageResourceURL(String relPath) {
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
		ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber())), 2, 2,
				0);
	}

	// http://kievan.hubpages.com/hub/How-to-Create-a-Basic-iText-PDF-Document
	private static Image createElement(URL imageSource, float width, float height) throws DocumentException {
		Image img = null;

		try {
			img = Image.getInstance(imageSource);
			img.scaleToFit(width, height);
			img.setAbsolutePosition(20, 20);
		} catch (MalformedURLException ex) {
			Logging.error(ex, "malformed URL --- ");
		} catch (IOException e) { // getInstannce
			Logging.error(e, "Error document add footer image --- ");
		}

		return img;
	}
}
