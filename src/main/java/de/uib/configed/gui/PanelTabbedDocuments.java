package de.uib.configed.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ClippedTitleTabbedPane;

public class PanelTabbedDocuments extends ClippedTitleTabbedPane {
	LogPane[] textPanes;
	String[] idents;
	final List<String> identsList;

	protected JFileChooser chooser;
	protected File chooserDirectory;

	public PanelTabbedDocuments(final String[] idents, String defaultText) {

		this.idents = idents;

		identsList = Arrays.asList(idents);

		super.setProportionOfTotalWidth(0.5);

		super.setForeground(Globals.blue);

		textPanes = new LogPane[idents.length];

		for (int i = 0; i < idents.length; i++) {
			final String ident = idents[i];
			LogPane showPane = new LogPane(defaultText) {
				@Override
				protected void reload() {
					super.reload();
					loadDocument(ident);
				}

				@Override
				protected void save() {
					String filename = ident;
					if (getInfo() != null) {
						filename = getInfo().replace('.', '_') + "___" + ident + ".log";
					}
					Logging.debug(this, "save with filename " + filename);
					String pathname = openFile(filename + ".log");
					if (pathname != null && !pathname.equals("")) {
						saveToFile(pathname, lines);
					}

				}

				@Override
				protected void saveAsZip() {
					Logging.info(this, "saveAsZip");

					String filename = ident;
					if (getInfo() != null) {
						filename = getInfo().replace('.', '_') + "___" + ident + ".log";
					}
					String pathname = openFile(filename + ".zip");
					if (pathname != null && !pathname.equals("")) {
						saveToZipFile(pathname, filename, lines);
					}

				}

				@Override
				protected void saveAllAsZip(boolean loadMissingDocs) {
					Logging.info(this, "saveAllAsZip got ident " + ident + " loadMissingDocs " + loadMissingDocs);

					String fname = ident;
					if (getInfo() != null) {
						fname = getInfo().replace('.', '_') + "_all";
					}

					Logging.info(this, "saveAllAsZip, start getting pathname");
					String pathname = openFile(fname + ".zip");

					if (pathname != null && !pathname.equals("")) {
						Logging.info(this, "saveAllAsZip, got pathname");

						if (loadMissingDocs) {
							for (int logNo = 0; logNo < idents.length; logNo++) {
								if (textPanes[logNo].lines.length <= 1)// empty
								{
									loadDocument(idents[logNo]);
								}

								Logging.info(this, "saveAllAsZip textPanes[" + logNo + "].lines.length "
										+ textPanes[logNo].lines.length);

							}
						}

						saveAllToZipFile(pathname);
					}

				}

			};

			textPanes[i] = showPane;

			super.addTab(ident, textPanes[i]);

		}

	}

	public void loadDocument(String ident)
	// override in subclasses
	{
		Logging.debug(this, "loadDocument ident " + ident);
	}

	private void setDocument(int i, final String document, final String info) {
		Logging.info(this, "setDocument " + i + " document == null " + (document == null));
		if (i < 0 || i >= idents.length) {
			return;
		}

		if (document == null) {
			textPanes[i].setText("");
			textPanes[i].setTitle("");
			return;
		}

		textPanes[i].setTitle(idents[i] + "  " + info);

		// should be name of client, delivered from info textfield
		textPanes[i].setInfo(info);
		textPanes[i].setText(document);

	}

	private void setDocument(String ident, final String document, final String info) {
		int i = identsList.indexOf(ident);
		setDocument(i, document, info);
	}

	public void setDocuments(final Map<String, String> documents, final String info) {
		Logging.info(this, " ------------------------------  idents.length " + idents.length + " info: " + info);
		for (int i = 0; i < idents.length; i++) {
			setDocument(idents[i], documents.get(idents[i]), info);
		}

	}

	private void setFileChooser(String fn) {
		if (chooser == null) {
			chooser = new JFileChooser(fn);
			chooser.setPreferredSize(Globals.filechooserSize);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z",
					"log", "zip", "gz", "7z"));

			chooser.setApproveButtonText("O.K.");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(
					Globals.APPNAME + " " + Configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
		}
	}

	private String openFile(String typename) {
		String fileName = null;

		// Guarantee that chooser is not null
		if (chooser == null) {
			setFileChooser("");
			chooserDirectory = chooser.getCurrentDirectory();
		}

		File f = new File(chooserDirectory, typename);
		chooser.setSelectedFile(f);

		int returnVal = chooser.showSaveDialog(ConfigedMain.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
			chooserDirectory = chooser.getCurrentDirectory();
		}

		return fileName;
	}

	private void saveToFile(String fn, String[] lines) {
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(fn);
		} catch (IOException ex) {
			Logging.error("Error opening file: " + fn + "\n --- " + ex);
			return;
		}
		int i = 0;
		while (i < lines.length) {
			try {
				fWriter.write(lines[i] + "\n");

			} catch (IOException ex) {
				Logging.error("Error writing file: " + fn + "\n --- " + ex);
			}
			i++;
		}
		try {
			fWriter.close();
		} catch (IOException ex) {
			Logging.error("Error closing file: " + fn + "\n --- " + ex);
		}
	}

	private void saveAllToZipFile(String pn) {

		byte[] crlf = new byte[2];
		crlf[0] = '\r';
		crlf[1] = '\n';

		byte[] buffer;
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pn))) {

			out.setMethod(ZipOutputStream.DEFLATED);
			for (int logNo = 0; logNo < idents.length; logNo++) {
				// load all logfiles ???
				//
				// save only if not empty
				if (textPanes[logNo].lines.length > 1) {
					ZipEntry entry = new ZipEntry(textPanes[logNo].title.replace(" ", "_").replace(".", "_") + ".log");
					out.putNextEntry(entry);
					int i = 0;
					while (i < textPanes[logNo].lines.length) {
						try {
							buffer = textPanes[logNo].lines[i].getBytes();
							out.write(buffer, 0, textPanes[logNo].lines[i].length());
							out.write(crlf, 0, 2);
						} catch (IOException ex) {
							Logging.error("Error writing zip file: " + pn + "\n --- " + ex);
						}
						i++;
					}
				}
				out.closeEntry();
			}
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + pn + "\n --- " + ex);
		}
	}

	private void saveToZipFile(String pn, String fn, String[] lines) {

		byte[] crlf = new byte[2];
		crlf[0] = '\r';
		crlf[1] = '\n';
		byte[] buffer;
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pn))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry entry = new ZipEntry(fn);
			out.putNextEntry(entry);
			int i = 0;
			while (i < lines.length) {
				try {

					buffer = lines[i].getBytes();
					out.write(buffer, 0, lines[i].length());
					out.write(crlf, 0, 2);
				} catch (IOException ex) {
					Logging.error("Error writing zip file: " + fn + "\n --- " + ex);
				}
				i++;
			}
			out.closeEntry();
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + fn + "\n --- " + ex);
		}

	}

}
