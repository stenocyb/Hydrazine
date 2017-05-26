package com.github.hydrazine.module.builtin;

import java.io.File;
import java.net.Proxy;
import java.util.Scanner;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.packetlib.Client;
import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;

/**
 * 
 * @author xTACTIXzZ
 * 
 * Connects a client to a server and lets you send chat messages.
 *
 */
public class ChatModule implements Module
{	
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getName() 
	{
		return "chat";
	}

	@Override
	public String getDescription() 
	{
		return "This module lets you chat on a server.";
	}

	@Override
	public void start()
	{
		// Load settings
		settings.load();
		
		System.out.println(Hydrazine.infoPrefix + "Starting module \'" + getName() + "\'. Press CTRL + C to exit.");

		Scanner sc = new Scanner(System.in);
		
		Authenticator auth = new Authenticator();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			String username = Authenticator.getUsername();
			
			MinecraftProtocol protocol = new MinecraftProtocol(username);
			
			Client client = ConnectionHelper.connect(protocol, server);
			
			while(client.getSession().isConnected())
			{
				doStuff(client, sc);
			}
			
			sc.close();
			
			stop();
		}
		// Server has offline mode disabled
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials creds = Authenticator.getCredentials();
			Client client = null;
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = Authenticator.getAuthProxy();
				
				MinecraftProtocol protocol = auth.authenticate(creds, proxy);
				
				client = ConnectionHelper.connect(protocol, server);
			}
			else
			{				
				MinecraftProtocol protocol = auth.authenticate(creds);
				
				client = ConnectionHelper.connect(protocol, server);
			}
			
			while(client.getSession().isConnected())
			{
				doStuff(client, sc);
			}
			
			sc.close();
			
			stop();
		}
		// User forgot to pass the options
		else
		{
			System.out.println(Hydrazine.errorPrefix + "No client option specified. You have to append one of those switches to the command: -u, -gu or -cr");
		}
	}

	@Override
	public void stop() 
	{
		System.out.println(Hydrazine.infoPrefix + "Module finished, bye bye!");
	}

	@Override
	public void configure() 
	{		
		settings.setProperty("sendDelay", ModuleSettings.askUser("Delay between sending messages: "));
		
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
	
	/*
	 * Does all the input and chatting
	 */
	private void doStuff(Client client, Scanner sc)
	{			
		System.out.print(Hydrazine.inputPrefix);
		
		int sendDelay = 1000;
		
		if(configFile.exists())
		{
			try
			{
				sendDelay = Integer.parseInt(settings.getProperty("sendDelay"));
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");				
			}
		}
		
		String line = sc.nextLine();
		
		int sendTime = 1;
		
		if(line.contains("%"))
		{
			int index = line.indexOf("%");
			String end = line.substring(index, line.length());			
			String s = end.replaceFirst("%", "");
						
			if(s.contains("%"))
			{								
				int index2 = line.replaceFirst("%", "X").indexOf("%");
								
				String part = line.substring(index, index2);
						
				line = line.replaceAll(part + "%", "");
				
				part = part.replaceAll("%", "");
								
				try
				{
					sendTime = Integer.parseInt(part);
				}
				catch(Exception e)
				{
					sendTime = 1;
				}				
			}
		}
		
		for(int i = 0; i < sendTime; i++)
		{
			client.getSession().send(new ClientChatPacket(line));
			
			try 
			{
				Thread.sleep(sendDelay);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

}
