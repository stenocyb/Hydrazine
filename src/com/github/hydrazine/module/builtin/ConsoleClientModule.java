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
import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.data.message.TranslationMessage;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

/**
 * 
 * @author xTACTIXzZ
 *
 * Combination of 'chat' and 'readchat' module
 *
 */
public class ConsoleClientModule implements Module
{

	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getName() 
	{
		return "cclient";
	}

	@Override
	public String getDescription() 
	{
		return "This module lets you send and receive chat messages.";
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
		System.out.println("Goodbye, my friend!");
	}

	@Override
	public void configure() 
	{
		settings.setProperty("sendDelay", ModuleSettings.askUser("Delay between sending messages: "));
		settings.setProperty("filterColorCodes", ModuleSettings.askUser("Filter color codes?"));
		
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
	 * Registers the listeners in order to read the chat
	 */
	private void registerListeners(Client client)
	{
		client.getSession().addListener(new SessionAdapter() 
		{
			@Override
			public void packetReceived(PacketReceivedEvent event) 
			{
				if(event.getPacket() instanceof ServerChatPacket)
				{
					ServerChatPacket packet = ((ServerChatPacket) event.getPacket());
					
					// Check if message is a chat message
					if(packet.getType() != MessageType.NOTIFICATION)
					{         
						if(settings.containsKey("filterColorCodes") && settings.getProperty("filterColorCodes").equals("true"))
						{
							String line = packet.getMessage().getFullText();
							
							if(packet.getMessage() instanceof TranslationMessage)
							{
								TranslationMessage msg = (TranslationMessage) packet.getMessage();
								
								String message = "";
								
								if(msg.getTranslationKey().startsWith("chat.type"))
								{
									message = String.format("<%s> %s", (Object[]) msg.getTranslationParams());
								}
								else if(msg.getTranslationKey().equals("commands.message.display.incoming"))
								{
									message = String.format("[PM] <%s> %s", (Object[]) msg.getTranslationParams());					
								}
								else if(msg.getTranslationKey().startsWith("multiplayer.player"))
								{
									if(msg.getTranslationKey().endsWith("left"))
									{
										message = String.format("%s left the game.", (Object[]) msg.getTranslationParams());							
									}
									else if(msg.getTranslationKey().endsWith("joined"))
									{
										message = String.format("%s joined the game.", (Object[]) msg.getTranslationParams());	
									}
								}
								
								if(!message.equals(""))
								{
									line = message;
								}
							}
															                		
               		       
							// Filter out color codes
							if(line.contains("§"))
							{
								String builder = line;
								
								int count = line.length() - line.replaceAll("§", "").length();
								
								for(int i = 0; i < count; i++)
								{
									int index = builder.indexOf("§");
									
									if(index > (-1)) // Check if index is invalid, happens sometimes.
									{		
										String buf = builder.substring(index, index + 1);
										
										String repl = builder.replace(buf, "");
										                				
										builder = repl;
									}
								}
								
								System.out.println(Hydrazine.inputPrefix + builder);
							}
							else
							{
								System.out.println(Hydrazine.inputPrefix + line);
							}
						}
						else
						{
							if(packet.getMessage() instanceof TranslationMessage)
							{
								TranslationMessage msg = (TranslationMessage) packet.getMessage();
																
								if(msg.getTranslationKey().startsWith("chat.type"))
								{
									String message = String.format("<%s> %s", (Object[]) msg.getTranslationParams());
									
									System.out.println(message);
								}
								else if(msg.getTranslationKey().equals("commands.message.display.incoming"))
								{
									String message = String.format("[PM] <%s> %s", (Object[]) msg.getTranslationParams());
									
									System.out.println(message);
								}
								else if(msg.getTranslationKey().startsWith("multiplayer.player"))
								{
									if(msg.getTranslationKey().endsWith("left"))
									{
										String message = String.format("%s left the game.", (Object[]) msg.getTranslationParams());
										
										System.out.println(message);
									}
									else if(msg.getTranslationKey().endsWith("joined"))
									{
										String message = String.format("%s joined the game.", (Object[]) msg.getTranslationParams());
										
										System.out.println(message);
									}
								}
							}
							else
							{
								System.out.println(packet.getMessage().getFullText());
							}
						}
					}
				}
			}
		});
	}
	
	/*
	 * Does all of the chatting related things
	 */
	private void doStuff(Client client, Scanner sc)
	{		
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
