package properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import de.uib.messages.Messages;


class TestTest {
	
	private final static String FILENAME_VALID_LOCALISATIONS = "valid_localisations.conf";

  Logger log = Logger.getGlobal();

  /**
   * This Test checks if all translation files are complete, i.e.
   * if they all contain the same keywords;
   * Test fails if all files don't contain exactly the same keywords
   * or if a translation-file cannot be found
   */
    @Test
    void testIfEverythingTranslated() {

      Properties properties = new Properties();
      InputStream is = Messages.class.getResourceAsStream(FILENAME_VALID_LOCALISATIONS);

      try {
        properties.load(is);
      } catch (Exception e) {
        assertTrue(false, "cannot load file with valid localisations: " + FILENAME_VALID_LOCALISATIONS);
      }
      
      Set<String> keySet = properties.stringPropertyNames();
      
      Iterator<String> iterator = keySet.iterator();

      String first = "";
      String second = "";

      if(iterator.hasNext()) {
        first = iterator.next();
      }

      while(iterator.hasNext()) {
        second = iterator.next();

        compareTwoLocals(first, second);

        first = second;
      }
    }


    /**
     * Compares the files for both languages and checks if they have
     * exactly the same properties / translation strings;
     * Test fails if one file is not complete
     * @param code first for first language
     * @param second code for second language
     */
    void compareTwoLocals(String first, String second) {

      String firstFileName = "configed_" + first + ".properties";
      String secondFileName = "configed_" + second + ".properties";

      Properties firstProperties = new Properties();
      Properties secondProperties = new Properties();
      
      InputStream firstInputStream = Messages.class.getResourceAsStream(firstFileName);
      InputStream secondInputStream = Messages.class.getResourceAsStream(secondFileName);

      try {
        firstProperties.load(firstInputStream);
      } catch(Exception e) {
        assertTrue(false, "Localization " + firstFileName + " not found");
      }

      try {
        secondProperties.load(secondInputStream);
      } catch(Exception e) {
        assertTrue(false, "Localization " + secondFileName + " not found");
      }
      
      Set<String> firstSet = firstProperties.stringPropertyNames();
      Set<String> secondSet = secondProperties.stringPropertyNames();
      
      // Checks for both sets if one contains properties that
      // the other one does not contain
      checkIfFirstSetComplete(firstSet, firstFileName, secondSet);
      checkIfFirstSetComplete(secondSet, secondFileName, firstSet);
    }
    
    /**
     * Checks if some elements from the second set are missing in the first set;
     * test fails in that case
     */
    void checkIfFirstSetComplete(Set<String> firstSet, String firstFileName, Set<String> secondSet) {
		
		// Creates a set with the elements existing in secondSet,
		// but missing in the firstSet
		Set<String> differenceSet = new HashSet<>(secondSet);
		differenceSet.removeAll(firstSet);
		
		assertTrue(differenceSet.isEmpty(), "The following properties are missing in " + firstFileName + ":\n" + differenceSet.toString());
  }
}
