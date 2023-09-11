/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.json.JSONObject;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.HealthInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import utils.Utils;

public class HealthCheckDialog extends FGeneralDialog {
	private static final Pattern pattern = Pattern.compile("OK|WARNING|ERROR");
	private final StyleContext styleContext = StyleContext.getDefaultStyleContext();

	private JTextPane textPane = new JTextPane();
	private DefaultStyledDocument styledDocument = new DefaultStyledDocument();

	private JButton jButtonCollapseAll;
	private JButton jButtonExpandAll;

	private Map<String, Map<String, Object>> healthData;

	public HealthCheckDialog() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("HealthCheckDialog.title"), false,
				new String[] { Configed.getResourceValue("FGeneralDialog.ok") },
				new Icon[] { Utils.createImageIcon("images/checked_withoutbox_blue14.png", "") }, 1, 700, 500, true,
				null);
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "start allLayout");

		if (!Main.THEMES) {
			allpane.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		northPanel = createNorthPanel();
		centerPanel = createCenterPanel();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(northPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE)
				.addComponent(centerPanel).addGap(Globals.HGAP_SIZE).addComponent(southPanel, 2 * Globals.LINE_HEIGHT,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(northPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)));
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setOpaque(false);

		JPopupMenu popupMenu = createPopupMenu();
		northPanel.setComponentPopupMenu(popupMenu);

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		textPane.setStyledDocument(styledDocument);

		textPane.setAutoscrolls(false);
		textPane.setEditable(false);
		textPane.setInheritsPopupMenu(true);
		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Element element = styledDocument.getParagraphElement(textPane.viewToModel2D(event.getPoint()));
				String key = retrieveKeyFromElement(element);

				if (showDetailsForKey(key)) {
					setMessage(healthData);
					textPane.setCaretPosition(textPane.viewToModel2D(event.getPoint()));

					jButtonExpandAll.setEnabled(!isAllDetailsShown());
					jButtonCollapseAll.setEnabled(isDetailsShown());
				}
			}
		});

		healthData = HealthInfo.getHealthDataMap(false);
		setMessage(healthData);
		textPane.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(textPane);
		if (!Main.THEMES) {
			scrollPane.setBackground(Globals.F_GENERAL_DIALOG_BACKGROUND_COLOR);
		}
		scrollPane.setOpaque(false);
		scrollPane.setInheritsPopupMenu(true);

		northLayout.setHorizontalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));
		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));

		return northPanel;
	}

	private boolean showDetailsForKey(String key) {
		if (key.isBlank()) {
			return false;
		}

		if (healthData.containsKey(key)) {
			Map<String, Object> details = healthData.get(key);

			if (((String) details.get("details")).isEmpty()) {
				return false;
			}

			details.put("showDetails", !((boolean) details.get("showDetails")));
			healthData.put(key, details);
		}

		return true;
	}

	private String retrieveKeyFromElement(Element element) {
		String text = "";
		try {
			text = textPane.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset()).trim();
		} catch (BadLocationException e) {
			Logging.warning("could not retrieve text from JTextPane, ", e);
		}
		return (text.isEmpty() || !text.contains(":")) ? "" : text.substring(0, text.indexOf(":"));
	}

	private boolean isDetailsShown() {
		for (Map<String, Object> healthDetails : healthData.values()) {
			if ((boolean) healthDetails.get("showDetails")) {
				return true;
			}
		}
		return false;
	}

	private boolean isAllDetailsShown() {
		for (Map<String, Object> healthDetails : healthData.values()) {
			if (!((boolean) healthDetails.get("showDetails")) && !((String) healthDetails.get("details")).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItemFormatted popupSaveAsZip = new JMenuItemFormatted(
				Configed.getResourceValue("PopupMenuTrait.saveAsZip"), Utils.createImageIcon("images/save.png", ""));

		popupSaveAsZip.addActionListener((ActionEvent e) -> saveAsZip());
		popupMenu.add(popupSaveAsZip);

		return popupMenu;
	}

	private void saveAsZip() {
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Zip file (.zip)", "zip");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showSaveDialog(ConfigedMain.getMainFrame());

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String fileName = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!fileName.endsWith(".zip")) {
				fileName = fileName.concat(".zip");
			}

			String dirname = ConfigedMain.getHost();

			if (dirname.contains(":")) {
				dirname = dirname.replace(":", "_");
			}

			saveDiagnosticDataToFile();

			List<File> files = new ArrayList<>();
			files.add(new File(Configed.getSavedStatesLocationName(),
					dirname + File.separator + Globals.HEALTH_CHECK_LOG_FILE_NAME));
			files.add(new File(Configed.getSavedStatesLocationName(),
					dirname + File.separator + Globals.DIAGNOSTIC_DATA_JSON_FILE_NAME));
			files.add(new File(Logging.getCurrentLogfilePath()));
			zipFiles(fileName, files);
		}
	}

	private void saveDiagnosticDataToFile() {
		String dirname = ConfigedMain.getHost();

		if (dirname.contains(":")) {
			dirname = dirname.replace(":", "_");
		}

		File diagnosticDataFile = new File(Configed.getSavedStatesLocationName(),
				dirname + File.separator + Globals.DIAGNOSTIC_DATA_JSON_FILE_NAME);

		if (diagnosticDataFile.exists() && diagnosticDataFile.length() != 0) {
			Logging.debug(this, "file already exists");
			return;
		}

		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();
		JSONObject jo = new JSONObject(persistenceController.getDiagnosticData());
		writeToFile(diagnosticDataFile, ByteBuffer.wrap(jo.toString(2).getBytes(StandardCharsets.UTF_8)));
	}

	private void writeToFile(File file, ByteBuffer data) {
		if (file == null) {
			Logging.error(this, "provided file is null");
		}

		try (FileOutputStream fos = new FileOutputStream(file); FileChannel channel = fos.getChannel()) {
			channel.write(data);
		} catch (IOException e) {
			Logging.error(this, "" + e);
		}
	}

	private void zipFiles(String zipFile, List<File> files) {
		if (zipFile == null || zipFile.isEmpty()) {
			Logging.info(this, "invalid file name: " + zipFile);
			return;
		}

		if (files.isEmpty()) {
			Logging.info(this, "no files provided");
			return;
		}

		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
			for (File file : files) {
				zipFile(zos, file);
			}
		} catch (IOException e) {
			Logging.error(this, "" + e);
		}
	}

	private void zipFile(ZipOutputStream zos, File file) {
		if (zos == null) {
			Logging.info(this, "ZIP outputstream is null");
			return;
		}

		if (file == null || !file.exists()) {
			Logging.info(this, "provided file doesn't exist");
			return;
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			ZipEntry ze = new ZipEntry(file.getName());
			zos.putNextEntry(ze);

			byte[] buffer = new byte[1024];
			int len = 0;

			while ((len = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			Logging.error(this, "" + e);
		}
	}

	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);

		GroupLayout centerPanelLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerPanelLayout);

		jButtonCollapseAll = new JButton(Configed.getResourceValue("HealthCheckDialog.collapseAll"));
		jButtonCollapseAll.setEnabled(false);

		jButtonExpandAll = new JButton(Configed.getResourceValue("HealthCheckDialog.expandAll"));
		jButtonExpandAll.setEnabled(true);

		JButton jButtonCopyHealthInformation = new JButton(
				Configed.getResourceValue("HealthCheckDialog.copyHealthInformation"));

		centerPanelLayout.setHorizontalGroup(centerPanelLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(jButtonExpandAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE)
				.addComponent(jButtonCollapseAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(
						jButtonCopyHealthInformation, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		centerPanelLayout.setVerticalGroup(centerPanelLayout.createSequentialGroup()
				.addGap(0, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(centerPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonExpandAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCollapseAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCopyHealthInformation, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.HGAP_SIZE));

		jButtonCollapseAll.addActionListener((ActionEvent event) -> {
			healthData = HealthInfo.getHealthDataMap(false);
			setMessage(healthData);
			textPane.setCaretPosition(0);
			jButtonCollapseAll.setEnabled(false);
			jButtonExpandAll.setEnabled(true);
		});
		jButtonExpandAll.addActionListener((ActionEvent event) -> {
			healthData = HealthInfo.getHealthDataMap(true);
			setMessage(healthData);
			textPane.setCaretPosition(0);
			jButtonCollapseAll.setEnabled(true);
			jButtonExpandAll.setEnabled(false);
		});
		jButtonCopyHealthInformation.addActionListener(event -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(textPane.getText()), null));

		return centerPanel;
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 50, 100, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		return southPanel;
	}

	private void setMessage(Map<String, Map<String, Object>> message) {
		try {
			styledDocument.remove(0, styledDocument.getLength());
			for (Map<String, Object> healthInfo : message.values()) {
				styledDocument.insertString(styledDocument.getLength(), ((String) healthInfo.get("message")), null);

				if (!((String) healthInfo.get("details")).isBlank()) {
					Style iconStyle = styledDocument.addStyle("iconStyle", null);
					String imagePath = (boolean) healthInfo.get("showDetails")
							? "images/arrows/arrow_green_16x16-down.png"
							: "images/arrows/arrow_green_16x16-right.png";
					StyleConstants.setIcon(iconStyle, Utils.createImageIcon(imagePath, ""));
					styledDocument.insertString(getMessageStartOffset((String) healthInfo.get("message")), " ",
							iconStyle);
				} else {
					styledDocument.insertString(getMessageStartOffset((String) healthInfo.get("message")), "    ",
							null);
				}

				if ((boolean) healthInfo.get("showDetails")) {
					styledDocument.insertString(styledDocument.getLength(), (String) healthInfo.get("details"), null);
					styledDocument.insertString(styledDocument.getLength(), "\n", null);
				}
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "could not insert message into health check dialog", e);
		}

		Matcher matcher = pattern.matcher(textPane.getText());
		while (matcher.find()) {
			Style style = getStyle(matcher.group());
			styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
		}
	}

	private int getMessageStartOffset(String message) {
		Element root = styledDocument.getDefaultRootElement();
		int offset = styledDocument.getLength() - message.trim().replace("\n", "").replace("\t", " ").length();
		int elementIndex = root.getElementIndex(offset);
		return root.getElement(elementIndex).getStartOffset();
	}

	private Style getStyle(String token) {
		Style style = null;

		switch (token) {
		case "OK":
			style = styleContext.addStyle("ok", null);
			StyleConstants.setForeground(style, Globals.LOG_COLOR_NOTICE);
			break;
		case "WARNING":
			style = styleContext.addStyle("warning", null);
			StyleConstants.setForeground(style, Globals.LOG_COLOR_WARNING);
			break;
		case "ERROR":
			style = styleContext.addStyle("error", null);
			StyleConstants.setForeground(style, Globals.LOG_COLOR_ERROR);
			break;
		default:
			Logging.notice(this, "unsupported token: " + token);
			break;
		}

		return style;
	}
}
