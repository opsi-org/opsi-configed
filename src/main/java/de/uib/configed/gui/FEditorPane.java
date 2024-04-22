/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

public class FEditorPane extends FGeneralDialog {
	private JEditorPane editPane = new JEditorPane();

	public FEditorPane(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList, preferredWidth, preferredHeight);
		init();
	}

	private void init() {
		editPane.setText("          ");
		editPane.setEditable(false);

		scrollpane.getViewport().add(editPane, null);
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
		} catch (IOException ioe) {
			result = false;
			editPane.setContentType("text/html");
			editPane.setText("<html>Could not load " + url + "<br>" + ioe + "</html>");
		}

		return result;
	}

	public void insertHTMLTable(String s, String title) {
		final String BASE_R_G = "f8f0f0";
		final String HEADER = "<HTML>\n <head>\n  <title>" + title + "</title>\n  </head>\n"
				+ "<body style=\"background-color: " + BASE_R_G + "cc" + "\">\n";

		final String FOOTER = "\n</body>\n</html>";

		editPane.setContentType("text/html");
		editPane.setText(HEADER + s + FOOTER);
		editPane.setCaretPosition(0);
	}
}
