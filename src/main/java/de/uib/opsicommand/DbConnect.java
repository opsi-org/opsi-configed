package de.uib.opsicommand;

import java.sql.*;
import de.uib.utilities.logging.logging;

public class DbConnect
{
	private final String driver = "com.mysql.jdbc.Driver";
	//private String url = "jdbc:mysql://%s/opsi";
	private String url = "jdbc:mysql://%s";
	private final static String defaultDB = "opsi";
	private final static String defaultUser = "opsi";
	private final static String defaultPassword = "linux123"; //"opsi";
	private static Connection con = null;
	
	private static String server;
	private static String user;
	private static String password;

	private DbConnect()
	{
		try {
			Class.forName( driver ).newInstance();
			//new Driver();
		}
		//         catch ( ClassNotFoundException e ) {
		//             e.printStackTrace();
		//             System.exit(1);
		//         }
		catch( Exception e ) {
			e.printStackTrace();
			return;
		}

		if (server != null && server.indexOf("/") == -1)
			server = server + "/" + defaultDB;
		
		url = String.format(url, server);
		logging.info("db url " + url);

		try {
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(true);
		}
		catch( SQLException e ) {
			logging.error( this, e.getMessage() );
			//e.printStackTrace();
			con = null;

			try {
				con = DriverManager.getConnection(url, defaultUser, defaultPassword);
				con.setAutoCommit(true);
			}
			catch( SQLException e1 ) {
				logging.error( this, e1.getMessage() );
				//e.printStackTrace();
				con = null;
			}

		}
	}

	
	
	
	public static Connection getConnection(String serverParameter, String userParameter, String passwordParameter)
	{
		if( con == null )
		{
			if (serverParameter == null)
				server = "localhost";
			else
				server = serverParameter;
			
			if (userParameter == null)
				user = defaultUser;
			else
				user = userParameter;
			
			if (passwordParameter == null)
				password = defaultPassword;
			else 
				password = passwordParameter;
				
				
			new DbConnect();
		}
		return con;
	}
	

	public static Connection getConnection()
	{
		if( con == null )
		{
			logging.warning("try default connections parameters");
		
			return getConnection(null, null, null);
		}
		return con;
	}

	public static void closeConnection()
	{
		try {
			con.close();
		}
		catch( SQLException e ) {
			logging.error( "DbConnect: " + e.getMessage() );
			e.printStackTrace();
		}
		con = null;
	}

	public static boolean checkForExistence( String sql )
	{
		logging.debug( "DbConnect: "+ sql );
		try {
			ResultSet reply = getConnection().createStatement().executeQuery(sql);
			if( reply.next() )
				return true;
		}
		catch( Exception e ){
			logging.error( "DbConnect: " + e.getMessage() );
			e.printStackTrace();
		}
		return false;
	}
}