/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import javax.swing.ComboBoxModel;

/**
 * Any implementation of this interface gives a ComboBoxModel for each pair
 * (row, column)
 */
public interface ComboBoxModeller {
	/**
	 * Producing a Combo
	 */
	ComboBoxModel<String> getComboBoxModel(int row, int column);
}
