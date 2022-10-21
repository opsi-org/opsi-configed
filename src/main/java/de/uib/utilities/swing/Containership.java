/* class Containership
   Author Rupert Roeder 1999
   By the Reflection API, this class establishes und helps to use the
   relationship between some container and the contained components
*/

package de.uib.utilities.swing;

import java.io.*;
import java.lang.reflect.Method;
import java.awt.*;
import de.uib.utilities.logging.*;


public class Containership
{
  int debugLevel = 4;

  java.awt.Container theContainer;

  public Containership (java.awt.Container conti)
  {
    theContainer = conti;
    logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT, "Containership initialized");
  }

  public void doForAllContainedCompis (String methodName, Object[] args)
  {
   Class[] theArgsTypes = new Class [args.length];
   for (int j = 0; j < args.length; j++)
   {
      theArgsTypes[j] = args[j].getClass();
   }
   doForAllContained (methodName, args, theArgsTypes, Object.class, theContainer);
  }

  public void doForAllContainedCompisOfClass (String methodName, Object[] args, Class selClass)
  {
   Class[] theArgsTypes = new Class [args.length];
   for (int j = 0; j < args.length; j++)
   {
      theArgsTypes[j] = args[j].getClass();
   }

   doForAllContained (methodName, args, theArgsTypes, selClass, theContainer);
  }

  public void doForAllContainedCompisOfClass(String methodName, Object[] args, Class[] theArgsTypes, Class selClass)
  {
  	  doForAllContained (methodName, args, theArgsTypes, selClass, theContainer);
  }
  	  

  private void
   doForAllContained (String methodName, Object[] args, Class[] theArgsTypes, Class selClass, java.awt.Container in)
  {
      Component theComp;
      Class theCompClass;
      Method theMethod;
      int cc = 0;
      int i = 0;

      cc = in.getComponentCount();

      logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT, "\n Number of Comps " + cc);

      for (i=0; i < cc; i++)
      {
        theComp = in.getComponent(i);
        theCompClass = theComp.getClass ();

        logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT, 
        "  " + i + " "  + theComp.getClass().getName()
                    + "\n" +  theComp.toString () + "\n");

        if  (selClass.isInstance (theComp))  // (selClass == theComp.getClass())
           // theComp is an instance of selClass
        {

          if (methodName != "")
          {
           try
           {
             theMethod = theCompClass.getMethod (methodName, theArgsTypes);
             theMethod.invoke (theComp, args);
           }
           catch (Exception ex)
           {
             logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT,
             methodName + ": not found >>>>> " + ex.toString() + "\n");
           }

          }

        };

      if (java.awt.Container.class.isInstance (theComp))
           // theComp is an instance of Container
        {
          logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT, 
          "\n +++ recursion ");
          doForAllContained (methodName, args, theArgsTypes, selClass, (java.awt.Container) theComp);
        }
      }
  }
}
