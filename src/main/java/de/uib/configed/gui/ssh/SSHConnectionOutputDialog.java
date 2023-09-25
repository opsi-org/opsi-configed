/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.utilities.logging.Logging;

public class SSHConnectionOutputDialog extends FGeneralDialog {
	private static final String ANSI_ESCAPE_1 = "\u001B";
	public static final String ANSI_CODE_END_1 = "\u001B[0;0;0m";
	public static final String ANSI_CODE_END_2 = "[0;0;0m";

	// user info not really ansi code !!
	public static final String ANSI_CODE_INFO = "[0;info;0m";
	public static final String ANSI_CODE_ERROR = "[0;error;0m";
	private static final Map<String, Color> ANSI_CODE_COLORS = new HashMap<>();
	static {
		ANSI_CODE_COLORS.put("[0;info;0m", Globals.GREYED);
		ANSI_CODE_COLORS.put("[0;error;0m", Globals.ACTION_COLOR);
		ANSI_CODE_COLORS.put("[0;30;40m", null);
		ANSI_CODE_COLORS.put("[1;30;40m", null);
		ANSI_CODE_COLORS.put("[0;40;40m", null);
		ANSI_CODE_COLORS.put("[1;40;40m", null);
		ANSI_CODE_COLORS.put("[0;31;40m", Globals.ACTION_COLOR);
		ANSI_CODE_COLORS.put("[1;31;40m", Globals.ACTION_COLOR);
		ANSI_CODE_COLORS.put("[0;41;40m", Globals.ACTION_COLOR);
		ANSI_CODE_COLORS.put("[1;41;40m", Globals.ACTION_COLOR);
		ANSI_CODE_COLORS.put("[0;32;40m", Globals.OK_COLOR);
		ANSI_CODE_COLORS.put("[1;32;40m", Globals.OK_COLOR);
		ANSI_CODE_COLORS.put("[0;42;40m", Globals.OK_COLOR);
		ANSI_CODE_COLORS.put("[1;42;40m", Globals.OK_COLOR);
		ANSI_CODE_COLORS.put("[0;33;40m", Globals.DARK_ORANGE);
		ANSI_CODE_COLORS.put("[1;33;40m", Globals.DARK_ORANGE);
		ANSI_CODE_COLORS.put("[0;43;40m", Globals.DARK_ORANGE);
		ANSI_CODE_COLORS.put("[1;43;40m", Globals.DARK_ORANGE);
		ANSI_CODE_COLORS.put("[0;34;40m", Globals.BLUE);
		ANSI_CODE_COLORS.put("[1;34;40m", Globals.BLUE);
		ANSI_CODE_COLORS.put("[0;44;40m", Globals.BLUE);
		ANSI_CODE_COLORS.put("[1;44;40m", Globals.BLUE);
		ANSI_CODE_COLORS.put("[0;35;40m", Color.MAGENTA);
		ANSI_CODE_COLORS.put("[1;35;40m", Color.MAGENTA);
		ANSI_CODE_COLORS.put("[0;45;40m", Color.MAGENTA);
		ANSI_CODE_COLORS.put("[1;45;40m", Color.MAGENTA);
		ANSI_CODE_COLORS.put("[0;36;40m", Color.CYAN);
		ANSI_CODE_COLORS.put("[1;36;40m", Color.CYAN);
		ANSI_CODE_COLORS.put("[0;46;40m", Color.CYAN);
		ANSI_CODE_COLORS.put("[1;46;40m", Color.CYAN);
		ANSI_CODE_COLORS.put("[0;37;40m", Globals.LIGHT_BLACK);
		ANSI_CODE_COLORS.put("[1;37;40m", Globals.LIGHT_BLACK);
		ANSI_CODE_COLORS.put("[0;47;40m", Globals.LIGHT_BLACK);
		ANSI_CODE_COLORS.put("[1;47;40m", Globals.LIGHT_BLACK);
	}

	protected JTextPane output;
	protected JScrollPane jScrollPane;

	protected JButton jButtonClose;
	protected boolean buildFrame;

	private JPanel mainPanel = new JPanel();
	protected JPanel inputPanel = new JPanel();

	protected GroupLayout konsolePanelLayout;
	protected GroupLayout mainPanelLayout;

	private Color linecolor;

