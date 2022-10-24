package de.uib.configed.gui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import de.uib.utilities.logging.*;

public class Autocomplete implements DocumentListener {

	private static enum Mode {
		INSERT,
		COMPLETION
	};

	private JTextField textField;
	private List<String> keywords;
	private Mode mode = Mode.INSERT;

	public Autocomplete(JTextField textField, List<String> keywords) 
	{
		this.textField = textField;
		this.keywords = keywords;
		Collections.sort(keywords);
	}
	public void setKeywordsList(List<String> l)
	{
		// keywords = new List<String>();
		this.keywords = l;
	}

	@Override
	public void changedUpdate(DocumentEvent ev) 
	{ 
		//logging.info(this,"changedUpdate " + ev);
	}

	@Override
	public void removeUpdate(DocumentEvent ev) 
	{ 
		//logging.info(this,"removeUpdate " + ev);
	}

	@Override
	public void insertUpdate(DocumentEvent ev) 
	{
		//logging.info(this,"insertUpdate " + ev);
		
		if (ev.getLength() != 1) return;

		int pos = ev.getOffset();
		String content = null;
		try 
		{
			content = textField.getText(0, pos + 1);
		} catch (BadLocationException e) 
		{
	  		logging.logTrace(e);
		}

		// Find where the word starts
		int w;
		for (w = pos; w >= 0; w--) 
		{
	  		// if (!Character.isLetter(content.charAt(w))) 
	  		if (content.charAt(w) == ' ')
	    			break;
		}

		// Too few chars
		if (pos - w < 1)
			return;

		String prefix = content.substring(w + 1).toLowerCase();
		int n = Collections.binarySearch(this.keywords, prefix);
		if (n < 0 && -n <= this.keywords.size()) 
		{
			String match = this.keywords.get(-n - 1);
			if (match.startsWith(prefix)) 
			{ // A completion is found
				String completion = match.substring(pos - w);
				// We cannot modify Document from within notification,
				// so we submit a task that does the change later
				SwingUtilities.invokeLater(new CompletionTask(completion, pos + 1));
			}
		} 
		else 
		{ // Nothing found
			mode = Mode.INSERT;
		}
	}

	public class CommitAction extends AbstractAction 
	{
		private static final long serialVersionUID = 5794543109646743416L;
		@Override
		public void actionPerformed(ActionEvent ev) 
		{
			if (mode == Mode.COMPLETION) 
			{
				int pos = textField.getSelectionEnd();
				StringBuffer sb = new StringBuffer(textField.getText());
				sb.insert(pos, " ");
				textField.setText(sb.toString());
				textField.setCaretPosition(pos + 1);
				mode = Mode.INSERT;
			} 
			else 
			{
				textField.replaceSelection("\t");
			}
		}
	}

	private class CompletionTask implements Runnable 
	{
		private String completion;
		private int position;

		CompletionTask(String completion, int position) 
		{
			this.completion = completion;
			this.position = position;
		}

		public void run() 
		{
			StringBuffer sb = new StringBuffer(textField.getText());
			sb.insert(position, completion);
			textField.setText(sb.toString());
			textField.setCaretPosition(position + completion.length());
			textField.moveCaretPosition(position);
			mode = Mode.COMPLETION;
		}
	}
}