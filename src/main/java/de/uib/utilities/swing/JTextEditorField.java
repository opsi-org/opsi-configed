package de.uib.utilities.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.text.Document;

import de.uib.utilities.logging.Logging;

public class JTextEditorField extends JTextField implements KeyListener {
	String lastSetS = null;

	public JTextEditorField(String s) {
		super(s);
		super.addKeyListener(this);
	}

	public JTextEditorField(Document doc, String text, int columns) {
		super(doc, text, columns);
		super.addKeyListener(this);
	}

	@Override
	public void setText(String s) {
		Logging.debug(this, "setText " + s);
		if (s == null) {
			lastSetS = "";
		} else {
			lastSetS = s;
		}

		super.setText(s);
	}

	public boolean isChangedText() {

		if (lastSetS == null && getText() == null) {
			return false;
		}

		if (lastSetS == null && getText() != null) {
			return true;
		}

		return !lastSetS.equals(getText());
	}

	// KeyListener

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setText(lastSetS);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}
}
