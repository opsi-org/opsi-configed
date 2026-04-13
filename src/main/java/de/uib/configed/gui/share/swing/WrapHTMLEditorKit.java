/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;

/**
 * {@link HTMLEditorKit} with character-level line wrapping enabled.
 * <p>
 * This implementation customizes the {@link ViewFactory} so that text can wrap
 * at any character rather than only at word boundaries.
 * </p>
 * <p>
 * Instances are intended to be created via {@link WrapEditorKit} to keep editor
 * kit creation centralized.
 * </p>
 */
public final class WrapHTMLEditorKit extends HTMLEditorKit {
	private final ViewFactory factory = new CharWrapViewFactory();

	protected WrapHTMLEditorKit() {
		super();
	}

	@Override
	public ViewFactory getViewFactory() {
		return factory;
	}

	/**
	 * Custom HTMLFactory that replaces the default views with our
	 * character-wrapping versions.
	 */
	private static class CharWrapViewFactory extends HTMLEditorKit.HTMLFactory {
		@Override
		public View create(Element elem) {
			View view = super.create(elem);
			return switch (view) {
			case InlineView _ when AbstractDocument.ContentElementName.equals(elem.getName()) -> new CharWrapInlineView(
					elem);
			case ParagraphView _ -> new CharWrapParagraphView(elem);
			default -> view;
			};
		}
	}

	/**
	 * InlineView that supports character-level wrapping.
	 * <p>
	 * Overrides breakView and getBreakWeight so that lines can break at any
	 * character instead of only at word boundaries.
	 * </p>
	 */
	@SuppressWarnings("java:S2972")
	private static class CharWrapInlineView extends InlineView {
		public CharWrapInlineView(Element elem) {
			super(elem);
		}

		@Override
		public int getBreakWeight(int axis, float pos, float len) {
			return GoodBreakWeight;
		}

		@Override
		public View breakView(int axis, int p0, float pos, float len) {
			if (axis == View.X_AXIS) {
				checkPainter();

				int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
				if (p0 == getStartOffset() && p1 == getEndOffset()) {
					return this;
				}
				return createFragment(p0, p1);
			}
			return this;
		}

		@Override
		public float getMinimumSpan(int axis) {
			if (axis == X_AXIS) {
				// allow the layout to shrink to 1 character (useful for very long words/URLs)
				return 0;
			}
			return super.getMinimumSpan(axis);
		}
	}

	/**
	 * Paragraph view that allows character-level shrinking.
	 * <p>
	 * Only the minimum span is overridden to allow lines to shrink to a single
	 * character. We do not override the layout, because the default Swing HTML
	 * layout already handles {@code <br>
	 * } correctly
	 * </p>
	 */
	private static class CharWrapParagraphView extends ParagraphView {
		public CharWrapParagraphView(Element elem) {
			super(elem);
		}

		@Override
		public float getMinimumSpan(int axis) {
			if (axis == View.X_AXIS) {
				// allow shrinking to a single character
				return 0;
			}
			return super.getMinimumSpan(axis);
		}
	}
}
