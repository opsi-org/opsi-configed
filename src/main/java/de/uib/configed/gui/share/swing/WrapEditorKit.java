/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import javax.swing.text.EditorKit;

/**
 * Central registry for character wrapped {@link EditorKit} implementations.
 * <p>
 * Each enum constant creates a specific {@code EditorKit} instance, providing a
 * single place to see which editor kits are supported.
 * </p>
 */
public enum WrapEditorKit {
	/** Styled editor kit with wrapping support. */
	STYLED {
		public EditorKit create() {
			return new WrapStyledEditorKit();
		}
	},
	/** HTML editor kit with wrapping support. */
	HTML {
		public EditorKit create() {
			return new WrapHTMLEditorKit();
		}
	};

	public abstract EditorKit create();
}
