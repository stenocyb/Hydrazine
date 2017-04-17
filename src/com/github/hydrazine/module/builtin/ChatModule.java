package com.github.hydrazine.module.builtin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Properties;
import java.util.Scanner;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.ConnectedEvent;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.ClientFactory;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleHelper;
import com.github.hydrazine.util.UsernameGenerator;

public class ChatModule implements Module
{

	// Configuration settings are stored in here
	private Properties properties = new Properties();
	
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "." + this.getClass().getName());
	
	// Configuration variables
	private int amplifier = 1;
	private int amplDelay = 500;
	
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
		loadProperties();
		
		amplifier = Integer.parseInt(properties.getProperty("amplifier"));
		
		System.out.println(Hydrazine.infoPrefix + "Starting module \'" + getName() + "\'. Press CTRL + C to exit.");

		Authenticator auth = new Authenticator();
		ClientFactory factory = new ClientFactory();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			if(Hydrazine.settings.hasSetting("username"))
			{
				String username = Hydrazine.settings.getSetting("username");
				
				MinecraftProtocol protocol = auth.authenticate(username);
				
				connect(factory, protocol, server);
			}
			else
			{
				String method = Hydrazine.settings.getSetting("genuser");
				
				UsernameGenerator ug = new UsernameGenerator();
				
				String username = ug.deliverUsername(method);
				
				MinecraftProtocol protocol = auth.authenticate(username);
				
				connect(factory, protocol, server);
			}
		}
		// Server has offline mode disabled
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials creds = null;
			
			try
			{
				String[] parts = Hydrazine.settings.getSetting("credentials").split(":");
				creds = new Credentials(parts[0], parts[1]);
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value for switch -cr");
				
				return;
			}
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = null;
				
				try
				{
					String[] parts = Hydrazine.settings.getSetting("authproxy").split(":");
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
				}
				catch(Exception e)
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch -ap");
					
					return;
				}
				
				MinecraftProtocol protocol = auth.authenticate(creds, proxy);
				
				connect(factory, protocol, server);
			}
			else
			{				
				MinecraftProtocol protocol = auth.authenticate(creds);
				
				connect(factory, protocol, server);
			}
			
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
		ModuleHelper mh = new ModuleHelper();
		
		try
		{
			amplifier = Integer.parseInt(mh.askUser("How many times should each message be sent?"));
			amplDelay = Integer.parseInt(mh.askUser("Delay between amplified messages: "));
		}
		catch(Exception e)
		{
			System.out.println(Hydrazine.errorPrefix + "Invalid value.");
			
			return;
		}
		
		properties.setProperty("amplifier", String.valueOf(amplifier));
		properties.setProperty("amplDelay", String.valueOf(amplDelay));
		
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			try 
			{
				configFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Unable to create configuration file");
				
				return;
			}
		}
		
		// Store configuration variables
		try 
		{
			properties.store(new FileOutputStream(configFile), null);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
			System.out.println(Hydrazine.errorPrefix + "Unable to store configurations");
		}
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
                     System.out.println("\n" + Hydrazine.infoPrefix + ((MinecraftProtocol) client.getPacketProtocol()).getProfile().getName() + " joined the game!");
                }
            }
            
            @Override
            public void connected(ConnectedEvent event)
            {
                System.out.println(Hydrazine.infoPrefix + ((MinecraftProtocol) client.getPacketProtocol()).getProfile().getName() + " connected to the server!");
            }

            @Override
            public void disconnected(DisconnectedEvent event) 
            {
            	System.out.println(Hydrazine.infoPrefix + "Client disconnected: " + event.getReason());
            	            	
            	System.exit(1);
            }
        });
	}
	
	/*
	 * Connect client to server
	 */
	private void connect(ClientFactory factory, MinecraftProtocol protocol, Server server)
	{
		// Check if authenticated successfully
		if(protocol == null)
		{
			System.out.println(Hydrazine.errorPrefix + "Could not authenticate, possibly invalid credentials.");
			
			return;
		}
		
		// Check if socks proxy should be used
		if(Hydrazine.settings.hasSetting("socksproxy"))
		{
			Proxy proxy = null;
			
			try
			{
				String[] parts = Hydrazine.settings.getSetting("socksproxy").split(":");
				proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value for switch -sp");
				
				return;
			}
			
			Client client = factory.create(server, protocol, proxy);
			
			registerListeners(client);
						
			client.getSession().connect();
			
			while(client.getSession().isConnected())
			{
				Scanner sc = new Scanner(System.in);
				
				System.out.print(Hydrazine.inputPrefix);
				
				String line = sc.nextLine();
				
				if(line.contains("%%"))
				{
					int index = line.indexOf("%");
					String buf = line.substring(index, index + 3);
					
					String s = buf.replaceAll("%%", "");
					
					line = line.replaceAll(buf, "");
					
					try
					{
						amplifier = Integer.parseInt(s);
					}
					catch(Exception e)
					{
						// Ignore
						
						continue;
					}
				}
				
				for(int i = 0; i < amplifier; i++)
				{
					client.getSession().send(new ClientChatPacket(line));
					
					try 
					{
						Thread.sleep(Integer.parseInt(properties.getProperty("amplDelay")));
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			Client client = factory.create(server, protocol);
			
			registerListeners(client);
						
			client.getSession().connect();
			
			while(client.getSession().isConnected())
			{
				Scanner sc = new Scanner(System.in);
				
				System.out.print(Hydrazine.inputPrefix);
				
				String line = sc.nextLine();
				
				if(line.contains("%%"))
				{
					int index = line.indexOf("%");
					String buf = line.substring(index, index + 3);
					
					String s = buf.replaceAll("%%", "");
					
					line = line.replaceAll(buf, "");
					
					try
					{
						amplifier = Integer.parseInt(s);
					}
					catch(Exception e)
					{
						// Ignore
						
						continue;
					}
				}
				
				for(int i = 0; i < amplifier; i++)
				{
					client.getSession().send(new ClientChatPacket(line));
					
					try 
					{
						Thread.sleep(Integer.parseInt(properties.getProperty("amplDelay")));
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*
	 * Load properties from file 
	 */
	private void loadProperties() 
	{
		if(configFile.exists())
		{
			try 
			{
				properties.load(new FileInputStream(configFile));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Unable to load properties from file");
			}
		}
	}

}
