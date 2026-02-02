/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * {@link StyledEditorKit} with character-level line wrapping enabled.
 * <p>
 * This implementation customizes the {@link ViewFactory} so that text can wrap
 * at any character rather than only at word boundaries.
 * </p>
 * <p>
 * Instances are intended to be created via {@link WrapEditorKit} to keep editor
 * kit creation centralized.
 * </p>
 */
public final class WrapStyledEditorKit extends StyledEditorKit {
	private transient ViewFactory defaultFactory;

	protected WrapStyledEditorKit() {
		super();
	}

	@Override
	public ViewFactory getViewFactory() {
		if (defaultFactory == null) {
			defaultFactory = new WrapColumnFactory();
		}
		return defaultFactory;
	}

	private static class WrapColumnFactory implements ViewFactory {
		@Override
		public View create(Element elem) {
			String kind = elem.getName();
			return switch (kind) {
			case AbstractDocument.ContentElementName -> new WrapLabelView(elem);
			case AbstractDocument.ParagraphElementName -> new ParagraphView(elem);
			case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
			case StyleConstants.ComponentElementName -> new ComponentView(elem);
			case StyleConstants.IconElementName -> new IconView(elem);
			case null, default -> new LabelView(elem);
			};
		}
	}

	private static class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}

		@Override
		public float getMinimumSpan(int axis) {
			if (axis == View.X_AXIS) {
				// allow wrapping
				return 0;
			}
			return super.getMinimumSpan(axis);
		}
	}
}
