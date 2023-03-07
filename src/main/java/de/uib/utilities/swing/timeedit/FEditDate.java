/*
 * FEditDate.java
 *
 */

package de.uib.utilities.swing.timeedit;

/**
 *
 * @author roeder
 */
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.ParseException;

import org.jdesktop.swingx.event.DateSelectionListener;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEdit;

public class FEditDate extends FEdit implements DateSelectionListener, MouseListener {

	public static final Dimension AREA_DIMENSION = new Dimension(380, 300);
	private DateTimeEditor dateEditor;

	protected DateFormat dateFormat;

	public FEditDate(String initialText, boolean withTime) {
		super(initialText);

		init(withTime);
	}

	private void init(boolean withTime) {
		areaDimension = AREA_DIMENSION;

		dateFormat = DateFormat.getDateInstance(Globals.DATE_FORMAT_STYLE_PATTERN);

		dateEditor = new DateTimeEditor(withTime);

		editingArea.add(dateEditor);

		dateEditor.setEditable(true);

		dateEditor.addDateSelectionListener(this);
		dateEditor.addKeyListener(this);
		dateEditor.addMonthViewMouseListener(this);

		setStartText(this.initialText);
	}

	@Override
	public void setStartText(String s) {
		super.setStartText(s);

		Logging.info(this, "setStartText(): " + s);
		setDataChanged(false);

		if (s == null || s.equals("")) {
			dateEditor.setDate(false);
		} else {
			String s1 = null;
			java.util.Date newDate = null;

			try {
				newDate = dateFormat.parse(s);
				dateEditor.setSelectionDate(newDate);
				setDataChanged(false);
			} catch (ParseException pex) {
				try {
					// fallback for standard sql time format
					s1 = s;
					if (s1.indexOf(' ') == -1) {
						s1 = s1 + " 00:00:00";
					}

					newDate = java.sql.Timestamp.valueOf(s1);
					Logging.info(this, "after supplement setStartText(): " + s1);
					dateEditor.setSelectionDate(newDate);

					setDataChanged(false);
				} catch (IllegalArgumentException ex) {
					Logging.warning("not valid date: " + s1);
					dateEditor.setDate(true);
					setDataChanged(true);
				}
			}
		}

	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			dateEditor.requestFocus();
		}
		// get focus in order to receive keyboard events
		super.setVisible(b);
		setSize(areaDimension);
		if (b) {
			setStartText(initialText);
		}
	}

	private String getSelectedDateTime() {
		Logging.debug(this, " getSelectedDateTime() : " + dateEditor.getSelectedSqlTime());

		if (dateEditor.getSelectedSqlTime() == null) {
			return "";
		}

		return dateEditor.getSelectedSqlTime().toString();
	}

	protected String getSelectedDateString() {
		// at the moment, the implementation decides about the date formatting

		return getSelectedDateTime();
	}

	@Override
	public String getText() {
		String oldText = initialText;
		Logging.info(this, "getText initialText was " + oldText);

		// set new initial text for use in processWindowEvent
		initialText = getSelectedDateString();
		Logging.info(this, "getText initialText changed to  " + initialText);
		return initialText;
	}

	// DateSelectionListener
	@Override
	public void valueChanged(org.jdesktop.swingx.event.DateSelectionEvent ev) {
		Logging.info(this, "valueChanged dateSelectionEvent");

		setDataChanged(true);

		updateCaller(getSelectedDateString());
	}

	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, " key event " + e);
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			commit();
		}
	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {

		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
			commit();
		}
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
