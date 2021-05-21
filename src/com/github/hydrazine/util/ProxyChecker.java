package com.github.hydrazine.util;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyChecker 
{
	
	public static boolean checkAuthProxy(Proxy p)
	{
		InetSocketAddress addr = (InetSocketAddress) p.address();
		System.setProperty("https.proxyHost", addr.getHostName());
	    System.setProperty("https.proxyPort", String.valueOf(addr.getPort()));
	    
	    try 
	    {
	        HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com").openConnection();
	        connection.connect();
	        
	        return true;
	    } 
	    catch (Exception e)
	    {
	        return false;
	    }
	}
	
	public static boolean checkSocksProxy(Proxy p)
	{
		InetSocketAddress addr = (InetSocketAddress) p.address();
		System.setProperty("proxySet", "true");
		System.setProperty("socksProxyHost", addr.getHostName());
	    System.setProperty("socksProxyPort", String.valueOf(addr.getPort()));
	    
	    try 
	    {
	        HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com").openConnection();
	        connection.connect();
	        
	        return true;
	    } 
	    catch (Exception e)
	    {
	        return false;
	    }
	}
}
