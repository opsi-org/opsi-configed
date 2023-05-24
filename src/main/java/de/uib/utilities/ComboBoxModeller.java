/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

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
