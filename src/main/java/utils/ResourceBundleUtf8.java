package utils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Same as {@link ResourceBundle} but is target for UTF-8 property file resources.
 * May not be used with 8-bit ASCII - only with UTF-8.
 *
 * @author Marc Neumann
 */
public class ResourceBundleUtf8 {

  private static class PropertyResourceBundleUtf8 extends ResourceBundle {

    private final Map<String, String> valueByKey = new HashMap<String, String>();

    private PropertyResourceBundleUtf8(PropertyResourceBundle pBundle) {
      loadEntries(pBundle, valueByKey);
    }

    /**
     * @see java.util.ResourceBundle#getKeys()
     */
    public Enumeration<String> getKeys() {
      return Collections.enumeration(valueByKey.keySet());
    }

    private void loadEntries(PropertyResourceBundle pBundle, Map<String, String> pValueByKey) {
      for (Enumeration<String> keys = pBundle.getKeys(); keys.hasMoreElements();) {
        String key = keys.nextElement();
        String valueRaw = pBundle.getString(key);
        String value;

        try {
          value = new String(valueRaw.getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          throw new MissingResourceException("could not load UTF-8 property resource bundle [" + pBundle + "]", "", "");
        }

        if (pValueByKey.put(key, value) != null) {
          throw new MissingResourceException("duplicate key [" + key + "] in UTF-8 property resource bundle [" + pBundle + "]", "", "");
        }
      }
    }

    /**
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    protected Object handleGetObject(String pKey) {
      return valueByKey.get(pKey);
    }
  }

  private static Map<ClassLoader, Map<String,Map<Locale,ResourceBundle>>> bundleByClassLoaderByBaseNameByLocale =
    new HashMap<ClassLoader, Map<String,Map<Locale,ResourceBundle>>>();

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

    Map<String,Map<Locale,ResourceBundle>> bundleByBaseNameByLocale;
    Map<Locale,ResourceBundle> bundleByLocale = null;
    ResourceBundle bundle = null;

    synchronized (bundleByClassLoaderByBaseNameByLocale) {
      bundleByBaseNameByLocale = bundleByClassLoaderByBaseNameByLocale.get(pLoader);
      if (bundleByBaseNameByLocale ==  null) {
        bundleByBaseNameByLocale = new HashMap<String, Map<Locale,ResourceBundle>>();
        bundleByClassLoaderByBaseNameByLocale.put(pLoader, bundleByBaseNameByLocale);
      }
    }

    synchronized (bundleByBaseNameByLocale) {
      bundleByLocale = bundleByBaseNameByLocale.get(pBaseName);
      if (bundleByLocale == null) {
        bundleByLocale = new HashMap<Locale, ResourceBundle>();
        bundleByBaseNameByLocale.put(pBaseName, bundleByLocale);
      }
    }

    synchronized (bundleByLocale) {
      bundle = bundleByLocale.get(pLocale);
      if (bundle == null) {
        bundle = ResourceBundle.getBundle(pBaseName, pLocale);
        bundle = createUtf8PropertyResourceBundle(bundle);
        bundleByLocale.put(pLocale, bundle);
      }
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

