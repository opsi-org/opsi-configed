/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.util.Arrays;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import de.uib.utilities.logging.Logging;
import utils.Utils;

public class TextMarkdownPane extends JTextPane {
	public TextMarkdownPane() {
		super.addHyperlinkListener(this::hyperlinkUpdate);
		super.setEditable(false);

		DefaultCaret caret = (DefaultCaret) super.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		setContentType("text/html");
	}

	@Override
	public void setText(String s) {
		Parser parser = Parser.builder().extensions(Arrays.asList(AutolinkExtension.create())).build();
		Node document = parser.parse(s);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);
		super.setText(html);
	}

	private void hyperlinkUpdate(HyperlinkEvent event) {
		Logging.info(this, "Hyperlinkevent in Markdown, inputevent: " + event.getInputEvent());

		String link = event.getURL().toString();

		if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			Utils.showExternalDocument(link);
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
