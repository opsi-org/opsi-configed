/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.messageoftheday;

import java.time.LocalDateTime;

public interface IDateTimePickerCaller {
	void dataChanged(LocalDateTime date);
}
