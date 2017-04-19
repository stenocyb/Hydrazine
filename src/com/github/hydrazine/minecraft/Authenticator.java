package com.github.hydrazine.minecraft;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.mc.protocol.MinecraftProtocol;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.util.UsernameGenerator;

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
			System.out.println(Hydrazine.errorPrefix + "Could not authenticate " + creds.getUsername() + ":" + creds.getPassword() + "!");	
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
			System.out.println(Hydrazine.errorPrefix + "Could not authenticate " + creds.getUsername() + ":" + creds.getPassword() + "!");	
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
	
	/**
	 * @return The auth proxy specified by the user
	 */
	public Proxy getAuthProxy()
	{
		if(Hydrazine.settings.hasSetting("authproxy"))
		{
			Proxy proxy = null;
			
			try
			{
				String[] parts = Hydrazine.settings.getSetting("authproxy").split(":");
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value for switch \'-ap\'");
				
				return null;
			}
			
			return proxy;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return The credentials specified by the user
	 */
	public Credentials getCredentials()
	{
		if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials creds = null;
			
			try
			{
				String[] parts = Hydrazine.settings.getSetting("credentials").split(":");
				creds = new Credentials(parts[0], parts[1]);
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value for switch \'-cr\'");
				
				return null;
			}
			
			return creds;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return The (generated) username specified by the user
	 */
	public String getUsername()
	{
		if(Hydrazine.settings.hasSetting("username"))
		{
			return Hydrazine.settings.getSetting("username");
		}
		else if(Hydrazine.settings.hasSetting("genuser"))
		{
			String method = Hydrazine.settings.getSetting("genuser");
			
			UsernameGenerator ug = new UsernameGenerator();
			
			return ug.deliverUsername(method);
		}
		else
		{
			return null;
		}
	}

}
