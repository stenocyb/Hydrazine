package com.github.hydrazine.mc;

import java.net.Proxy;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

/**
 * 
 * @author xTACTIXzZ
 *
 * <br><br>This class creates clients (who would have thought it?)
 */
public class ClientFactory
{
	
	public ClientFactory()
	{
		
	}
	
	/**
	 * Creates a client using a proxy
	 * @param server A minecraft server
	 * @param protocol An authenticated MinecraftProtocol
	 * @param proxy A proxy (socks)
	 * @return A client
	 */
	public Client create(Server server, MinecraftProtocol protocol, Proxy proxy)
	{
		return new Client(server.getHost(), server.getPort(), protocol, new TcpSessionFactory(proxy));
	}
	
	/**
	 * Creates a client
	 * @param server A minecraft server
	 * @param protocol An authenticated MinecraftProtocol
	 * @return A client
	 */
	public Client create(Server server, MinecraftProtocol protocol)
	{
		return new Client(server.getHost(), server.getPort(), protocol, new TcpSessionFactory());
	}

}
