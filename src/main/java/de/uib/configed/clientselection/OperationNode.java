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
public record OperationNode(String element, String refinedElement, List<String> elementPath, String operation,
		String dataType, Object data, List<OperationNode> children) {
}