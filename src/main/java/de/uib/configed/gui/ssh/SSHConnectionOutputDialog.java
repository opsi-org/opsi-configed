package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
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
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.utilities.logging.logging;

public class SSHConnectionOutputDialog extends FGeneralDialog/// *javax.swing.JDialog */ GeneralFrame
{
	protected JTextPane output;
	protected JScrollPane jScrollPane;

	// private JCheckBox cb_showResult;
	// private JButton btn_inBackground;
	protected JButton btn_close;
	protected boolean buildFrame = false;

	protected JPanel mainPanel = new JPanel();
	protected JPanel inputPanel = new JPanel();

	protected GroupLayout konsolePanelLayout;
	protected GroupLayout mainPanelLayout;

	private Color linecolor = Globals.lightBlack;
	private final String ansi_escape1 = "";
	private final String ansi_escape2 = "\u001B";

	public final String ansiCodeEnd = "[0;0;0m";
	public final String ansiCodeEnd1 = "\u001B[0;0;0m";
	public final String ansiCodeEnd2 = "[0;0;0m";

	public final String ansiCodeInfo = "[0;info;0m"; // user info not really ansi code !!
	public final String ansiCodeError = "[0;error;0m"; // user info "error" not really ansi code !!

	private final Map<String, Color> ansiCodeColors = new HashMap<>() {
		{
			put("[0;info;0m", Globals.greyed); // user info not really ansi code !!
			put("[0;error;0m", Globals.actionRed); // user info "error" not really ansi code !!
			put("[0;30;40m", Color.BLACK);
			// ansis beginning with "[1": lines should be unterlined - are not !
			put("[1;30;40m", Color.BLACK);
			put("[0;40;40m", Color.BLACK);
			put("[1;40;40m", Color.BLACK);
			put("[0;31;40m", Globals.actionRed);
			put("[1;31;40m", Globals.actionRed);
			put("[0;41;40m", Globals.actionRed);
			put("[1;41;40m", Globals.actionRed);
			put("[0;32;40m", Globals.okGreen);
			put("[1;32;40m", Globals.okGreen);
			put("[0;42;40m", Globals.okGreen);
			put("[1;42;40m", Globals.okGreen);
			put("[0;33;40m", Globals.darkOrange);
			put("[1;33;40m", Globals.darkOrange);
			put("[0;43;40m", Globals.darkOrange);
			put("[1;43;40m", Globals.darkOrange);
			put("[0;34;40m", Globals.blue);
			put("[1;34;40m", Globals.blue);
			put("[0;44;40m", Globals.blue);
			put("[1;44;40m", Globals.blue);
			put("[0;35;40m", Color.MAGENTA);
			put("[1;35;40m", Color.MAGENTA);
			put("[0;45;40m", Color.MAGENTA);
			put("[1;45;40m", Color.MAGENTA);
			put("[0;36;40m", Color.CYAN);
			put("[1;36;40m", Color.CYAN);
			put("[0;46;40m", Color.CYAN);
			put("[1;46;40m", Color.CYAN);
			put("[0;37;40m", Globals.lightBlack);
			put("[1;37;40m", Globals.lightBlack);
			put("[0;47;40m", Globals.lightBlack);
			put("[1;47;40m", Globals.lightBlack);
		}
	};

