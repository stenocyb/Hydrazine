package com.github.hydrazine.module.builtin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.util.Properties;
import java.util.Scanner;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.packetlib.Client;
import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.ClientFactory;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleHelper;
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
	 * Does all the input and chatting
	 */
	private void doStuff(Client client, Scanner sc)
	{	
		System.out.print(Hydrazine.inputPrefix);
		
		String line = sc.nextLine();
		
		if(line.contains("%%"))
		{
			int sendTime = 1;
			int index = line.indexOf("%");
			
			String buf = "";
			
			if(line.length() == index + 3)
			{
				buf = line.substring(index, index + 3);
			}
			else if(line.length() == index + 4)
			{
				buf = line.substring(index, index + 4);
			}
			
			String s = buf.replaceAll("%%", "");
			
			line = line.replaceAll(buf, "");
			
			try
			{
				sendTime = Integer.parseInt(s);
			}
			catch(Exception e)
			{				
				sendTime = 1;
			}
			
			for(int i = 0; i < sendTime; i++)
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
		else
		{
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
