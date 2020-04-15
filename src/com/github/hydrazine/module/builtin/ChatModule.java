package com.github.hydrazine.module.builtin;

import java.io.File;
import java.net.Proxy;
import java.util.Scanner;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

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
		
		System.out.println(Hydrazine.infoPrefix + "Note: You can send a message x amount of times by adding a '%x' to the message. (Without the quotes)");

		Scanner sc = new Scanner(System.in);
		
		Authenticator auth = new Authenticator();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			String username = Authenticator.getUsername();
			
			MinecraftProtocol protocol = new MinecraftProtocol(username);
			
			Client client = ConnectionHelper.connect(protocol, server);
			
			registerListeners(client);
			
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
			
			registerListeners(client);
			
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
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			boolean success = settings.createConfigFile();
			
			if(!success)
			{
				return;
			}
		}
				
		boolean answer = ModuleSettings.askUserYesNo("Send automated message?");
		settings.setProperty("automatedMessages", String.valueOf(answer));
		
		if(answer)
		{
			settings.setProperty("message", ModuleSettings.askUser("Message to send: "));
		}
		
		settings.setProperty("sendDelay", ModuleSettings.askUser("Delay between sending messages: "));
		
		// Store configuration variables
		settings.store();
	}
	
	private void sendAutomatedMessage(Client client, int sendDelay, String msg)
	{
		try 
		{
			Thread.sleep(sendDelay);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		client.getSession().send(new ClientChatPacket(msg));
		System.out.print(".");
	}
	
	private void registerListeners(Client client)
	{
		client.getSession().addListener(new SessionAdapter() 
		{
			@Override
			public void disconnected(DisconnectedEvent event) 
			{
				System.exit(1);
			}
		});
	}
	
	/*
	 * Does all the input and chatting
	 */
	private void doStuff(Client client, Scanner sc)
	{					
		int sendDelay = 1000;
		boolean automatedMessage = false;
		String msg = null;
		
		if(configFile.exists())
		{
			try
			{
				sendDelay = Integer.parseInt(settings.getProperty("sendDelay"));
				automatedMessage = Boolean.valueOf(settings.getProperty("automatedMessages"));
				
				if(automatedMessage)
				{
					msg = settings.getProperty("message");
				}
					
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");				
			}
		}
		
		if(automatedMessage)
		{
			sendAutomatedMessage(client, sendDelay, msg);
		}
		else
		{
			System.out.print(Hydrazine.inputPrefix);

			String line = sc.nextLine();
				
			int sendTime = 1;
			
			if(line.contains("%"))
			{
				int index = line.indexOf("%");
				String end = line.substring(index, line.length());			
				String amount = end.replaceFirst("%", "");
							
				try
				{
					sendTime = Integer.parseInt(amount);
				}
				catch(Exception e)
				{
					//Either %x not at the end of line
					//Or x is not a number
					sendTime = 1;
				}
				
				// Remove "%x" from line
				line = line.substring(0, index);
				line = line.replaceAll("%", "");
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

}
