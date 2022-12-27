/* class Containership
   Author Rupert Roeder 1999
   By the Reflection API, this class establishes und helps to use the
   relationship between some container and the contained components
*/

package de.uib.utilities.swing;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;

import de.uib.utilities.logging.logging;

public class Containership {
	int debugLevel = 4;

	java.awt.Container theContainer;

	public Containership(java.awt.Container conti) {
		theContainer = conti;
		logging.debug("Containership initialized");
	}

	public void doForAllContainedCompis(String methodName, Object[] args) {
		Class[] theArgsTypes = new Class[args.length];
		for (int j = 0; j < args.length; j++) {
			theArgsTypes[j] = args[j].getClass();
		}
		doForAllContained(methodName, args, theArgsTypes, Object.class, theContainer);
	}

	public void doForAllContainedCompisOfClass(String methodName, Object[] args, Class selClass) {
		Class[] theArgsTypes = new Class[args.length];
		for (int j = 0; j < args.length; j++) {
			theArgsTypes[j] = args[j].getClass();
		}

		doForAllContained(methodName, args, theArgsTypes, selClass, theContainer);
	}

	public void doForAllContainedCompisOfClass(String methodName, Object[] args, Class[] theArgsTypes, Class selClass) {
		doForAllContained(methodName, args, theArgsTypes, selClass, theContainer);
	}

	private void doForAllContained(String methodName, Object[] args, Class[] theArgsTypes, Class selClass,
			java.awt.Container in) {
		Component theComp;
		Class theCompClass;
		Method theMethod;
		int cc = 0;
		int i = 0;

		cc = in.getComponentCount();

		logging.debug("Number of Comps " + cc);

		for (i = 0; i < cc; i++) {
			theComp = in.getComponent(i);
			theCompClass = theComp.getClass();

			logging.debug("  " + i + " " + theComp.getClass().getName() + "\n" + theComp.toString() + "\n");

			if (selClass.isInstance(theComp)) // (selClass == theComp.getClass())
			// theComp is an instance of selClass
			{

				if (methodName != "") {
					try {
						theMethod = theCompClass.getMethod(methodName, theArgsTypes);
						theMethod.invoke(theComp, args);
					} catch (Exception ex) {
						logging.debug(methodName + ": not found >>>>> " + ex.toString() + "\n");
					}

				}

			}

			if (theComp instanceof Container)
			// theComp is an instance of Container
			{
				logging.debug("+++ recursion ");
				doForAllContained(methodName, args, theArgsTypes, selClass, (java.awt.Container) theComp);
			}
		}
	}
}
