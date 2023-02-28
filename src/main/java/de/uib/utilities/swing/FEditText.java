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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.utilities.logging.Logging;

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

	private void initFEditText() {
		scrollpane = new JScrollPane();
		textarea = new javax.swing.JTextArea();
		scrollpane.setViewportView(textarea);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		textarea.setEditable(true);
		textarea.addKeyListener(this);
		textarea.addMouseListener(this);
		textarea.getDocument().addDocumentListener(this);
		setStartText(this.initialText);
	}

	public final void setSingleLine(boolean b) {
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
		if (singleLine) {
			textarea.setText(textarea.getText().replace("\n", ""));
		}

		// set new initial text for use in processWindowEvent
		initialText = textarea.getText();
		return initialText;
	}

	public void select(int selectionStart, int selectionEnd) {
		textarea.select(selectionStart, selectionEnd);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textarea) {

			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
					&& e.getKeyCode() == KeyEvent.VK_TAB) {
				buttonCommit.requestFocusInWindow();
			}

			else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && singleLine) {
				commit();
			}
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

		if (standalone) {
			Logging.debug(getText());
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}
}
