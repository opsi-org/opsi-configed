/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import com.formdev.flatlaf.util.SystemInfo;

import de.uib.utils.logging.Logging;
import de.uib.utils.script.CmdLauncher;

public class FEditPane extends FEdit implements DocumentListener, MouseListener, MouseMotionListener {
	public static final Dimension AREA_DIMENSION = new Dimension(600, 300);
	public static final String WINDOWS_LINK_INTERPRETER = "explorer.exe";
	public static final String LINUX_LINK_INTERPRETER = "firefox";
	private JTextPane textpane;

	private LinkSearcher searcher;
	private CmdLauncher cmdLauncher;

	private boolean singleLine;

	public FEditPane(String initialText, String hint) {
		super(initialText, hint);
		Logging.info(this.getClass(), " FEdit constructed for >>", initialText, "<< title ", hint);

		initFEditText();
		singleLine = false;
	}

	public FEditPane(String initialText) {
		super(initialText);
		initFEditText();
	}

	private void initFEditText() {
		JScrollPane scrollpane = new JScrollPane();
		textpane = new JTextPane();
		scrollpane.setViewportView(textpane);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editingArea.add(scrollpane, BorderLayout.CENTER);

		textpane.setContentType("text/plain");

		textpane.setEditable(true);

		textpane.addKeyListener(this);
		textpane.addMouseListener(this);
		textpane.addMouseMotionListener(this);

		// we only register changes after loading the initial document

		searcher = new LinkSearcher(textpane);
		searcher.setCaseSensitivity(true);
		Highlighter highlighter = new DefaultHighlighter();
		textpane.setHighlighter(highlighter);
		setDataChanged(false);

		cmdLauncher = new CmdLauncher();
		if (SystemInfo.isWindows) {
			cmdLauncher.setPrefix(WINDOWS_LINK_INTERPRETER);
		} else {
			cmdLauncher.setPrefix(LINUX_LINK_INTERPRETER);
		}

		// HyperlinkListener hyperlinkListener = new
	}

	@Override
	public void setStartText(String s) {
		Logging.debug(this, " FEeditPane setStartText: ", s);
		super.setStartText(s);

		textpane.setText(s);

		searchAndHighlight();

		// we only register changes after loading the initial document
		textpane.getDocument().addDocumentListener(this);

		super.setDataChanged(false);
	}

	private void searchAndHighlight() {
		searcher.searchLinks();
	}

	@Override
	public String getText() {
		// set new initial text for use in processWindowEvent
		initialText = textpane.getText();
		return initialText;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textpane) {
			if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_TAB) {
				buttonCommit.requestFocusInWindow();
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER && singleLine) {
				commit();
			} else {
				// Do nothing on other  keys
			}
		}

		super.keyPressed(e);
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void setDataChanged(boolean b) {
		super.setDataChanged(b);
		searchAndHighlight();
	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			Point p = e.getPoint();

			String line = searcher.getMarkedLine(textpane.viewToModel2D(p));

			if (line != null) {
				Logging.info(this, " got link ", line);
				cmdLauncher.launch("\"" + line + "\"");
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	// MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();

		if (searcher.getMarkedLine(textpane.viewToModel2D(p)) != null) {
			textpane.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else {
			textpane.setCursor(null);
		}
	}
}
