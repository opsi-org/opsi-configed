/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * DynamicCombo.java
 *
 * Created on 14.04.2009, 10:36:25
 */

package de.uib.utilities.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class AutoCompletionComboBox<T> extends JComboBox<T> {
	public AutoCompletionComboBox() {
		initComponents();
	}

	private void initComponents() {
		setBorder(null);
		JTextComponent editor = (JTextComponent) getEditor().getEditorComponent();
		editor.setDocument(new AutoCompletionDocument());
		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (isDisplayable()) {
					setPopupVisible(true);
				}
			}
		});
	}

	@SuppressWarnings({ "java:S2972" })
	public class AutoCompletionDocument extends PlainDocument {
		private boolean selecting;

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (selecting) {
				return;
			}
			super.insertString(offs, str, a);
			String content = ((JTextField) getEditor().getEditorComponent()).getText(0, getLength());
			Object item = lookupItem(content);
			if (item != null) {
				setSelectedItem(item);
				setText(item.toString());
				highlightCompletedText(offs + str.length());
			}
		}

		private void setText(String text) throws BadLocationException {
			super.remove(0, getLength());
			super.insertString(0, text, null);
		}

		private void setSelectedItem(Object item) {
			selecting = true;
			getModel().setSelectedItem(item);
			selecting = false;
		}

		private void highlightCompletedText(int start) {
			((JTextComponent) getEditor().getEditorComponent()).setSelectionStart(start);
			((JTextComponent) getEditor().getEditorComponent()).setSelectionEnd(getLength());
		}

		private Object lookupItem(String pattern) {
			ComboBoxModel<T> model = getModel();
			for (int i = 0, n = model.getSize(); i < n; i++) {
				Object currentItem = model.getElementAt(i);
				if (currentItem.toString().startsWith(pattern)) {
					return currentItem;
				}
			}
			return null;
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {
			if (selecting) {
				return;
			}
			super.remove(offs, len);
		}
	}
}
