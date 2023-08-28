/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import de.uib.Main;
import de.uib.configed.Globals;

public class FEditorPane extends FGeneralDialog {

	private JEditorPane editPane = new JEditorPane();

	public FEditorPane(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList, preferredWidth, preferredHeight);
		init();
	}

	private void init() {
		editPane.setOpaque(true);

		if (!Main.THEMES) {
			editPane.setBackground(Globals.BACKGROUND_COLOR_4);
		}

		editPane.setText("          ");
		editPane.setEditable(false);
		if (!Main.FONT) {
			editPane.setFont(new Font("Dialog", 0, 10));
		}

		scrollpane.getViewport().add(editPane, null);

		editPane.addKeyListener(this);

		pack();

	}

	public boolean setPage(String url) {

		/*
		 * tip from stackoverflow for a ssl connection:
		 * 
		 * Extend JEditorPane to override the getStream() method.
		 * 
		 * Inside that method, you can open a URLConnection. Test whether it is an
		 * HttpsURLConnection. If it is, initialize your own SSLContext with a custom
		 * X509TrustManager that doesn't perform any checks. Get the context's
		 * SSLSocketFactory and set it as the socket factory for the connection. Then
		 * return the InputStream from the connection.
		 * 
		 * This will defeat any attempts by the runtime to protect the user from a spoof
		 * site serving up malware. If that's really what you want…
		 */

		boolean result = true;
		editPane.setEditable(false);
		try {
			editPane.setPage(url);
		} catch (java.io.IOException ioe) {
			result = false;
			editPane.setContentType("text/html");
			editPane.setText("<html>Could not load " + url + "<br>" + ioe + "</html>");
		}

		return result;
	}

	private static String hexNoForColorizing(int indentationCount) {
		switch (indentationCount) {
		case 0:
			return "aa";
		case 1:
			return "cc";
		case 2:
			return "ff";
		default:
			return "ff";
		}
	}

	public void insertHTMLTable(String s, String title) {
		final String BASE_R_G = "f8f0f0";
		final String HEADER = "<HTML>\n <head>\n  <title>" + title + "</title>\n  </head>\n"
				+ "<body style=\"background-color: " + BASE_R_G + hexNoForColorizing(1) + "\">\n";

		final String FOOTER = "\n</body>\n</html>";

		editPane.setContentType("text/html");
		editPane.setText(HEADER + s + FOOTER);
		editPane.setCaretPosition(0);
	}

	// KeyListener

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_TAB && !shiftPressed && e.getSource() == editPane) {
			jButton1.requestFocus();
		}

		if (e.getKeyCode() == KeyEvent.VK_TAB && shiftPressed && e.getSource() == jButton1) {
			editPane.requestFocus();
		}
	}
}
