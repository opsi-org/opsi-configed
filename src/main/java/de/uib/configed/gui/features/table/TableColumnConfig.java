/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.Comparator;

import javax.swing.table.TableCellRenderer;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Configuration for a single column. This replaces the imperative logic in
 * initRenderer().
 */
@Value
@With
@Builder
public class TableColumnConfig {
	private final String key;
	private final String header;
	private final boolean editable;
	@Builder.Default
	private final boolean visible = true;
	private final int prefferedWidth;

	private transient TableCellRenderer renderer;
	private transient Comparator<Object> comparator;
}
