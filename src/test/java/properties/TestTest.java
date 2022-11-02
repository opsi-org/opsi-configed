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

  Logger log = Logger.getGlobal();

  /**
   * Testet, ob die Übersetzungsdateien vollständig sind in dem Sinne, dass
   * in jeder Datei die gleichen Properties enthalten sind...
   * Gibt bei Fehler an, welche Properties fehlen
   */
    @Test
    void testIfEverythingTranslated() {
	  	assertTrue(true);

      Properties properties = new Properties();
      InputStream is = Messages.class.getResourceAsStream("valid_localisations.conf");

      try {
        properties.load(is);
      } catch (Exception e) {
        assertTrue(false, "Fehler beim laden von 'valid_localisations.conf'");
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
     * Vergleicht zwei property-Files und checkt ab ob sie
     * die selben Übersetzungswörter haben
     * @param first erste Sprache (Kürzel)
     * @param second zweite Sprache (Kürzel)
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
        log.info("Localization " + firstFileName + " not found");
        assertTrue(false);
      }

      try {
        secondProperties.load(secondInputStream);
      } catch(Exception e) {
        log.info("Localization " + secondFileName + " not found");
        assertTrue(false);
      }

      Set<String> firstMinusSecond = new HashSet<>(firstProperties.stringPropertyNames());
      firstMinusSecond.removeAll(secondProperties.stringPropertyNames());

      assertTrue(firstMinusSecond.isEmpty(), "Folgende properties fehlen in " + secondFileName + ":\n" + firstMinusSecond.toString());

      Set<String> secondMinusFirst = new HashSet<>(secondProperties.stringPropertyNames());
      secondMinusFirst.removeAll(firstProperties.stringPropertyNames());

      assertTrue(secondMinusFirst.isEmpty(), "Folgende properties fehlen in " + firstFileName + ":\n" + secondMinusFirst.toString());
    }
}