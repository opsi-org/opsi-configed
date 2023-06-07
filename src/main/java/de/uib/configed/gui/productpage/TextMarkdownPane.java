/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class TextMarkdownPane extends JTextPane implements HyperlinkListener {

	public TextMarkdownPane() {
		super.addHyperlinkListener(this);
		super.setEditable(false);

		setContentType("text/html");
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
		Logging.info(this, "Hyperlinkevent in Markdown, inputevent: " + event.getInputEvent());

		String link = event.getURL().toString();

		if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {

			// Open Link in Browser
			Globals.showExternalDocument(link);

		} else if (event.getEventType().equals(HyperlinkEvent.EventType.ENTERED)) {
			// Activate tooltip if mouse on link
			setToolTipText(link);
		} else if (event.getEventType().equals(HyperlinkEvent.EventType.EXITED)) {
			// Deactivates tooltip
			setToolTipText(null);
		} else {
			// Do nothing on other hyperlink events
		}
	}
}
