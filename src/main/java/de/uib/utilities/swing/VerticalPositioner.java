package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;

public class  VerticalPositioner extends JPanel
{

    public VerticalPositioner (JComponent topC, JComponent centerC, JComponent bottomC)
    {
       setLayout (new BorderLayout());
       add(topC, BorderLayout.NORTH);
       add(centerC, BorderLayout.CENTER);
       add(bottomC, BorderLayout.SOUTH);
    }

    public VerticalPositioner (JComponent firstC, JComponent  secondC)
    {
       setLayout (new BorderLayout());
       add(firstC, BorderLayout.NORTH);
       add( new CenterPositioner(secondC), BorderLayout.CENTER);
    }
}
