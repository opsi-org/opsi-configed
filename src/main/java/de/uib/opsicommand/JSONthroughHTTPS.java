package de.uib.opsicommand;

/*  Copyright (c) 2006 uib.de
 
Usage of this portion of software is allowed unter the restrictions of the GPL
 
*/

import utils.*;
import org.json.*;
import de.uib.utilities.logging.*;
import de.uib.configed.configed;
import java.io.*;
import java.io.Reader.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;
import java.util.*;


/**
*
* @author Rupert Roeder 
*/

public class JSONthroughHTTPS extends JSONthroughHTTP
{
	protected final String CODING_TABLE = "UTF8";
	static private SSLSocketFactory sslFactory;
	
	/**
	*  @param host
	*  @param username
	*  @param password 
	*/
	public JSONthroughHTTPS (String host, String username, String password)
	{
		super (host, username, password);
		//logging.info(this, "username " + username + ":" + password);
		sslFactory = createDullSSLContext(); //produces sslFactory (which does not really look for certificates)
	}
	
	protected String produceBaseURL(String rpcPath)
	{
		return "https://" + host + ":" +  portHTTPS + rpcPath;
	}

	
	
	/**
	* Opening the connection and set the SSL parameters
	*/
	@Override
	protected HttpURLConnection produceConnection () throws java.io.IOException
	{
		logging.info(this, "produceConnection, url; " + serviceURL); 
		HttpURLConnection connection =  (HttpURLConnection)serviceURL.openConnection();
		((HttpsURLConnection)connection).setSSLSocketFactory(sslFactory);
		((HttpsURLConnection)connection).setHostnameVerifier(new DullHostnameVerifier());
		return connection;
	}
	
	
	class DullHostnameVerifier implements HostnameVerifier
	{
		public boolean verify (String hostname, SSLSession session)
		{
			return true;
		}
	}
	
	private class SecureSSLSocketFactory extends SSLSocketFactory 
	//http://stackoverflow.com/questions/27075678/get-ssl-version-used-in-httpsurlconnection-java
	{
			private final SSLSocketFactory delegate;
			private HandshakeCompletedListener handshakeListener;
			
			public SecureSSLSocketFactory(
					SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
				this.delegate = delegate;
				this.handshakeListener = handshakeListener;
			}
			
			
			@Override
			public Socket createSocket(Socket s, String host, int port, boolean autoClose) 
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket(s, host, port, autoClose);
				logging.debug(this, "createSocket host, port: " + host + "," + port + " autoClose " + autoClose +  
					" enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
			
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			
			@Override
			public Socket createSocket()
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket();
					logging.debug(this, "createSocke " +  
						" enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
					// on some connections there is, after some time, a javax.net.ssl.SSLException: SSL peer shut down incorrectl
					// the standard enabled cipher suite seems to be TLS_RSA_WITH_AES_256_CBC_SHA256
				
					if (!configed.TLS_CIPHER_SUITE.equals(""))
						socket.setEnabledCipherSuites(new String[]{ configed.TLS_CIPHER_SUITE } );
					//			TLS_RSA_WITH_AES_128_GCM_SHA256"});  // with this suit it seems to work
				
				
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			
			
			@Override
			public Socket createSocket(InetAddress host, int port)
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port);
				logging.debug(this, "createSocket host, port: " + host + "," + port + 
					" enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
			
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			
			@Override
			public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket(address, port, localAddress, localPort);
				logging.debug(this, "createSocket adress, port, localAddress, localPort: " + address + "," + port + "," +  localAddress + "," +  localPort 
					+ " enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
				
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			
			@Override
			public Socket createSocket(String host, int port)
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port);
				logging.debug(this, "createSocket host, port: " + host + "," + port + 
					" enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
			
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			
			
				
			@Override
			public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException {
				SSLSocket socket = (SSLSocket) this.delegate.createSocket(host, port, localHost, localPort);
				logging.debug(this, "createSocket host, port, localHost, localPort: " + host + "," + port + "," +  localHost + "," +  localPort +
					" enabled cipher suites "  + Arrays.toString(socket.getEnabledCipherSuites()));
			
				if (null != this.handshakeListener) {
					socket.addHandshakeCompletedListener(this.handshakeListener);
				}
			
				return socket;
			}
			// and so on for all the other createSocket methods of SSLSocketFactory.

			@Override
			public String[] getDefaultCipherSuites() {
				// TODO: or your own preferences
				return this.delegate.getDefaultCipherSuites();
			}
			
			@Override
			public String[] getSupportedCipherSuites() {
				// TODO: or your own preferences
				return this.delegate.getSupportedCipherSuites();
			}			
			
	}
	public class MyHandshakeCompletedListener implements HandshakeCompletedListener {
		@Override
		public void handshakeCompleted(HandshakeCompletedEvent event) {
			SSLSession session = event.getSession();
			String protocol = session.getProtocol();
			String cipherSuite = session.getCipherSuite();
			String peerName = null;
		
			try {
				peerName = session.getPeerPrincipal().getName();
			} catch (SSLPeerUnverifiedException e) {
			}
			logging.info(this, "protocol "  + protocol  + "  peerName " + peerName);
			logging.info(this, "cipher suite "  + cipherSuite);
			
			
		}
	}
	

	
			
	
	protected SSLSocketFactory createDullSSLContext()
	{ 
		try
		{
			SSLContext sslContext = null;
			// create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
				
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };
			
			// install the all-trusting trust manager
			//sslContext= SSLContext.getInstance("SSL");
			//logging.info(this, "install ssl provider");
			try {
				//sslContext = SSLContext.getInstance("TLSv1.2", "SunJSSE");
				logging.info(this, "try to install provider for " +  configed.PREFERRED_SSL_VERSION);
				sslContext = SSLContext.getInstance(configed.PREFERRED_SSL_VERSION, "SunJSSE");
			} catch (Exception e) {
				try {
					logging.warning(this, "we did not install the requested provider, therefore we try with v1");
					sslContext = SSLContext.getInstance("TLSv1", "SunJSSE");
				} catch (Exception e1) {
					// The TLS 1.0 provider should always be available.
					logging.warning(this, "we did not install TLSv1.0 provider");
					throw new AssertionError(e1);
				}
			}
			
			
			
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			//sslFactory = sslContext.getSocketFactory();
			sslFactory = new SecureSSLSocketFactory( sslContext.getSocketFactory(), new MyHandshakeCompletedListener() );
			
			logging.debug(this, "SSLSocketFactory default cipher suites " + Arrays.toString(sslFactory.getDefaultCipherSuites()));
			logging.debug(this, "SSLSocketFactory supported cipher suites " + Arrays.toString(sslFactory.getSupportedCipherSuites()));
			
		}
		catch (Exception e)
		{
			logging.logTrace(e);
		}
		
		return sslFactory;
	}
	
	
	public static void main (String args[])
	{
		String resulting = "";
		JSONthroughHTTPS instance;
		OpsiMethodCall omc = new  OpsiMethodCall ("getProductStates_listOfHashes", new String[]{"pcbon1.uib.local"} );
		String urlEnc = "??";
		try
		{urlEnc = URLEncoder.encode (omc.getJsonString(), "UTF8");
		}
		catch(Exception ex)
		{
			logging.debug("Exception " + ex);
		}
		
		//System.out.println( " omc " + omc + " encoded " + urlEnc);
		
		//instance = new JSONthroughHTTPS ("194.31.185.160",  "cn=admin,dc=uib,dc=local", "umwelt");
		instance = new JSONthroughHTTPS ("0.0.173.186",  "root", "linux123");
		instance.retrieveJSONObject ( omc );
	}
}

