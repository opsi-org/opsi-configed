/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.terminal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class TerminalFileUploadProgressIndicator extends JPanel {
	private JProgressBar fileUploadProgressBar;
	private JLabel uploadedFilesLabel;
	private JLabel fileNameLabel;

	public void init() {
		JLabel uploadingFileLabel = new JLabel(Configed.getResourceValue("Terminal.uploadingFile"));
		fileNameLabel = new JLabel();
		uploadedFilesLabel = new JLabel();

		fileUploadProgressBar = new JProgressBar();
		fileUploadProgressBar.setStringPainted(true);

		setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", fillx, wrap 4",
				"[pref!]push[pref!]push[pref!][pref!]", "[center]"));
		add(uploadingFileLabel, "aligny center, hmin 10, shrink 1");
		add(fileNameLabel, "aligny center, hmin 10, shrink 1");
		add(uploadedFilesLabel, "aligny center, hmin 10, shrink 1");
		add(fileUploadProgressBar, "aligny center, hmin 10");
	}

	public void indicateFileUpload(File file, int uploadedFiles, int totalFiles) {
		showFileUploadProgress(true);

		try {
			fileUploadProgressBar.setMaximum((int) Files.size(file.toPath()));
		} catch (IOException e) {
			Logging.warning(this, e, "unable to retrieve file size: ");
		}

		uploadedFilesLabel.setText(uploadedFiles + "/" + totalFiles);
		fileNameLabel.setText(file.getAbsolutePath());
	}

	public void updateFileUploadProgressBar(int progress, long fileSize) {
		if (!isVisible()) {
			showFileUploadProgress(true);
		}

		ByteUnitConverter converter = new ByteUnitConverter();
		ByteUnit byteUnit = converter.detectByteUnit(fileSize);
		String uploadedFileSize = converter.asString(converter.convertByteUnit(progress, byteUnit), byteUnit);
		String totalFileSize = converter.asString(converter.convertByteUnit(fileSize, byteUnit), byteUnit);

		fileUploadProgressBar.setValue(progress);
		fileUploadProgressBar.setString(uploadedFileSize + "/" + totalFileSize);
		fileUploadProgressBar.repaint();
	}

	public void showFileUploadProgress(boolean show) {
		setVisible(show);
	}
}