	protected class DialogCloseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			logging.debug(this, "actionPerformed " + e);
			cancel();
			// JOptionPane.showMessageDialog(Globals.mainFrame, "we got
			// cancel");
		}
	};

	DialogCloseListener closeListener;

	// protected JDialog parentDialog;
	// private static SSHConnectionOutputDialog instance;
	public SSHConnectionOutputDialog(String title) {
		super(null, "", false);
		logging.info(this, "\ncreated a SSHConnectionOutputDialog with title " + title + "\n");
		buildFrame = false;
		closeListener = new DialogCloseListener();
		initOutputGui();
		this.setSize(700, 400);
		this.centerOn(Globals.mainFrame);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void setStartAnsi(Color c) {
		linecolor = c;
	}

	public void append(String line, Component focusedComponent) {
		append("", line);
	}

	private String findAnsiCodeColor(Map.Entry entry, String key, String line) {
		if (line.trim().replaceAll("\\t", "").replaceAll(" ", "").startsWith(key)) {
			linecolor = (Color) entry.getValue();
			line = line.replace(key, "");
			logging.debug(this,
					"append parseAnsiCodes found color key " + key + " value " + ((Color) entry.getValue()).toString());

			// if ( (line.trim().replaceAll("\\t","").replaceAll(" ","").charAt(0) ==
			// ansi_escape1.toCharArray()[0])
			// || (line.trim().replaceAll("\\t","").replaceAll(" ","").charAt(0) ==
			// ansi_escape2.toCharArray()[0]) )
			// line = line.replace(ansi_escape2, "");
			line = line.replace(ansi_escape1, "").replace(ansi_escape2, "");
		}
		return line;
	}

	public void append(String line) {
		append("", line);
	}

	public void append(String caller, String line) {
		// if ((line == null) || (line.trim().length() <=0)) return;
		// Color linecolor = Color.BLACK;
		if (SSHCommandFactory.ssh_colored_output) {
			if ((line != null) && (!line.trim().replaceAll("\\t", "").replaceAll(" ", "").equals("")))
				for (Map.Entry entry : ansiCodeColors.entrySet())
					line = findAnsiCodeColor(entry, (String) entry.getKey(), line);

		}
		logging.debug(this, "line " + line.replace("\n", "") + " color " + linecolor.toString());
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, linecolor);
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		output.setCaretPosition(output.getDocument().getLength());
		output.setCharacterAttributes(aset, false);
		if ((line.contains(ansiCodeEnd)) || (line.contains(ansiCodeEnd1)) || (line.contains(ansiCodeEnd2))) {
			line = line.replace(ansiCodeEnd, "").replace(ansiCodeEnd1, "").replace(ansiCodeEnd2, "");
			linecolor = Color.BLACK;
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
			Dimension btn_dim = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3);
			inputPanel.setBackground(Globals.backLightBlue);
			mainPanel.setBackground(Globals.backLightBlue);
			getContentPane().add(mainPanel, BorderLayout.CENTER);

			mainPanelLayout = new GroupLayout((JComponent) mainPanel);
			konsolePanelLayout = new GroupLayout((JComponent) inputPanel);

			inputPanel.setLayout(konsolePanelLayout);
			mainPanel.setLayout(mainPanelLayout);

			jScrollPane = new JScrollPane();
			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			output = new JTextPane();
			output.setEditable(false);
			output.setBackground(Color.GREEN);
			output.setContentType("text/rtf");
			output.setPreferredSize(new Dimension(250, 200));
			StyledDocument doc = (StyledDocument) output.getDocument();
			Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
			Style readonlyStyle = doc.addStyle("readonlyStyle", defaultStyle);

			StyleConstants.setBackground(readonlyStyle, Color.GREEN);// Kein grün :(

			// StyleConstants.setBackground(style, Color.blue);
			StyleConstants.setForeground(readonlyStyle, Color.RED); // Was ist rot?

			SimpleAttributeSet readOnlyAttributeSet = new SimpleAttributeSet(doc.getStyle("readonlyStyle"));
			readOnlyAttributeSet.addAttribute("readonly", true);
			((AbstractDocument) doc).setDocumentFilter(new InputFilter(output));

			DefaultCaret caret = (DefaultCaret) output.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			jScrollPane.setViewportView(output);
			output.setText("");

			// btn_close = new JButton();
			// // buttonPanel.add(btn_close);
			// btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.buttonClose"), "images/cancel.png",
					"images/cancel.png", "images/cancel.png", true);
			// btn_test_command.setSize(new Dimension( Globals.graphicButtonWidth + 15
			// ,Globals.lineHeight));
			// btn_test_command.setSize(new Dimension( Globals.graphicButtonWidth + 15
			// ,Globals.lineHeight));
			btn_close.setPreferredSize(btn_dim);

			btn_close.addActionListener(closeListener);

			// lbl_userhost = new JLabel();
			// lbl_userhost.setText("user@host");

			// createLayout(konsolePanelLayout, jScrollPane,Globals.gapSize,
			// Globals.gapSize, false);
			// createLayout(mainPanelLayout, inputPanel,0,0, false);
		} catch (Exception e) {
			logging.warning(this, "initOutputGui, exception occurred", e);
		}
	}

	public boolean showResult = true;

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