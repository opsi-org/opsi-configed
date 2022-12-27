/*
 * FEditText.java
 *
 * (c) uib 2009-2010,2021
 *
 */

package de.uib.utilities.swing;

/**
 *
 * @author roeder
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.utilities.logging.logging;

public class FEditText extends FEdit implements DocumentListener, MouseListener {
	protected javax.swing.JScrollPane scrollpane;
	protected javax.swing.JTextArea textarea;

	protected boolean singleLine;

	protected boolean standalone = true;
	static int count = 0;

	public FEditText(String initialText, String hint) {
		super(initialText, hint);
		initFEditText();
		setSingleLine(false);
	}

	public FEditText(String initialText) {
		super(initialText);
		initFEditText();
	}

	protected void initFEditText() {
		scrollpane = new javax.swing.JScrollPane();
		textarea = new javax.swing.JTextArea();
		scrollpane.setViewportView(textarea);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		textarea.setEditable(true);
		textarea.addKeyListener(this);
		textarea.addMouseListener(this);
		textarea.getDocument().addDocumentListener(this);
		setStartText(this.initialText);
	}

	public void setSingleLine(boolean b) {
		singleLine = b;
		textarea.setLineWrap(!singleLine);
		textarea.setWrapStyleWord(!singleLine);
	}

	@Override
	public void setStartText(String s) {
		super.setStartText(s);
		textarea.setText(s);
	}

	@Override
	public String getText() {
		textarea.setText(textarea.getText().replace("\t", ""));
		if (singleLine)
			textarea.setText(textarea.getText().replace("\n", ""));
		initialText = textarea.getText(); // set new initial text for use in processWindowEvent
		return initialText;
	}

	public void select(int selectionStart, int selectionEnd) {
		textarea.select(selectionStart, selectionEnd);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textarea) {

			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
					&& e.getKeyCode() == KeyEvent.VK_TAB)
				buttonCommit.requestFocusInWindow();

			else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && singleLine)
				commit();
		}

		super.keyPressed(e);
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {

		setDataChanged(true);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {

		setDataChanged(true);
	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {

		if (standalone)
			logging.debug(getText());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void removeUpdate(DocumentEvent e) {

		setDataChanged(true);
	}

	public static void main(String[] args) {
		logging.debug(" invoking " + FEditText.class);
		SwingUtilities.invokeLater(() -> {
			FEditText f = new FEditText(args[0], "");
			f.init(new Dimension(300, 200));
			f.setVisible(true);
			f.setStartText(args[0]);
			count++;
			logging.debug("having " + count + " " + f.getText());
		});

	}

}
