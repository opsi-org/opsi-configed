/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
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
import de.uib.utilities.swing.FEdit;
import de.uib.utilities.swing.FEditText;

public class CellEditor4TableText extends DefaultCellEditor implements MouseListener, KeyListener, FocusListener {
	private String currentValue;
	private JTextField editorContent;
	private FEdit fEdit;
	private boolean fEditInitialized;
	private Dimension initSize;

	public CellEditor4TableText(FEdit fEdit, Dimension initSize) {
		super(new JTextField());
		editorContent = (JTextField) super.getComponent();

		if (fEdit == null) {
			this.fEdit = new FEditText("");
		} else {
			this.fEdit = fEdit;
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
		if (!fEditInitialized) {
			fEditInitialized = true;
		}

		return currentValue;
	}

	@Override
	public void focusLost(FocusEvent e) {
		/* Not needed */}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		String oldValue = (String) value;
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

		Point loc = null;
		Rectangle rec = table.getCellRect(row, column, true);
		try {
			loc = table.getLocationOnScreen();
		} catch (Exception ex) {
			Logging.warning(this, "get location error " + ex);
			loc = new Point(50, 50);
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
