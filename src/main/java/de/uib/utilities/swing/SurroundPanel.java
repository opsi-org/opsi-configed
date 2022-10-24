package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;

 public class SurroundPanel extends JPanel
  {
    public SurroundPanel ()
    {
     super();
     setOpaque(false);
     setLayout (new FlowLayout(FlowLayout.CENTER));
    }
    
    public SurroundPanel ( JComponent c)
    {
     this();
     add (c);
    }
    
  }
