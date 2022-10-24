package de.uib.utilities;

import java.io.*;

public class FileX
{
	
	public static String getPath(String[] parts, boolean absolute, String separator)
	{
		if (parts == null || parts.length == 0)
			return "";
		
		String result = parts[0];
		
		if (absolute)
			result = separator + result;
		
		for (int i = 1; i<parts.length; i++)
		{
			result = result + separator + parts[i];
		}
		
		return result;
	}
	
	public static String getLocalsystemPath(String[] parts)
	{
		return getPath(parts, false, File.separator);
	}
	
	//public static String getRemotePath(String protocol, String server, String share, String[] parts)
	public static String getRemotePath(String server, String share, String[] parts)
	{
		if (parts == null || parts.length == 0)
			return "";
		
		String result = File.separator + File.separator + server + File.separator + share;
		
		for (int i = 0; i<parts.length; i++)
		{
			result = result + File.separator + parts[i];
		}
		
		//mount
		
		return result;
	}
	
}
