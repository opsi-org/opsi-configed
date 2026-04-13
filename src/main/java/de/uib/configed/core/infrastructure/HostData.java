/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */
package de.uib.configed.core.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostData {
	private String host;
	private String user;
	private String password;
	private String otp;
	@Accessors(fluent = true)
	private boolean useSSO;
	@Accessors(fluent = true)
	private boolean useMFA;
}
