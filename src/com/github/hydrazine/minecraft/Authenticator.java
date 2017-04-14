package com.github.hydrazine.minecraft;

import java.net.Proxy;

import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.mc.protocol.MinecraftProtocol;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This class authenticates the clients
 *
 */
public class Authenticator 
{	
	public Authenticator()
	{
		
	}
	
	/**
	 * Authenticates a client using a proxy
	 * @param creds The credentials of the minecraft account
	 * @param proxy The proxy used to authenticate the client
	 * @return A MinecraftProtocol which can be used to create a client object
	 */
	public MinecraftProtocol authenticate(Credentials creds, Proxy proxy)
	{
		MinecraftProtocol protocol = null;
		
		try 
		{
			protocol = new MinecraftProtocol(creds.getUsername(), creds.getPassword(), false, proxy);
		} 
		catch (RequestException e) 
		{
			System.out.println("Could not authenticate " + creds.getUsername() + ":" + creds.getPassword() + "!");	
		}
		
		return protocol;
	}
	
	/**
	 * Authenticates a client
	 * @param creds The credentials of the minecraft account
	 * @return A MinecraftProtocol which can be used to create a client object
	 */
	public MinecraftProtocol authenticate(Credentials creds)
	{
		MinecraftProtocol protocol = null;
		
		try 
		{
			protocol = new MinecraftProtocol(creds.getUsername(), creds.getPassword(), false);
		} 
		catch (RequestException e) 
		{
			System.out.println("Could not authenticate " + creds.getUsername() + ":" + creds.getPassword() + "!");	
		}
		
		return protocol;
	}
	
	/**
	 * Used for cracked servers
	 * @param username A minecraft username
	 * @return A MinecraftProtocol which can be used to create a client object
	 */
	public MinecraftProtocol authenticate(String username)
	{
		return new MinecraftProtocol(username);
	}

}
