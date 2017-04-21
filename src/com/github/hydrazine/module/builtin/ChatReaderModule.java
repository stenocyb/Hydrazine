package com.github.hydrazine.module.builtin;

import java.io.File;
import java.net.Proxy;
import java.util.Scanner;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.data.game.values.MessageType;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.ClientFactory;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;

/**
 * 
 * @author xTACTIXzZ
 *
 * Connects a client to a server and reads the chat.
 *
 */
public class ChatReaderModule implements Module
{
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "." + this.getClass().getName());
	
	// Configuration settings are stored in here	
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getName() 
	{
		return "readchat";
	}

	@Override
	public String getDescription() 
	{
		return "This module connects to a server and passively reads the chat.";
	}

	@Override
	public void start() 
	{
		// Load settings
		settings.load();
		
		System.out.println(Hydrazine.infoPrefix + "Starting module \'" + getName() + "\'. Press CTRL + C to exit.");
		
		Scanner sc = new Scanner(System.in);
		
		Authenticator auth = new Authenticator();
		ClientFactory factory = new ClientFactory();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			String username = auth.getUsername();
			
			MinecraftProtocol protocol = new MinecraftProtocol(username);
			
			Client client = ConnectionHelper.connect(factory, protocol, server);
			
			registerListeners(client);
			
			while(client.getSession().isConnected())
			{
				try
				{
					Thread.sleep(20);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			
			sc.close();
			
			stop();
		}
		// Server has offline mode disabled
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
		    Credentials creds = auth.getCredentials();
			Client client = null;
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = auth.getAuthProxy();
				
				MinecraftProtocol protocol = auth.authenticate(creds, proxy);
				
				client = ConnectionHelper.connect(factory, protocol, server);
			}
			else
			{				
				MinecraftProtocol protocol = auth.authenticate(creds);
				
				client = ConnectionHelper.connect(factory, protocol, server);
			}
						
			registerListeners(client);
			
			while(client.getSession().isConnected())
			{
				try 
				{
					Thread.sleep(20);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
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
		System.out.println("Module stopped, bye!");
	}

	@Override
	public void configure() 
	{				
		settings.setProperty("registerCommand", ModuleSettings.askUser("Enter register command: "));
		settings.setProperty("loginCommand", ModuleSettings.askUser("Enter login command: "));
		settings.setProperty("commandDelay", ModuleSettings.askUser("Enter the delay between the commands in milliseconds: "));
		settings.setProperty("filterColorCodes", String.valueOf(ModuleSettings.askUserYesNo("Filter color codes?")));
		
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
	 * Register listeners
	 */
	private void registerListeners(Client client)
	{
		client.getSession().addListener(new SessionAdapter() 
		{
			@Override
			public void packetReceived(PacketReceivedEvent event) 
			{
				if(event.getPacket() instanceof ServerJoinGamePacket) 
				{
					if(settings.containsKey("loginCommand") && settings.containsKey("registerCommand"))
					{
				    	if(!(settings.getProperty("loginCommand").isEmpty() && settings.getProperty("registerCommand").isEmpty()))
				        {
				        	// Sleep because there may be a command cooldown
				    		try 
				        	{
								Thread.sleep(Integer.parseInt(settings.getProperty("commandDelay")));
							} 
				        	catch (InterruptedException e) 
				        	{
				        		e.printStackTrace();
							}
				        	
				        	client.getSession().send(new ClientChatPacket(settings.getProperty("registerCommand")));
				        	
				        	// Sleep because there may be a command cooldown
				        	try 
				        	{
								Thread.sleep(Integer.parseInt(settings.getProperty("commandDelay")));
							} 
				        	catch (InterruptedException e) 
				        	{
				        		e.printStackTrace();
							}
				        	
				        	client.getSession().send(new ClientChatPacket(settings.getProperty("loginCommand")));
				        }
				    }                    
				}
				else if(event.getPacket() instanceof ServerChatPacket)
				{
					ServerChatPacket packet = ((ServerChatPacket) event.getPacket());
					
					// Check if message is a chat message
					if(packet.getType() != MessageType.NOTIFICATION)
					{            		
						if(settings.containsKey("filterColorCodes") && settings.getProperty("filterColorCodes").equals("true"))
						{
							String line = packet.getMessage().getFullText();
								                		
							String builder = line;
								                		       
							// Filter out color codes
							if(builder.contains("§"))
							{
								int count = builder.length() - builder.replace("§", "").length();
								
								for(int i = 0; i < count; i++)
								{
									int index = builder.indexOf("§");
									
									if(index > (-1)) // Check if index is invalid, happens sometimes.
									{		
										String buf = builder.substring(index, index + 2);
										
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
							System.out.println(Hydrazine.inputPrefix + packet.getMessage().getFullText());
						}
					}
				}
		    }
		});
	}
}
