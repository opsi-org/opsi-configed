/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import de.uib.configed.gui.logpane.LogPane.ImmutableDefaultStyledDocument;
import de.uib.configed.gui.logpane.PartialDocumentBuilder.DocumentBuilderResult;
import de.uib.utilities.logging.Logging;

public class PartialDocumentBuilder extends SwingWorker<DocumentBuilderResult, Void> {
	private int linesToRead;
	private int lineNumberToSearch;
	private int lineStartPosition;
	private boolean lineFound;
	private ImmutableDefaultStyledDocument document2;
	private boolean showTypeRestricted;
	private int selTypeIndex;
	private int initialLines;
	private int lineIndex;
	private int logLevel;
	private Consumer<DocumentBuilderResult> callBack;
	private LogFileParser parser;
	private TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	private TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	@SuppressWarnings({ "java:S107" })
	public PartialDocumentBuilder(ImmutableDefaultStyledDocument document, boolean showTypeRestricted, int selTyepIndex,
			int logLevel, LogFileParser parser, int linesToRead, int lineNumberToSearch, int lineIndex) {
		this.document2 = document;
		this.showTypeRestricted = showTypeRestricted;
		this.selTypeIndex = selTyepIndex;
		this.linesToRead = linesToRead;
		this.initialLines = linesToRead;
		this.lineNumberToSearch = lineNumberToSearch;
		this.logLevel = logLevel;
		this.lineIndex = lineIndex;
		this.parser = parser;
	}

	public void setOnDocumentBuilt(Consumer<DocumentBuilderResult> callBack) {
		this.callBack = callBack;
	}

	@Override
	protected DocumentBuilderResult doInBackground() throws Exception {
		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();
		if (lineNumberToSearch == 0) {
			readChunkOfFile();
		} else {
			while (!lineFound) {
				readChunkOfFile();
				findLineStartPosition();
				linesToRead += initialLines;
			}
		}
		return new DocumentBuilderResult(document2, lineStartPosition, lineIndex, docLinestartPosition2lineCount);
	}

	private void readChunkOfFile() {
		try {
			int lineNumber = lineIndex;
			int linesRead = 0;
			while (lineNumber < parser.getParsedLogLines().size() && linesRead <= linesToRead) {
				LogLine line = parser.getParsedLogLine(lineNumber);
				if (showLine(line)) {
					docLinestartPosition2lineCount.put(document2.getLength(), lineNumber);
					lineCount2docLinestartPosition.put(lineNumber, document2.getLength());

					String lineNumberRepresentation = "(" + line.getLineNumber() + ")";
					document2.insertStringTruely(document2.getLength(),
							String.format("%-10s", lineNumberRepresentation) + line.getText() + '\n', line.getStyle());
					linesRead++;
				}

				lineNumber++;
				lineIndex = lineNumber;
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "BadLocationException thrown in logging: " + e);
		}
	}

	private void findLineStartPosition() {
		if (lineCount2docLinestartPosition.containsKey(lineNumberToSearch)) {
			lineStartPosition = lineCount2docLinestartPosition.get(lineNumberToSearch);
			lineFound = true;
		} else if (!lineCount2docLinestartPosition.isEmpty() && lineIndex == parser.getParsedLogLines().size()) {
			Iterator<Integer> linesIterator = lineCount2docLinestartPosition.keySet().iterator();
			int nextLineNo = linesIterator.next();

			while (linesIterator.hasNext() && nextLineNo < lineNumberToSearch) {
				nextLineNo = linesIterator.next();
			}

			lineStartPosition = lineCount2docLinestartPosition.get(nextLineNo);
			lineFound = true;
		} else {
			Logging.notice(this, "lineCount2docLinestartPosition is empty, so there will be no lines");
			lineFound = false;
		}
	}

	private boolean showLine(LogLine line) {
		boolean show = false;
		if (line.getLogLevel() <= logLevel) {
			show = true;
		}
		if (show && showTypeRestricted && line.getTypeIndex() != selTypeIndex) {
			show = false;
		}
		return show;
	}

	@Override
	public void done() {
		try {
			if (callBack != null) {
				callBack.accept(get());
			}
		} catch (ExecutionException e) {
			Logging.warning(this, "Exception encountered while building document: " + e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@SuppressWarnings({ "java:S2972", "java:S1319" })
	protected static class DocumentBuilderResult {
		private int lineStartPosition;
		private int lineIndex;
		private ImmutableDefaultStyledDocument document;
		private TreeMap<Integer, Integer> docLinestartPosition2lineCount;

		public DocumentBuilderResult(ImmutableDefaultStyledDocument document, int lineStartPosition, int lineIndex,
				TreeMap<Integer, Integer> docLinestartPosition2lineCount) {
			this.document = document;
			this.lineStartPosition = lineStartPosition;
			this.lineIndex = lineIndex;
			this.docLinestartPosition2lineCount = docLinestartPosition2lineCount;
		}

		public ImmutableDefaultStyledDocument getDocument() {
			return document;
		}

		public int getLineStartPosition() {
			return lineStartPosition;
		}

		public int getLineIndex() {
			return lineIndex;
		}

		public Map<Integer, Integer> getDocLinestartPosition2lineCount() {
			return docLinestartPosition2lineCount;
		}
	}
}
