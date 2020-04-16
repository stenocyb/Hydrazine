package com.github.hydrazine.module.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.module.Module;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module retrieves the status of all minecraft related services
 *
 */
public class MinecraftStatusModule implements Module
{
	
	private final String ANSI_GREEN = "\u001B[32m";
	private final String ANSI_YELLOW = "\u001B[33m";
	private final String ANSI_RED = "\u001B[31m";
	private final String ANSI_RESET = "\u001B[0m";
	
	@Override
	public String getName() 
	{
		return "status";
	}

	@Override
	public String getDescription() 
	{
		return "This module retrieves the status of all minecraft related services";
	}
	
	@Override
	public void start() 
	{
		System.out.println(Hydrazine.infoPrefix + "Getting status from https://status.mojang.com/check ...");
		URL url;
		
		try 
		{        	
			url = new URL("https://status.mojang.com/check");
			
			URLConnection connection = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine = br.readLine();
						
			String parts[] = inputLine.split(","); // 8 parts
            
            for(int i = 0; i < parts.length; i++)
            {
            	String info = parts[i];
            	info = info.replace("[", "");
            	info = info.replace("]", "");
            	info = info.replace("{", "");
            	info = info.replace("}", "");
            	info = info.replace("\"", "");
            	info = info.replace(" ", "");
            	
            	String[] subParts = info.split(":"); // 2 parts
            	String service = subParts[0];
            	String status = subParts[1];
            	
            	if(status.toLowerCase().equals("red"))
            	{
            		if(isUnix())
            		{
            			status = ANSI_RED + "unavailable" + ANSI_RESET;
            		}
            		else
            		{
                		status = "unavailable";
            		}
            	}
            	else if(status.toLowerCase().equals("yellow"))
            	{
            		if(isUnix())
            		{
            			status = ANSI_YELLOW + "some issues" + ANSI_RESET;
            		}
            		else
            		{
                		status = "some issues";
            		}
            	}
            	else if(status.toLowerCase().equals("green"))
            	{
            		if(isUnix())
            		{
            			status = ANSI_GREEN + "no issues" + ANSI_RESET;
            		}
            		else
            		{
                		status = "no issues";
            		}
            	}
            	
            	System.out.println("- [" + service + "]:	" + status);
            }
            
            br.close();
            
            stop();
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		    
		    stop();
		}
	}
	
	@Override
	public void stop() 
	{
		
	}

	@Override
	public void configure() 
	{
		System.out.println(Hydrazine.infoPrefix + "This module can't be configured.");
	}
	
	private boolean isUnix()
	{
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		
		return (operatingSystem.contains("nix") || operatingSystem.contains("nux") || operatingSystem.contains("aix"));
	}
	
}
