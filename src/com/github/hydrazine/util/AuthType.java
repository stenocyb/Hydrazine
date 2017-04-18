package com.github.hydrazine.util;

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Credentials;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class is used to simplify the authentication process
 *
 */
public enum AuthType 
{
	
	CREDENTIALS,
	CRACKED;
	
	AuthType authType;
	
	AuthType()
	{
		authType = this;
	}
	
	/**
	 * @return The credentials specified by the user
	 */
	public Credentials getCredentials()
	{
		if(authType == AuthType.CREDENTIALS)
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
		if(authType == AuthType.CRACKED)
		{
			if(Hydrazine.settings.hasSetting("username"))
			{
				return Hydrazine.settings.getSetting("username");
			}
			else
			{
				String method = Hydrazine.settings.getSetting("genuser");
				
				UsernameGenerator ug = new UsernameGenerator();
				
				return ug.deliverUsername(method);
			}
		}
		else
		{
			return null;
		}
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

}
