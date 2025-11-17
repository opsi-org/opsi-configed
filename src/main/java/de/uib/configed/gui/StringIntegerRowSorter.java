/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Comparator;
import java.util.Set;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.share.ExtendedInteger;
import de.uib.configed.share.logging.Logging;

public class StringIntegerRowSorter extends TableRowSorter<TableModel> {
	private Set<Integer> integerColumns;

	public StringIntegerRowSorter(TableModel model, Set<Integer> integerColumns) {
		super(model);
		this.integerColumns = integerColumns;
	}

	@Override
	public Comparator<?> getComparator(int column) {
		Logging.info(this, "getComparator for column ", column, " with integerColumns ", integerColumns);
		if (integerColumns != null && integerColumns.contains(column)) {
			return Comparator.comparingInt((Object o) -> {
				String value = o.toString();
				if (ExtendedInteger.DISPLAY_INFINITE.equals(value)) {
					return Integer.MAX_VALUE;
				} else {
					return Integer.parseInt(value);
				}
			});
		} else {
			return super.getComparator(column);
		}
	}
}