	protected class DialogCloseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Logging.debug(this, "actionPerformed " + e);
			cancel();
		}
	}

	protected DialogCloseListener closeListener;

	public SSHConnectionOutputDialog(String title) {
		super(null, "", false);
		Logging.info(this.getClass(), "\ncreated a SSHConnectionOutputDialog with title " + title + "\n");
		buildFrame = false;
		linecolor = getAnsiForegroundColor();

		closeListener = new DialogCloseListener();
		initOutputGui();
		super.setSize(700, 400);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void append(String line) {
		append("", line);
	}

	private String findAnsiCodeColor(Entry<String, Color> entry, String key, String line) {
		if (line.trim().replace("\\t", "").replace(" ", "").startsWith(key)) {

			linecolor = entry.getValue() == null ? getAnsiForegroundColor() : entry.getValue();
			line = line.replace(key, "");
			Logging.debug(this, "append parseAnsiCodes found color key " + key + " value " + linecolor);

			line = line.replace(ANSI_ESCAPE_1, "");
		}
		return line;
	}

	private static Color getAnsiForegroundColor() {
		return UIManager.getColor("Label.foreground");
	}

	public void append(String caller, String line) {
		if (line == null) {
			Logging.error(this, "cannot append to string, since it is null");
			return;
		}

		if (SSHCommandFactory.hasColoredOutput() && !line.trim().replace("\\t", "").replace(" ", "").isEmpty()) {
			for (Entry<String, Color> entry : ANSI_CODE_COLORS.entrySet()) {
				line = findAnsiCodeColor(entry, entry.getKey(), line);
			}
		}

		Logging.debug(this, "line " + line.replace("\n", "") + " color " + linecolor.toString());
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, linecolor);
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		output.setCaretPosition(output.getDocument().getLength());
		output.setCharacterAttributes(aset, false);
		if (line.contains(ANSI_CODE_END_1) || line.contains(ANSI_CODE_END_2)) {
			line = line.replace(ANSI_CODE_END_1, "").replace(ANSI_CODE_END_2, "");
			linecolor = getAnsiForegroundColor();
		}

		try {
			StyledDocument doc = output.getStyledDocument();
			doc.insertString(doc.getLength(), caller + line, aset);
		} catch (BadLocationException e) {
			Logging.warning(this, "append, bad location exception occurred ", e);
		}
	}

	private void initOutputGui() {

		Dimension jButtonDimension = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3);
		if (!Main.THEMES) {
			inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			mainPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		mainPanelLayout = new GroupLayout(mainPanel);
		konsolePanelLayout = new GroupLayout(inputPanel);

		inputPanel.setLayout(konsolePanelLayout);
		mainPanel.setLayout(mainPanelLayout);

		jScrollPane = new JScrollPane();
		jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		output = new JTextPane();
		output.setEditable(false);

		// THEME color question
		if (!Main.THEMES) {
			output.setBackground(Globals.SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR);
		}
		output.setContentType("text/rtf");
		output.setPreferredSize(new Dimension(250, 200));
		StyledDocument doc = (StyledDocument) output.getDocument();
		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style readonlyStyle = doc.addStyle("readonlyStyle", defaultStyle);

		if (!Main.THEMES) {
			// Kein grün :(
			StyleConstants.setBackground(readonlyStyle, Globals.SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR);

			// Was ist rot?
			StyleConstants.setForeground(readonlyStyle, Globals.SSH_CONNECTION_OUTPUT_INIT_FOREGROUND_COLOR);
		}

		SimpleAttributeSet readOnlyAttributeSet = new SimpleAttributeSet(doc.getStyle("readonlyStyle"));
		readOnlyAttributeSet.addAttribute("readonly", true);
		((AbstractDocument) doc).setDocumentFilter(new InputFilter(output));

		DefaultCaret caret = (DefaultCaret) output.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		jScrollPane.setViewportView(output);
		output.setText("");

		jButtonClose = new IconButton(Configed.getResourceValue("buttonClose"), "images/cancel.png",
				"images/cancel.png", "images/cancel.png", true);

		jButtonClose.setPreferredSize(jButtonDimension);

		jButtonClose.addActionListener(closeListener);
	}

	public void setStatusFinish() {
		setVisible(true);
	}

	@Override
	public void setVisible(boolean b) {
		Logging.info(this, "outputDialog setVisible " + b);

		super.setVisible(b);
	}

	public void cancel() {
		buildFrame = false;
		Logging.debug(this, "cancel");
		super.doAction2();
	}

	private static class InputFilter extends DocumentFilter {
		JTextPane editor;

		public InputFilter(JTextPane editor) {
			this.editor = editor;
		}

		@Override
		public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException {
			if (!isReadOnly()) {
				super.remove(fb, offset, length);
			}
		}

		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, final String text,
				final AttributeSet attrs) throws BadLocationException {
			if (!isReadOnly()) {
				super.replace(fb, offset, length, text, attrs);
			}
		}

		private boolean isReadOnly() {
			AttributeSet attributeSet = editor.getCharacterAttributes();
			return attributeSet != null && attributeSet.getAttribute("readonly") != null;
		}
	}
}
