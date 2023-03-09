package de.uib.configed.tree;

public class VisualClientNodeNameModifierFactory {
	VisualClientNodeNameModifier mod;

	static class SuppressTerminatingUnderscores implements VisualClientNodeNameModifier {
		@Override
		public String modify(final String in) {
			if (in == null) {
				return null;
			}

			int l = in.length();
			int i = l - 1;
			while (i > 0 && (in.charAt(i) == '_')) {
				i--;
			}

			if (i == l - 1) {
				return in;
			}

			return in.substring(0, i + 1);
		}
	}

	public VisualClientNodeNameModifierFactory() {
		mod = new SuppressTerminatingUnderscores();
	}

	public VisualClientNodeNameModifier getModifier() {
		return mod;
	}
}
