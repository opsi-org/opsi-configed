package properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.uib.messages.Messages;

class TestTranslation {
	private static final String FILENAME_VALID_LOCALISATIONS = "valid_localisations.conf";
	private static final String SRC_ROOT_DIR = "src/main/java/de/uib";

	private Set<String> usedProperties = new HashSet<>();

	/**
	 * This Test checks if all translation files are complete, i.e. if they all
	 * contain the same keywords; Test fails if all files don't contain exactly
	 * the same keywords or if a translation-file cannot be found.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	@Test
	void testIfEverythingTranslated() throws IOException {
		Properties properties = new Properties();
		properties.load(Messages.class.getResourceAsStream(FILENAME_VALID_LOCALISATIONS));

		Set<String> keySet = properties.stringPropertyNames();
		Iterator<String> iterator = keySet.iterator();

		String first = "";
		String second = "";

		if (iterator.hasNext()) {
			first = iterator.next();
		}

		locateUsedProperties(new File(SRC_ROOT_DIR));

		while (iterator.hasNext()) {
			second = iterator.next();

			compareTwoLocals(first, second);
			checkIfUsedPropertiesAreTranslated(first);

			first = second;
		}
	}

	/**
	 * Compares the files for both languages and checks if they have exactly the
	 * same properties / translation strings; Test fails if one file is not
	 * complete.
	 * 
	 * @param first  code for first language
	 * @param second code for second language
	 * @throws IOException if an I/O error occurs
	 */
	void compareTwoLocals(String first, String second) throws IOException {
		String firstFileName = "configed_" + first + ".properties";
		String secondFileName = "configed_" + second + ".properties";

		Properties firstProperties = new Properties();
		Properties secondProperties = new Properties();

		firstProperties.load(Messages.class.getResourceAsStream(firstFileName));
		secondProperties.load(Messages.class.getResourceAsStream(secondFileName));

		Set<String> firstSet = firstProperties.stringPropertyNames();
		Set<String> secondSet = secondProperties.stringPropertyNames();

		// Checks for both sets if one contains properties that
		// the other one does not contain
		checkIfFirstSetComplete(firstSet, firstFileName, secondSet);
		checkIfFirstSetComplete(secondSet, secondFileName, firstSet);
	}

	/**
	 * Checks if some elements from the second set are missing in the first set;
	 * test fails in that case.
	 * 
	 * @param firstSet      properties' name for first language properties file
	 * @param firstFileName properties file name for first language
	 * @param secondSet     properties' name for second language properties file
	 */
	void checkIfFirstSetComplete(Set<String> firstSet, String firstFileName, Set<String> secondSet) {
		// Creates a set with the elements existing in `secondSet`,
		// but missing in the `firstSet`
		Set<String> differenceSet = new HashSet<>(secondSet);
		differenceSet.removeAll(firstSet);

		assertTrue(differenceSet.isEmpty(), "\nAt least " + differenceSet.size() + "  properties are missing in "
				+ firstFileName + ":\n" + differenceSet.toString());
	}

	/**
	 * Checks if properties file contains all translation properties, that are
	 * used in the source code.
	 * 
	 * @param languageCode language code for a properties file (de, en, fr, and
	 *                     so on)
	 * @throws IOException if an I/O error occurs
	 */
	void checkIfUsedPropertiesAreTranslated(String languageCode) throws IOException {
		String propertiesFileName = "configed_" + languageCode + ".properties";

		Properties properties = new Properties();
		properties.load(Messages.class.getResourceAsStream(propertiesFileName));

		Set<String> propertiesSet = properties.stringPropertyNames();
		Set<String> differenceSet = new HashSet<>(usedProperties);
		differenceSet.removeAll(propertiesSet);

		assertTrue(differenceSet.isEmpty(), "\nThere are " + differenceSet.size() + " properties untranslated in "
				+ propertiesFileName + ":\n" + differenceSet.toString());
	}

	/**
	 * Locates all translation properties that are used in source code. It works
	 * by searching for `getResourceValue` method and returning first string
	 * parameter (word that is contained in quotation makrs) from that method.
	 * It ignores found words, that end in '.', because it most likely means
	 * that full property name is dynamically generated.
	 * 
	 * @param file source code file to analyze for used properties
	 * @throws IOException if an I/O error occurs
	 */
	void locateUsedProperties(File file) throws IOException {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				locateUsedProperties(subFile);
			}
		} else {
			if (file.getName().endsWith(".java")) {
				String fileContent = Files.readString(Path.of(file.getPath()));
				Pattern pattern = Pattern.compile("getResourceValue\\(\"([^\"]+)\"");
				Matcher matcher = pattern.matcher(fileContent);
				while (matcher.find()) {
					String resourceValue = matcher.group(1);
					if (!resourceValue.endsWith(".")) {
						usedProperties.add(resourceValue);
					}
				}
			}
		}
	}
}
