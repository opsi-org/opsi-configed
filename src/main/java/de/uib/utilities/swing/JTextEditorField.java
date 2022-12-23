package de.uib.utilities.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.Document;

import de.uib.utilities.logging.logging;

public class JTextEditorField extends javax.swing.JTextField implements KeyListener {
	String lastSetS = null;

	public JTextEditorField(String s) {
		super(s);
		addKeyListener(this);
	}

	public JTextEditorField(Document doc, String text, int columns) {
		super(doc, text, columns);
		addKeyListener(this);
	}

	@Override
	public void setText(String s) {
		logging.debug(this, "setText " + s);
		if (s == null)
			lastSetS = "";
		else
			lastSetS = s;
		super.setText(s);
	}

	public boolean isChangedText() {
		// logging.info(this, "isChangedText: lastSetS " + lastSetS);
		// logging.info(this, "isChangedText: getText " + getText());

		if (lastSetS == null && getText() == null)
			return false;

		if (lastSetS == null && getText() != null)
			return true;

		// logging.info(this, "isChangedText: not equal, equal? " + lastSetS.equals(
		

		return !lastSetS.equals(getText());
	}

	// KeyListener
	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// logging.debug(this, "keyPressed code " + e.getKeyCode() + " char " +
		// e.getKeyChar());
		// logging.debug(this, "keyPressed KeyEvent.VK_ESCAPE " + KeyEvent.VK_ESCAPE);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setText(lastSetS);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
