/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a node in the operation tree for client selection.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationNode {
	private String element;
	private String refinedElement;
	private List<String> elementPath;
	private String operation;
	private String dataType;
	private Object data;
	private List<OperationNode> children;

	public OperationNode() {
	}

	public OperationNode(String element, String refinedElement, List<String> elementPath, String operation,
			String dataType, Object data, List<OperationNode> children) {
		this.element = element;
		this.refinedElement = refinedElement;
		this.elementPath = elementPath;
		this.operation = operation;
		this.dataType = dataType;
		this.data = data;
		this.children = children;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getRefinedElement() {
		return refinedElement;
	}

	public void setRefinedElement(String refinedElement) {
		this.refinedElement = refinedElement;
	}

	public List<String> getElementPath() {
		return elementPath;
	}

	public void setElementPath(List<String> elementPath) {
		this.elementPath = elementPath;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public List<OperationNode> getChildren() {
		return children;
	}

	public void setChildren(List<OperationNode> children) {
		this.children = children;
	}
}