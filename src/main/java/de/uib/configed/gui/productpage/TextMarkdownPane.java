/*
 *  TextMarkdownPane.java
 * 
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2019 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 */

package de.uib.configed.gui.productpage;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class TextMarkdownPane extends JTextPane implements HyperlinkListener {

	public TextMarkdownPane() {
		super();

		addHyperlinkListener(this);
		setEditable(false);
		setContentType("text/html");
		setText("Das hier ist ein Abschnitt über Farben. Im folgenden Abschnitt möchten wir einige Beispiele für "
				+ " Farben geben und anschließend noch weitere Ressourcen für weiterreichende Informationen geben.\n## Farben\n * Rot\n"
				+ "* Grün\n" + "* Gelb\n" + "\nFür mehr Farben besuchen Sie doch unsere [Website](https://www.uib.de)");
	}

	@Override
	public void setText(String s) {

		Parser parser = Parser.builder().build();
		Node document = parser.parse(s);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);

		html = html.replace("<p>", "");
		html = html.replace("</p>", "");

		super.setText(html);
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
			Application appl = new Application() {

				@Override
				public void start(Stage primaryStage) throws Exception {
					// TODO Auto-generated method stub

				}

			};
			HostServices services = appl.getHostServices();
			services.showDocument(event.getURL().toString());
		}

		/*if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
			Desktop desktop = Desktop.getDesktop();
		
			URL clickedURL = event.getURL();
			String clickedString = clickedURL.toString();
		
			// This will now try to open the standard browser with link.
			// if not possible, try to open firefox with the link
			// And if even that is not possible, show Message Dialog with link
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(clickedURL.toURI());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				try {
					logging.info(this, "trying to open link with firefox, because Desktop.brose, not supported");
					Process process = new ProcessBuilder("firefox", clickedString).start();
		
					// check if opening with firefox successful
					if (process.waitFor() != 0) {
						logging.trace(this, "firefox not available, show message frame");
						openMessageDialogWithURL(clickedString);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
					openMessageDialogWithURL(clickedString);
		
				} catch (InterruptedException ie) {
					ie.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}*/

	}

	private void openMessageDialogWithURL(String clickedString) {
		JTextPane pa = new JTextPane();
		pa.setEditable(false);
		pa.setText("Browser zum öffnen von \n" + clickedString
				+ "\nkann nicht gefunden werden. Bitte manuell durchführen");
		JOptionPane.showMessageDialog(this, pa);
	}

}
