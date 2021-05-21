package com.github.hydrazine.module.builtin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.FileFactory;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module retrieves the uuid(s) of a given player name or list
 *
 */
public class UUIDGrabModule implements Module
{

	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getModuleName()
	{
		return "uuid";
	}

	@Override
	public String getDescription()
	{
		return "Returns the UUID(s) of a given player name or list. ";
	}

	@Override
	public void start() 
	{
		if(!configFile.exists())
		{
			settings.createConfigFile();
		}
		
		settings.load();
		
		if(settings.containsKey("inputFile") && !settings.getProperty("inputFile").isEmpty())
		{
			File file = new File(settings.getProperty("inputFile"));
			FileFactory factory = new FileFactory(file);
			String[] usernames = factory.getUsernames();
			for(String username : usernames)
			{
				long timestamp = System.currentTimeMillis() / 1000L;
				URL url;
				
				try 
				{        	
					url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + timestamp);
					
					URLConnection connection = url.openConnection();
					BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine = br.readLine();
					
					if(inputLine == null)
					{
						System.out.println(username + ":null");
						
						continue;
					}
					
					String uuid = inputLine.split(",")[0];
					uuid = uuid.split(":")[1];
					uuid = uuid.replace("\"", "");
					uuid = uuid.replace(" ", "");
					
					System.out.println(username + ":" + uuid);
					
		            br.close();		            
				} 
				catch (Exception e) 
				{				    
				    stop(e.toString());
				}
			}
		}
		else if(Hydrazine.settings.hasSetting("username"))
		{
			String username = Hydrazine.settings.getSetting("username");
			
			long timestamp = System.currentTimeMillis() / 1000L;
			URL url;
			
			try 
			{        	
				url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + timestamp);
				
				URLConnection connection = url.openConnection();
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine = br.readLine();
				
				if(inputLine == null)
				{
					System.out.println(username + ":null");
					
					return;
				}
				
				String uuid = inputLine.split(",")[0];
				uuid = uuid.split(":")[1];
				uuid = uuid.replace("\"", "");
				uuid = uuid.replace(" ", "");
				
				System.out.println(username + ":" + uuid);
				
	            br.close();	            
			} 
			catch (Exception e) 
			{			    
			    stop(e.toString());
			}
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "You need to specify a minecraft username. You can do this by configuring the module to use a list (-c) or by using the username switch (-u)");
		}
	}

	@Override
	public void stop(String cause)
	{
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
		
		System.exit(0);
	}

	@Override
	public void configure() 
	{
		if(ModuleSettings.askUserYesNo("Load usernames from file?"))
		{
			settings.setProperty("inputFile", ModuleSettings.askUser("File path:"));
		}
		
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			boolean success = settings.createConfigFile();
			
			if(!success)
			{
				return;
			}
		}
		
		// Store configuration variables
		settings.store();
	}

	@Override
	public void run() 
	{
		start();
	}
	
}
