/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

public class OperationWithStatus {
	private AbstractSelectOperation operation;
	private ConnectionStatus status;
	private boolean parenthesisOpen;
	private boolean parenthesisClose;

	public AbstractSelectOperation getOperation() {
		return operation;
	}

	public void setOperation(AbstractSelectOperation operation) {
		this.operation = operation;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}

	public boolean isParenthesisOpen() {
		return parenthesisOpen;
	}

	public void setParenthesisOpen(boolean open) {
		this.parenthesisOpen = open;
	}

	public boolean isParenthesisClosed() {
		return parenthesisClose;
	}

	public void setParenthesisClose(boolean close) {
		this.parenthesisClose = close;
	}
}
