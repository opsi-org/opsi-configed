package de.uib.configed;

public class ErrorCode
{
	static final public int NO_ERROR = 0;
	static final public int INITIALIZATION_ERROR = 1;
	static final public int CONNECTION_ERROR = 2;
	static final public int CLIENTNAMES_FILENAME_MISSING = 11;
	
	
	public static String tell(int n)
	{
		String result = ""; 
		if (n > 0) 
			result = "problem type " + n + ": ";
		switch( n )
		{
			case NO_ERROR :  result = result +  "no error occured"; break;
			case INITIALIZATION_ERROR : result = result +  "inititalization error"; break;
			case CONNECTION_ERROR : result = result +  "connection error"; break;
			case CLIENTNAMES_FILENAME_MISSING : result = result + "REQUIRED: name of file with clientnames"; break;
			default: result = result + "_"; 
		}
		
		return result;
	}	
		
	
}

