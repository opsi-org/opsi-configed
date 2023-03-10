package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import de.uib.utilities.logging.Logging;

public class Autocomplete implements DocumentListener {

	private enum Mode {
		INSERT, COMPLETION
	}

	private JTextField textField;
	private List<String> keywords;
	private Mode mode = Mode.INSERT;

	public Autocomplete(JTextField textField, List<String> keywords) {
		this.textField = textField;
		this.keywords = keywords;
		Collections.sort(keywords);
	}

	public void setKeywordsList(List<String> l) {

		this.keywords = l;
	}

	@Override
	public void changedUpdate(DocumentEvent ev) {
		/* Not needed */}

	@Override
	public void removeUpdate(DocumentEvent ev) {
		/* Not needed */}

	@Override
	public void insertUpdate(DocumentEvent ev) {

		if (ev.getLength() != 1) {
			return;
		}

		int pos = ev.getOffset();
		String content = null;
		try {
			content = textField.getText(0, pos + 1);
		} catch (BadLocationException e) {
			Logging.error("Failed to get text", e);
			return;
		}

		// Find where the word starts
		int w;
		for (w = pos; w >= 0; w--) {

			if (content.charAt(w) == ' ') {
				break;
			}
		}

		// Too few chars
		if (pos - w < 1) {
			return;
		}

		String prefix = content.substring(w + 1).toLowerCase();
		int n = Collections.binarySearch(this.keywords, prefix);
		if (n < 0 && -n <= this.keywords.size()) {
			String match = this.keywords.get(-n - 1);

			// A completion is found
			if (match.startsWith(prefix)) {
				String completion = match.substring(pos - w);
				// We cannot modify Document from within notification,
				// so we submit a task that does the change later
				SwingUtilities.invokeLater(new CompletionTask(completion, pos + 1));
			}
		} else {
			// Nothing found
			mode = Mode.INSERT;
		}
	}

	public class CommitAction extends AbstractAction {
		private static final long serialVersionUID = 5794543109646743416L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			if (mode == Mode.COMPLETION) {
				int pos = textField.getSelectionEnd();
				StringBuilder sb = new StringBuilder(textField.getText());
				sb.insert(pos, " ");
				textField.setText(sb.toString());
				textField.setCaretPosition(pos + 1);
				mode = Mode.INSERT;
			} else {
				textField.replaceSelection("\t");
			}
		}
	}

	private class CompletionTask implements Runnable {
		private String completion;
		private int position;

		CompletionTask(String completion, int position) {
			this.completion = completion;
			this.position = position;
		}

		@Override
		public void run() {
			StringBuilder sb = new StringBuilder(textField.getText());
			sb.insert(position, completion);
			textField.setText(sb.toString());
			textField.setCaretPosition(position + completion.length());
			textField.moveCaretPosition(position);
			mode = Mode.COMPLETION;
		}
	}
}