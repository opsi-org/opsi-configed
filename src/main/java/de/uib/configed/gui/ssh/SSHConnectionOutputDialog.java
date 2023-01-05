package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
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

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.utilities.logging.logging;

public class SSHConnectionOutputDialog extends FGeneralDialog/// *javax.swing.JDialog */ GeneralFrame
{
	protected JTextPane output;
	protected JScrollPane jScrollPane;

	protected JButton jButtonClose;
	protected boolean buildFrame = false;

	protected JPanel mainPanel = new JPanel();
	protected JPanel inputPanel = new JPanel();

	protected GroupLayout konsolePanelLayout;
	protected GroupLayout mainPanelLayout;

	private Color linecolor = Globals.SSH_CONNECTION_OUTPUT_DIALOG_START_LINE_COLOR;
	private static final String ANSI_ESCAPE_1 = "";
	private static final String ANSI_ESCAPE_2 = "\u001B";

	public static final String ANSI_CODE_END = "[0;0;0m";
	public static final String ANSI_CODE_END_1 = "\u001B[0;0;0m";
	public static final String ANSI_CODE_END_2 = "[0;0;0m";

	public static final String ANSI_CODE_INFO = "[0;info;0m"; // user info not really ansi code !!
	public static final String ANSI_CODE_ERROR = "[0;error;0m";

	private static final Map<String, Color> ansiCodeColors = Globals.SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS;

	protected class DialogCloseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			logging.debug(this, "actionPerformed " + e);
			cancel();
		}
	}

	DialogCloseListener closeListener;

	public SSHConnectionOutputDialog(String title) {
		super(null, "", false);
		logging.info(this, "\ncreated a SSHConnectionOutputDialog with title " + title + "\n");
		buildFrame = false;
		closeListener = new DialogCloseListener();
		initOutputGui();
		this.setSize(700, 400);
		this.setLocationRelativeTo(Globals.mainFrame);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void setStartAnsi(Color c) {
		linecolor = c;
	}

	public void append(String line) {
		append("", line);
	}

	private String findAnsiCodeColor(Entry<String, Color> entry, String key, String line) {
		if (line.trim().replace("\\t", "").replace(" ", "").startsWith(key)) {
			linecolor = entry.getValue();
			line = line.replace(key, "");
			logging.debug(this,
					"append parseAnsiCodes found color key " + key + " value " + entry.getValue().toString());

			line = line.replace(ANSI_ESCAPE_1, "").replace(ANSI_ESCAPE_2, "");
		}
		return line;
	}

	public void append(String caller, String line) {

		if (SSHCommandFactory.ssh_colored_output && (line != null)
				&& (!line.trim().replace("\\t", "").replace(" ", "").equals("")))
			for (Entry<String, Color> entry : ansiCodeColors.entrySet())
				line = findAnsiCodeColor(entry, entry.getKey(), line);

		logging.debug(this, "line " + line.replace("\n", "") + " color " + linecolor.toString());
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, linecolor);
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		output.setCaretPosition(output.getDocument().getLength());
		output.setCharacterAttributes(aset, false);
		if ((line.contains(ANSI_CODE_END)) || (line.contains(ANSI_CODE_END_1)) || (line.contains(ANSI_CODE_END_2))) {
			line = line.replace(ANSI_CODE_END, "").replace(ANSI_CODE_END_1, "").replace(ANSI_CODE_END_2, "");
			linecolor = Globals.SSH_CONNECTION_OUTPUT_DIALOG_DIFFERENT_LINE_COLOR;
		}
		try {
			StyledDocument doc = output.getStyledDocument();
			doc.insertString(doc.getLength(), caller + line, aset);
		} catch (Exception e) {
			logging.warning(this, "append, exception occurred ", e);
		}
	}

	private void initOutputGui() {
		try {
			Dimension jButtonDimension = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3);
			inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			mainPanel.setBackground(Globals.BACKGROUND_COLOR_7);
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
			output.setBackground(Globals.SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR);
			output.setContentType("text/rtf");
			output.setPreferredSize(new Dimension(250, 200));
			StyledDocument doc = (StyledDocument) output.getDocument();
			Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
			Style readonlyStyle = doc.addStyle("readonlyStyle", defaultStyle);

			StyleConstants.setBackground(readonlyStyle, Globals.SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR);// Kein grün :(

			StyleConstants.setForeground(readonlyStyle, Globals.SSH_CONNECTION_OUTPUT_INIT_FOREGROUND_COLOR); // Was ist rot?

			SimpleAttributeSet readOnlyAttributeSet = new SimpleAttributeSet(doc.getStyle("readonlyStyle"));
			readOnlyAttributeSet.addAttribute("readonly", true);
			((AbstractDocument) doc).setDocumentFilter(new InputFilter(output));

			DefaultCaret caret = (DefaultCaret) output.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			jScrollPane.setViewportView(output);
			output.setText("");

			jButtonClose = new de.uib.configed.gui.IconButton(configed.getResourceValue("SSHConnection.buttonClose"),
					"images/cancel.png", "images/cancel.png", "images/cancel.png", true);

			jButtonClose.setPreferredSize(jButtonDimension);

			jButtonClose.addActionListener(closeListener);

		} catch (Exception e) {
			logging.warning(this, "initOutputGui, exception occurred", e);
		}
	}

	private boolean showResult = true;

	public void setStatusFinish(String s) {
		if (showResult)
			setVisible(true);
		else
			cancel();
	}

	public void setStatusFinish() {

		if (showResult)
			setVisible(true);
		else
			cancel();

	}

	@Override
	public void setVisible(boolean b) {
		logging.info(this, "outputDialog setVisible " + b);

		super.setVisible(b);
	}

	public void cancel() {
		buildFrame = false;
		logging.debug(this, "cancel");
		super.doAction2();
	}

	class InputFilter extends DocumentFilter {
		JTextPane editor;

		public InputFilter(JTextPane editor) {
			this.editor = editor;
		}

		@Override
		public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException {
			if (!isReadOnly())
				super.remove(fb, offset, length);
		}

		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, final String text,
				final AttributeSet attrs) throws BadLocationException {
			if (!isReadOnly())
				super.replace(fb, offset, length, text, attrs);
		}

		private boolean isReadOnly() {
			AttributeSet attributeSet = editor.getCharacterAttributes();
			return attributeSet != null && attributeSet.getAttribute("readonly") != null;
		}
	}
}