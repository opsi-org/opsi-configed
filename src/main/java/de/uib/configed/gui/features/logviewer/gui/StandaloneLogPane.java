/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.gui;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.logpane.LogPaneComponent;
import de.uib.configed.gui.features.logpane.LogPaneModel;
import de.uib.configed.gui.features.logpane.LogPaneMsg;
import de.uib.configed.share.logging.Logging;

public class StandaloneLogPane extends LogPaneComponent {
	private LogFrame logFrame;

	public StandaloneLogPane(LogFrame logFrame) {
		super(LogPaneModel.builder().build());

		this.logFrame = logFrame;
	}

	@Override
	public void reload() {
		int caretPosition = getCaretPosition();
		super.dispatch(new LogPaneMsg.ParseLogRequest(reloadFile(logFrame.getFileName())));
		super.setTitle(logFrame.getFileName());
		super.setCaretPosition(caretPosition);
		super.removeAllHighlights();
	}

	public void close() {
		LogFrame.resetFileName();
		super.dispatch(new LogPaneMsg.ParseLogRequest(logFrame.getFileName()));
		super.setTitle(logFrame.getFileName());
		super.removeAllHighlights();
	}

	@Override
	public void download() {
		String fn = LogFrame.openFile(Configed.getResourceValue("LogFrame.jMenuFileSave"));
		if (fn != null && !fn.isEmpty()) {
			saveToFile(fn, logTextPane.getLines());
			super.setTitle(fn);
		}
	}

	private String reloadFile(String fn) {
		if (fn != null && !fn.isEmpty()) {
			return logFrame.readFile(fn);
		} else {
			Logging.error(this, "File does not exist: ", fn);
			JOptionPane.showMessageDialog(logFrame, Configed.getResourceValue("LogFrame.fileDoesNotExist") + " " + fn,
					null, JOptionPane.WARNING_MESSAGE);
			return "";
		}
	}

	private void saveToFile(String filename, String[] logfilelines) {
		try (FileWriter fWriter = new FileWriter(filename, StandardCharsets.UTF_8)) {
			int i = 0;
			while (i < logfilelines.length) {
				fWriter.write(logfilelines[i] + "\n");
				logFrame.setTitle(filename);
				i++;
			}
		} catch (IOException ex) {
			Logging.error(ex, "Error encountered while trying to save to file: ", filename,
					"\n --- ; stop saving to file");
		}
	}
}
