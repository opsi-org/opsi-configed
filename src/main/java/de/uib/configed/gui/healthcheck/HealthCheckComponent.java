/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
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

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.HealthDataProcessor;
import de.uib.configed.gui.healthcheck.HealthCheckUpdate.HealthCheckEffect;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class HealthCheckComponent extends
		AbstractTeaComponent<HealthCheckUpdate.HealthCheckModel, HealthCheckMsg, HealthCheckUpdate.HealthCheckEffect> {
	private static final Pattern pattern = Pattern.compile("OK|WARNING|ERROR");
	private final StyleContext styleContext = StyleContext.getDefaultStyleContext();

	private final OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private JTextPane textPane;
	private DefaultStyledDocument styledDocument;
	private JButton jButtonCollapseAll;
	private JButton jButtonExpandAll;

	public HealthCheckComponent() {
		// The model will be initialized in createUI() via initModel()
		// Save health data to file on construction
		saveHealthDataToFile();
	}

	@Override
	protected HealthCheckUpdate.HealthCheckModel initModel() {
		Map<String, Map<String, Object>> initialHealthData = HealthDataProcessor.buildHealthDataForUI(false);
		return new HealthCheckUpdate.HealthCheckModel(initialHealthData);
	}

	@Override
	protected UpdateResult<HealthCheckUpdate.HealthCheckModel, HealthCheckUpdate.HealthCheckEffect> updateModel(
			HealthCheckMsg msg, HealthCheckUpdate.HealthCheckModel model) {
		return HealthCheckUpdate.update(model, msg);
	}

	@Override
	protected JComponent renderView(HealthCheckUpdate.HealthCheckModel model, Consumer<HealthCheckMsg> dispatch) {
		JPanel rootPanel = new JPanel();
		GroupLayout allLayout = new GroupLayout(rootPanel);
		rootPanel.setLayout(allLayout);

		JPanel northPanel = createNorthPanel();
		JPanel centerPanel = createCenterPanel();

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(northPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)
				.addComponent(centerPanel).addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		refreshView();

		return rootPanel;
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		JPopupMenu popupMenu = createPopupMenu();
		northPanel.setComponentPopupMenu(popupMenu);

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		styledDocument = new DefaultStyledDocument();
		textPane = new JTextPane();
		textPane.setStyledDocument(styledDocument);

		textPane.setAutoscrolls(false);
		textPane.setEditable(false);
		textPane.setInheritsPopupMenu(true);
		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Element element = styledDocument.getParagraphElement(textPane.viewToModel2D(event.getPoint()));
				String key = retrieveKeyFromElement(element);

				if (!key.isBlank() && model.getHealthData().containsKey(key)) {
					dispatch(new HealthCheckMsg.ToggleDetails(key));
					textPane.setCaretPosition(textPane.viewToModel2D(event.getPoint()));
					jButtonExpandAll.setEnabled(!allDetailsShown(model.getHealthData()));
					jButtonCollapseAll.setEnabled(anyDetailsShown(model.getHealthData()));
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setInheritsPopupMenu(true);

		northLayout.setHorizontalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));
		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));

		return northPanel;
	}

	private String retrieveKeyFromElement(Element element) {
		String text = "";
		try {
			text = textPane.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset()).trim();
		} catch (BadLocationException e) {
			Logging.warning(e, "could not retrieve text from JTextPane, ");
		}
		return (text.isEmpty() || !text.contains(":")) ? "" : text.substring(0, text.indexOf(":"));
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem popupSaveAsZip = new JMenuItem(Configed.getResourceValue("download"));
		Icons.addIntellijIconToMenuItem(popupSaveAsZip, "download");

		popupSaveAsZip.addActionListener(actionEvent -> dispatch(HealthCheckMsg.SimpleMsg.DOWNLOAD_DIAGNOSTIC_DATA));
		popupMenu.add(popupSaveAsZip);

		return popupMenu;
	}

	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel();

		GroupLayout centerPanelLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerPanelLayout);

		jButtonCollapseAll = new JButton(Configed.getResourceValue("HealthCheckDialog.collapseAll"));
		jButtonCollapseAll.setEnabled(anyDetailsShown(model.getHealthData()));

		jButtonExpandAll = new JButton(Configed.getResourceValue("HealthCheckDialog.expandAll"));
		jButtonExpandAll.setEnabled(!allDetailsShown(model.getHealthData()));

		JButton jButtonCopyHealthInformation = new JButton(Configed.getResourceValue("copy"));

		JButton jButtonDownloadDiagnosticData = new JButton(
				Configed.getResourceValue("HealthCheckDialog.downloadDiagnosticData"));
		jButtonDownloadDiagnosticData
				.setToolTipText(Configed.getResourceValue("HealthCheckDialog.downloadDiagnosticData.tooltip"));

		centerPanelLayout.setHorizontalGroup(centerPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jButtonExpandAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonCollapseAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(jButtonCopyHealthInformation, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jButtonDownloadDiagnosticData, 10, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
		centerPanelLayout.setVerticalGroup(centerPanelLayout.createSequentialGroup()
				.addGap(0, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(centerPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonExpandAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCollapseAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCopyHealthInformation, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonDownloadDiagnosticData, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		jButtonCollapseAll.addActionListener((ActionEvent event) -> dispatch(HealthCheckMsg.SimpleMsg.COLLAPSE_ALL));
		jButtonExpandAll.addActionListener((ActionEvent event) -> dispatch(HealthCheckMsg.SimpleMsg.EXPAND_ALL));
		jButtonCopyHealthInformation
				.addActionListener(event -> dispatch(HealthCheckMsg.SimpleMsg.COPY_HEALTH_INFORMATION));
		jButtonDownloadDiagnosticData
				.addActionListener(event -> dispatch(HealthCheckMsg.SimpleMsg.DOWNLOAD_DIAGNOSTIC_DATA));

		return centerPanel;
	}

	@Override
	protected void handleEffect(HealthCheckEffect effect) {
		switch (effect) {
		case HealthCheckEffect.SimpleEffect e -> handleSimpleEffect(e);
		}
	}

	@SuppressWarnings("java:S1301")
	private void handleSimpleEffect(HealthCheckEffect.SimpleEffect effect) {
		switch (effect) {
		case COPY -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(textPane.getText()), null);
		case DOWNLOAD -> saveAsZip();
		}
	}

	@Override
	protected void refreshView() {
		if (styledDocument == null || model == null) {
			return;
		}
		setMessage(model.getHealthData());

		if (jButtonExpandAll != null) {
			jButtonExpandAll.setEnabled(!allDetailsShown(model.getHealthData()));
		}
		if (jButtonCollapseAll != null) {
			jButtonCollapseAll.setEnabled(anyDetailsShown(model.getHealthData()));
		}
	}

	private void setMessage(Map<String, Map<String, Object>> message) {
		try {
			styledDocument.remove(0, styledDocument.getLength());
			for (Map<String, Object> healthInfo : message.values()) {
				styledDocument.insertString(styledDocument.getLength(),
						((String) healthInfo.get(HealthDataProcessor.KEY_MESSAGE)), null);

				if (!((String) healthInfo.get(HealthDataProcessor.KEY_DETAILS)).isBlank()) {
					Style iconStyle = styledDocument.addStyle("iconStyle", null);
					String imagePath = Boolean.TRUE.equals(healthInfo.get(HealthDataProcessor.KEY_SHOW_DETAILS))
							? "arrowDown"
							: "arrowRight";
					StyleConstants.setIcon(iconStyle, Icons.getIntellijIcon(imagePath));
					styledDocument.insertString(
							getMessageStartOffset((String) healthInfo.get(HealthDataProcessor.KEY_MESSAGE)), " ",
							iconStyle);
				} else {
					styledDocument.insertString(
							getMessageStartOffset((String) healthInfo.get(HealthDataProcessor.KEY_MESSAGE)), "    ",
							null);
				}

				if (Boolean.TRUE.equals(healthInfo.get(HealthDataProcessor.KEY_SHOW_DETAILS))) {
					styledDocument.insertString(styledDocument.getLength(),
							(String) healthInfo.get(HealthDataProcessor.KEY_DETAILS), null);
					styledDocument.insertString(styledDocument.getLength(), "\n", null);
				}
			}
		} catch (BadLocationException e) {
			Logging.warning(this, e, "could not insert message into health check dialog");
		}

		highlightStatusText();
	}

	private void highlightStatusText() {
		try {
			Matcher matcher = pattern.matcher(styledDocument.getText(0, styledDocument.getLength()));
			while (matcher.find()) {
				Style style = getStyle(matcher.group());
				styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
			}
		} catch (BadLocationException e) {
			Logging.error(this, "failed to retrieve text", e);
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
			Logging.notice(this, "unsupported token: ", token);
			break;
		}

		return style;
	}

	private void saveHealthDataToFile() {
		File healthDataFile = new File(getDirectoryLocation(), Globals.HEALTH_CHECK_LOG_FILE_NAME);
		writeToFile(healthDataFile,
				ByteBuffer.wrap(HealthDataProcessor.buildHealthDataForExport().getBytes(StandardCharsets.UTF_8)));
	}

	private void saveDiagnosticDataToFile() {
		File diagnosticDataFile = new File(getDirectoryLocation(), Globals.DIAGNOSTIC_DATA_JSON_FILE_NAME);
		JSONObject jo = new JSONObject(persistenceController.getHealthDataService().getDiagnosticDataPD());
		writeToFile(diagnosticDataFile, ByteBuffer.wrap(jo.toString(2).getBytes(StandardCharsets.UTF_8)));
	}

	private static String getDirectoryLocation() {
		String dirname = PersistenceControllerFactory.getPersistenceController().getExecutioner().getHost();
		if (dirname.contains(":")) {
			dirname = dirname.replace(":", "_");
		}
		return new File(Configed.getSavedStatesLocationName(), dirname).toString();
	}

	private void writeToFile(File file, ByteBuffer data) {
		if (file == null) {
			Logging.error(this, "provided file is null");
		}

		try (FileOutputStream fos = new FileOutputStream(file); FileChannel channel = fos.getChannel()) {
			channel.write(data);
		} catch (IOException e) {
			Logging.error(this, e, "");
		}
	}

	public void saveAsZip() {
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jFileChooser.setFileHidingEnabled(false);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Zip file (.zip)", "zip");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showSaveDialog(ConfigedMain.getMainFrame());

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String fileName = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!fileName.endsWith(".zip")) {
				fileName = fileName.concat(".zip");
			}

			String dirname = persistenceController.getExecutioner().getHost();

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

	private void zipFiles(String zipFile, List<File> files) {
		if (zipFile == null || zipFile.isEmpty()) {
			Logging.info(this, "invalid file name: ", zipFile);
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
			Logging.error(this, e, "");
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
			Logging.error(this, e, "");
		}
	}

	private static boolean allDetailsShown(Map<String, Map<String, Object>> healthData) {
		return healthData.values().stream().flatMap(innerMap -> innerMap.entrySet().stream())
				.filter(entry -> HealthDataProcessor.KEY_SHOW_DETAILS.equals(entry.getKey()))
				.allMatch(entry -> Boolean.TRUE.equals(entry.getValue()));
	}

	private static boolean anyDetailsShown(Map<String, Map<String, Object>> healthData) {
		return healthData.values().stream().flatMap(innerMap -> innerMap.entrySet().stream())
				.filter(entry -> HealthDataProcessor.KEY_SHOW_DETAILS.equals(entry.getKey()))
				.anyMatch(entry -> Boolean.TRUE.equals(entry.getValue()));
	}
}