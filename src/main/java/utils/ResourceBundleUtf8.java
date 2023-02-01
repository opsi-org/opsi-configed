package utils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Same as {@link ResourceBundle} but is target for UTF-8 property file
 * resources. May not be used with 8-bit ASCII - only with UTF-8.
 *
 * @author Marc Neumann
 */
public class ResourceBundleUtf8 {

	// private constructor to hide the implicit public one
	private ResourceBundleUtf8() {
	}

	private static class PropertyResourceBundleUtf8 extends ResourceBundle {

		private final Map<String, String> valueByKey = new HashMap<>();

		private PropertyResourceBundleUtf8(PropertyResourceBundle pBundle) {
			loadEntries(pBundle, valueByKey);
		}

		/**
		 * @see java.util.ResourceBundle#getKeys()
		 */
		@Override
		public Enumeration<String> getKeys() {
			return Collections.enumeration(valueByKey.keySet());
		}

		private void loadEntries(PropertyResourceBundle pBundle, Map<String, String> pValueByKey) {
			for (Enumeration<String> keys = pBundle.getKeys(); keys.hasMoreElements();) {
				String key = keys.nextElement();
				String valueRaw = pBundle.getString(key);
				String value = new String(valueRaw.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

				if (pValueByKey.put(key, value) != null) {
					throw new MissingResourceException(
							"duplicate key [" + key + "] in UTF-8 property resource bundle [" + pBundle + "]", "", "");
				}
			}
		}

		/**
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		@Override
		protected Object handleGetObject(String pKey) {
			return valueByKey.get(pKey);
		}
	}

	private static Map<ClassLoader, Map<String, Map<Locale, ResourceBundle>>> bundleByClassLoaderByBaseNameByLocale = new HashMap<>();

	/**
	 * @see ResourceBundle#getBundle(String)
	 */
	public static final ResourceBundle getBundle(String pBaseName) {
		ResourceBundle bundle = ResourceBundle.getBundle(pBaseName);
		return createUtf8PropertyResourceBundle(bundle);
	}

	/**
	 * @see ResourceBundle#getBundle(String, Locale)
	 */
	public static final ResourceBundle getBundle(String pBaseName, Locale pLocale) {
		ResourceBundle bundle = ResourceBundle.getBundle(pBaseName, pLocale);
		return createUtf8PropertyResourceBundle(bundle);
	}

	/**
	 * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
	 */
	public static ResourceBundle getBundle(String pBaseName, Locale pLocale, ClassLoader pLoader) {

		Map<String, Map<Locale, ResourceBundle>> bundleByBaseNameByLocale;
		Map<Locale, ResourceBundle> bundleByLocale = null;
		ResourceBundle bundle = null;

		synchronized (bundleByClassLoaderByBaseNameByLocale) {
			bundleByBaseNameByLocale = bundleByClassLoaderByBaseNameByLocale.computeIfAbsent(pLoader,
					arg -> new HashMap<>());
		}

		synchronized (bundleByBaseNameByLocale) {
			bundleByLocale = bundleByBaseNameByLocale.computeIfAbsent(pBaseName, arg -> new HashMap<>());
		}

		synchronized (bundleByLocale) {
			bundle = bundleByLocale.computeIfAbsent(pLocale, arg -> {

				ResourceBundle newBundle = ResourceBundle.getBundle(pBaseName, pLocale);
				return createUtf8PropertyResourceBundle(newBundle);
			});
		}
		return bundle;
	}

	private static ResourceBundle createUtf8PropertyResourceBundle(ResourceBundle pBundle) {
		if (!(pBundle instanceof PropertyResourceBundle)) {
			throw new MissingResourceException("only UTF-8 property files are supported", "", "");
		}

		return new PropertyResourceBundleUtf8((PropertyResourceBundle) pBundle);
	}

}
