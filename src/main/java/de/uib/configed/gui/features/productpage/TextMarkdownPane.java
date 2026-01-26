/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import de.uib.configed.gui.share.swing.WrapEditorKit;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class TextMarkdownPane extends JTextPane {
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
		super.setText(Utils.parseMarkdown(s));
	}

	private void hyperlinkUpdate(HyperlinkEvent event) {
		Logging.info(this, "Hyperlinkevent in Markdown, inputevent: ", event.getInputEvent());

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
