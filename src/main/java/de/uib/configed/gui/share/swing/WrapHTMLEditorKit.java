/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import javax.swing.SizeRequirements;
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
	private final ViewFactory factory = new WrapViewFactory();

	protected WrapHTMLEditorKit() {
		super();
	}

	@Override
	public ViewFactory getViewFactory() {
		return factory;
	}

	private static class WrapViewFactory extends HTMLEditorKit.HTMLFactory {

		@Override
		public View create(Element elem) {
			View view = super.create(elem);
			return switch (view) {
			case InlineView _ -> new WrapInlineView(elem);
			case ParagraphView _ -> new CharWrapParagraphView(elem);
			default -> view;
			};
		}
	}

	private static class WrapInlineView extends InlineView {
		public WrapInlineView(Element elem) {
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
	}

	private static class CharWrapParagraphView extends ParagraphView {
		public CharWrapParagraphView(Element elem) {
			super(elem);
		}

		@Override
		protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
			if (r == null) {
				r = new SizeRequirements();
			}
			float pref = layoutPool.getPreferredSpan(axis);
			float min = layoutPool.getMinimumSpan(axis);
			// Don't include insets, Box.getXXXSpan will include them. 
			r.minimum = (int) min;
			r.preferred = Math.max(r.minimum, (int) pref);
			r.maximum = Integer.MAX_VALUE;
			r.alignment = 0.5F;
			return r;
		}
	}
}
