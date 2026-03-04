/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.Arrays;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import de.uib.configed.gui.share.swing.WrapEditorKit;
import de.uib.configed.share.BrowserUtils;
import de.uib.configed.share.logging.Logging;

public class TextMarkdownPane extends JTextPane {
	private static Parser markdownParser = Parser.builder()
			.extensions(Arrays.asList(AutolinkExtension.create(), TablesExtension.create())).build();
	private static HtmlRenderer renderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();

	public TextMarkdownPane() {
		super.addHyperlinkListener(this::hyperlinkUpdate);
		super.setEditable(false);
		super.setEditorKit(WrapEditorKit.HTML.create());

		DefaultCaret caret = (DefaultCaret) super.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		setContentType("text/html");
	}

	@Override
	public void setText(String s) {
		super.setText(parseMarkdown(s));
	}

	private void hyperlinkUpdate(HyperlinkEvent event) {
		Logging.info(this, "Hyperlinkevent in Markdown, inputevent: ", event.getInputEvent());

		String link = event.getURL().toString();

		if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			BrowserUtils.openLink(link);
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

	public static String parseMarkdown(String markdown) {
		if (markdown == null) {
			return "";
		}

		Node document = markdownParser.parse(markdown);
		return renderer.render(document);
	}
}
