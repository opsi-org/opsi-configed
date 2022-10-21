package de.uib.utilities.thread;

import java.util.ArrayList;
import javax.swing.*;

import de.uib.utilities.logging.*;


public class WaitInfoString
	{
		private ArrayList<String> waitInfoList;
		private int current = 0;
		private String baseString;
		
		private boolean running;
		
		public WaitInfoString()
		{
			this("");
			init(baseString);
		}
		
		public WaitInfoString(String baseString)
		{
			this.baseString = baseString;
			if (baseString == null)
				this.baseString = "";
			init(this.baseString);
		}
		
		protected void init(String baseString)
		{
			waitInfoList = new ArrayList<String>();
		
			waitInfoList.add(baseString + "       ");
			waitInfoList.add(baseString + " .     ");
			waitInfoList.add(baseString + " ..    ");
			waitInfoList.add(baseString + " ....  ");
			waitInfoList.add(baseString + " ..... " );
		
		}
		
		
		/*
		
		public void feedLabel(JLabel labelInfo)
		{
			if (labelInfo == null)
				return;
			
			
			
			new Thread(){
				public void run()
				{
					running = true;
					while (running) 
					{
						try{
							Thread.sleep(2000);
						}
						catch(InterruptedException ex)
						{
						}
						
						SwingUtilities.invokeLater(new Runnable(){
								public void run()
								{
									String s = next();
									logging.info(this, "set " + s); 
									labelInfo.setText(s);
								}
							}
						);
					}
				}
			}.start();
		}
		*/
						
		public String start()
		{
			current = 0;
			return next();
		}
		
		public String next()
		{
			String result = "";
			
			if (waitInfoList != null && current < waitInfoList.size())
				 result = waitInfoList.get(current);
			
			if (waitInfoList != null)
			{
				current++;
				if (current >= waitInfoList.size()) 
					current = 0;
			}
			
			return result;
		}
	}
