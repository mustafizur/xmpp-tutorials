/**
 * Responsible for establishing a connection between a client and an XMPP server. 
 * Provides authentication and connection methods, and callbacks for handling 
 * connection failures.
 * Library used: aSmack 4.0.0
 * 
 * @author      Richard Lopes
 * @version     %I%, %G%
 * @since       1.0
 */

public class XMPP{
	/**
     * IP address or Domain address from the XMPP Server that will be connected to.
     */
	private String serverAddress;
	
	/**
     * Contains all information and methods about the management of the connection.
     */
	private XMPPTCPConnection connection;
	
	/**
     * User login name without domain name: {userName}@{domainName}
     */
	private String loginUser;
	
	/**
     * User password
     */
	private String passwordUser;

	/**
	 * Default constructor 
	 * 
	 * @param  serverAddress	{@link serverAddress}
	 * @param  loginUser 	{@link loginUser}
	 * @param  passwordUser 	{@link passwordUser}
	 */
	public XMPP(String serverAddress, String loginUser, String passwordUser){
		this.serverAddress = serverAddress;
		this.loginUser = loginUser;
		this.passwordUser = passwordUser;
	}

	/**
	 * Creates an AsyncTask to starts a connection usign serverAddress attribute from this class.
	 * It also attach a listener  to handle with changes on connection, like fall down.
	 */
	public void connect(){
		AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>(){			
			@Override
			protected Boolean doInBackground(Void... arg0){
				boolean isConnected = false;

				ConnectionConfiguration config = new ConnectionConfiguration(serverAddress);
				config.setReconnectionAllowed(true);
				
				connection = new XMPPTCPConnection(config);

				XMPPConnectionListener connectionListener = new XMPPConnectionListener();
				connection.addConnectionListener(connectionListener);

				try{
					connection.connect();
					isConnected = true;
				} catch (IOException e){
				} catch (SmackException e){
				} catch (XMPPException e){
				}

				return isConnected;
			}
		};		
		connectionThread.execute();
	}

	/**
	 * Provides an authentication to the server using an username and a password.
	 *
	 * @param  connection	{@link connection}
	 * @param  loginUser	{@link loginUser}
	 * @param  passwordUser	{@link passwordUser}
	 */
	private void login(XMPPConnection connection, final String loginUser, final String passwordUser){
		try{
			connection.login(loginUser, passwordUser);	
		} catch (NotConnectedException e){	
			// If is not connected, a timer is schelude and a it will try to reconnect
			new Timer().schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					connect(loginUser, passwordUser);
				}
			}, 5 * 1000);			
		} catch (SaslException e){
		} catch (XMPPException e){
		} catch (SmackException e){
		} catch (IOException e){
		}
	}
	
	/**
	 * Listener for changes in connection
	 * @see ConnectionListener from org.jivesoftware.smack
	 */
	public class XMPPConnectionListener implements ConnectionListener{	
		@Override
		public void connected(final XMPPConnection connection){
			if(!connection.isAuthenticated())
				login(connection, loginUser, passwordUser);
		}
		@Override
		public void authenticated(XMPPConnection arg0){}
		@Override
		public void connectionClosed(){}
		@Override
		public void connectionClosedOnError(Exception arg0){}
		@Override
		public void reconnectingIn(int arg0){}
		@Override
		public void reconnectionFailed(Exception arg0){}
		@Override
		public void reconnectionSuccessful(){}
	}
}