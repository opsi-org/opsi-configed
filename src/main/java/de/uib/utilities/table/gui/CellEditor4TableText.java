package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class CellEditor4TableText extends DefaultCellEditor implements MouseListener, KeyListener, FocusListener

{
	String oldValue;
	String currentValue;
	JTextField editorContent;
	de.uib.utilities.swing.FEdit fEdit;
	boolean globalFEdit = false;
	boolean fEditInitialized = false;
	Dimension initSize;

	public CellEditor4TableText(de.uib.utilities.swing.FEdit fEdit, Dimension initSize) {
		super(new JTextField());
		editorContent = (JTextField) super.getComponent();

		if (fEdit == null) {
			this.fEdit = new de.uib.utilities.swing.FEditText("");
		} else {
			this.fEdit = fEdit;
			globalFEdit = true;
		}

		fEditInitialized = false;

		this.initSize = initSize;

		editorContent.setEditable(false);

		editorContent.addMouseListener(this);
		editorContent.addKeyListener(this);
		editorContent.addFocusListener(this);
		editorContent.setBorder(null);
	}

	public CellEditor4TableText() {
		this(null, null);
	}

	// interface
	// ActionListener
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == editorContent) {

			fireEditingStopped();
		}

	}

	// interface
	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == editorContent && (e.getClickCount() > 1 || e.getButton() != MouseEvent.BUTTON1)) {

			fEdit.setVisible(true);
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

	// interface
	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == editorContent && e.getKeyCode() == 32) {
			fEdit.setVisible(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	// interface
	// FocusListener
	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource() == editorContent) {

			editorContent.setText(fEdit.getText());
		}
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	@Override
	public Object getCellEditorValue() {
		currentValue = fEdit.getText();
		fEdit.setVisible(false);
		if (!fEditInitialized)
			fEditInitialized = true;

		return currentValue;
	}

	@Override
	public void focusLost(FocusEvent e) {
		/* Not needed */}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		oldValue = (String) value;
		fEdit.setCaller(editorContent);
		fEdit.setStartText(oldValue);

		if (!fEditInitialized) {
			if (initSize != null) {
				fEditInitialized = fEdit.init(initSize);
			} else {
				fEditInitialized = fEdit.init(new Dimension(table.getCellRect(row, column, true).width + 60,
						table.getCellRect(row, column, true).height + 30));
			}
		}

		java.awt.Point loc = null;
		java.awt.Rectangle rec = table.getCellRect(row, column, true);
		try {
			loc = table.getLocationOnScreen();
		} catch (Exception ex) {
			Logging.warning(this, "get location error " + ex);
			loc = new java.awt.Point(50, 50);
		}

		fEdit.setVisible(true);

		fEdit.setLocation((int) (loc.getX() + rec.getX() + 30), (int) (loc.getY() + rec.getY() + 20));

		fEdit.setTitle(" (" + Globals.APPNAME + ")  '" + table.getColumnName(column) + "'");

		currentValue = oldValue;
		return editorContent;
	}

	@Override
	public boolean stopCellEditing() {
		Logging.debug(this, "stopCellEditing");

		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing() {
		Logging.debug(this, "cancelCellEditing");
		super.cancelCellEditing();
	}

}
