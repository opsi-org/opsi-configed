package de.uib.utilities.script;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.uib.utilities.logging.logging;

//a very rough class for simple command interpreting
public class Interpreter {

	protected Map<String, Object> specialValues;

	private String command;

	public Interpreter(String[] specialValues) {
		initSpecialValues(specialValues);
	}

	public void setCommand(String s) {
		command = s;
	}

	public static List<String> splitToList(String cmd) {
		List<String> result = new ArrayList<>();

		String blankDelims = " \t\n\r\f";
		String citMarks = "'\"";
		String lastCitMark = null;

		StringTokenizer tok = new StringTokenizer(cmd, blankDelims + citMarks, true);

		StringBuilder partBuff = null;

		while (tok.hasMoreTokens()) {
			String s = tok.nextToken();

			if (citMarks.indexOf(s) > -1) {
				if (partBuff == null) {
					// start of citation
					partBuff = new StringBuilder();
					lastCitMark = s;
				} else {
					if (s.equals(lastCitMark))
					// end of citation
					{

						result.add(partBuff.toString());
						partBuff = null;
					} else
						partBuff.append(s);

				}
			} else if (blankDelims.indexOf(s) > -1) {
				if (partBuff == null)
				// no buff started, real split
				{
				} else
					// buff started
					partBuff.append(s);
			} else
			// no delimiter
			if (partBuff == null)
			// no buff started
			{

				result.add(s);
			} else
				partBuff.append(s);
		}

		if (partBuff != null) {

			result.add(partBuff.toString());
		}

		return result;
	}

	public static String[] splitToStringArr(String cmd) {
		return splitToList(cmd).toArray(new String[] {});
	}

	protected void initSpecialValues(String[] specials) {
		specialValues = new LinkedHashMap<>();
		for (int i = 0; i < specials.length; i++)
			specialValues.put(specials[i], "");
	}

	public void setValues(Map<String, Object> givenValues) {
		for (String key : givenValues.keySet()) {
			if (specialValues.get(key) == null)
				logging.warning(this, "value set for an unknown key");
			else
				specialValues.put(key, "" + givenValues.get(key));
		}
	}

	public String interpret() {
		for (String key : specialValues.keySet()) {
			logging.debug(this, "interpret: replace " + key + " by " + specialValues.get(key));
			command = command.replace(key, (String) specialValues.get(key));
		}

		logging.debug(this, "produced command " + command);
		return command;
	}

}
