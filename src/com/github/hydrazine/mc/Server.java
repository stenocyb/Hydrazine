package com.github.hydrazine.mc;

/**
 * 
 * @author xTACTIXzZ
 *
 * <br><br>This class represents a minecraft server.
 */
public class Server 
{
	
	private String host;
	private int port;
	
	public Server(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	/**
	 * @return The hostname/ip address of the server
	 */
	public String getHost()
	{
		return host;
	}
	
	/**
	 * @return The port of the server
	 */
	public int getPort()
	{
		return port;
	}

}
