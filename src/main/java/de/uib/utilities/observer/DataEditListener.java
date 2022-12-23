// title: DataEditListener
// description:
// class for interaction with an Observable that reacts to changes in a document
// implements DocumentListener
// uib 2009

package de.uib.utilities.observer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.utilities.logging.logging;

public class DataEditListener implements DocumentListener, // for text components
		ItemListener, // for combo boxes

		KeyListener

{
	public static final String commitRequest = "COMMIT";
	public static final String cancelRequest = "CANCEL";
	protected Object source;
	protected ObservableSubject dataChangedSubject;
	protected boolean withFocusCheck = true;

	protected void act() {
		if (dataChangedSubject == null) {
			logging.info(this, "dataChangedSubject null");
			return;
		}
		
		if (!withFocusCheck || (source instanceof JComponent && ((JComponent) source).hasFocus())) {
			
			dataChangedSubject.setChanged();
			dataChangedSubject.notifyObservers();
		}
	}

	protected void requestAction(String action) {
		if (dataChangedSubject == null) {
			logging.info(this, "dataChangedSubject null");
			return;
		}

		if (!withFocusCheck || (source instanceof JComponent && ((JComponent) source).hasFocus())) {
			
			if (!(action.equals(cancelRequest) || action.equals(commitRequest))) {
				dataChangedSubject.setChanged();
				dataChangedSubject.notifyObservers(action);
			}
		}
	}

	public DataEditListener(ObservableSubject subject, Object source, boolean withFocusCheck) {
		logging.info(this, "constructed , subject  " + subject + ", source " + source);
		this.source = source;
		this.withFocusCheck = withFocusCheck;
		dataChangedSubject = subject;
	}

	public DataEditListener(ObservableSubject subject, Object source) {
		this(subject, source, true);
	}

	public void setWithFocusCheck(boolean b) {
		withFocusCheck = b;
	}

	public boolean isWithFocusCheck() {
		return withFocusCheck;
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		
		act();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		
		act();
		
		// dataChangedSubject.hasChanged());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		
		act();
	}

	// ItemListener interface
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		act();
	}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		

		if (e.getKeyCode() == 10)// KeyEvent.VK_ENTER)
			requestAction(commitRequest);

		else if (e.getKeyCode() == 27)// KeyEvent.VK_ESCAPE)
			requestAction(cancelRequest);

		else if (!e.isActionKey())
			act();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
