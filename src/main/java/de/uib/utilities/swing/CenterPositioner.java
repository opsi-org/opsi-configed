package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;

public class CenterPositioner extends JPanel
{
  
   public CenterPositioner (JComponent comp)
   {
       setLayout (new FlowLayout());
       add (comp);
   }
  
}
