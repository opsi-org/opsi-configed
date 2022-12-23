package de.uib.utilities.observer.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import de.uib.utilities.logging.logging;
import de.uib.utilities.observer.DataEditListener;
import de.uib.utilities.observer.ObservableSubject;

public class JTextFieldObserved extends JTextField implements KeyListener {
	protected String startText = "";

	protected ObservableSubject globalEditingSubject;

	public JTextFieldObserved() {
		this(null);
	}

	public JTextFieldObserved(ObservableSubject globalEditingSubject) {
		this("", globalEditingSubject);
	}

	public JTextFieldObserved(String s, ObservableSubject globalEditingSubject) {
		super(s);
		addKeyListener(this);
		setGlobalObservableSubject(globalEditingSubject);
	}

	public void setGlobalObservableSubject(ObservableSubject globalEditingSubject) {
		logging.debug(this, "setGlobalObservableSubject " + globalEditingSubject);
		this.globalEditingSubject = globalEditingSubject;
		addKeyListener(new DataEditListener(globalEditingSubject, this));
		getDocument().addDocumentListener(new DataEditListener(globalEditingSubject, this));
	}

	@Override
	public void setText(String s) {

		super.setText(s);
		startText = s;
		setCaretPosition(0);

	}

	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

			setText(startText);
			setCaretPosition(startText.length());
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			transferFocus();
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
