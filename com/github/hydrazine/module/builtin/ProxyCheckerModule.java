package com.github.hydrazine.module.builtin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.FileFactory;
import com.github.hydrazine.util.ProxyChecker;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module steals the skin of a minecraft player
 *
 */
public class ProxyCheckerModule implements Module
{
	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	// Output File
	private File outputFile;
	
	@Override
	public String getModuleName() 
	{
		return "proxychecker";
	}

	@Override
	public String getDescription() 
	{
		return "Checks the online status of the proxies supplied by \'-ap\' or \'-sp\'.";
	}

	@Override
	public void start() 
	{		
		if(!configFile.exists())
		{
			settings.createConfigFile();
		}
		
		settings.load();
				
		if(Hydrazine.settings.hasSetting("authproxy"))
		{
			if(Hydrazine.settings.getSetting("authproxy").contains(":"))
			{
				Proxy p = Authenticator.getAuthProxy();
				boolean isOnline = ProxyChecker.checkAuthProxy(p);
				InetSocketAddress addr = (InetSocketAddress) p.address();

				if(isOnline)
					System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
				else
					System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");
				
				if(settings.containsKey("outputFile"))
				{
					BufferedWriter w = null;
					outputFile = new File(settings.getProperty("outputFile"));
					
					try 
					{
						w = new BufferedWriter(new FileWriter(outputFile, true));
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
						
						System.exit(1);
					}
					
					try
					{
						w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
						w.newLine();
						w.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
				}
			}
			else
			{
				File authFile = new File(Hydrazine.settings.getSetting("authproxy"));
				
				if(authFile.exists())
				{
					BufferedWriter w = null;
					FileFactory authFactory = new FileFactory(authFile);
					Proxy[] proxies = authFactory.getProxies(Proxy.Type.HTTP);
					
					if(settings.containsKey("outputFile"))
					{
						outputFile = new File(settings.getProperty("outputFile"));
						
						try 
						{
							w = new BufferedWriter(new FileWriter(outputFile, true));
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
							
							System.exit(1);
						}
					}
					
					for(Proxy p : proxies)
					{
						boolean isOnline = ProxyChecker.checkAuthProxy(p);
						InetSocketAddress addr = (InetSocketAddress) p.address();

						if(isOnline)
							System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
						else
							System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");
						
						if(settings.containsKey("outputFile"))
						{	
							try
							{
								w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
								w.newLine();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							
							System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
						}
					}
					
					if(w != null)
					{
						try 
						{
							w.close();
						}
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				}
				else
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch \'-ap\'");					
				}
			}
		}
		else if(Hydrazine.settings.hasSetting("socksproxy"))
		{
			if(Hydrazine.settings.getSetting("socksproxy").contains(":"))
			{
				Proxy p = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(Hydrazine.settings.getSetting("socksproxy").split(":")[0], Integer.valueOf(Hydrazine.settings.getSetting("socksproxy").split(":")[1])));
				boolean isOnline = ProxyChecker.checkSocksProxy(p);
				InetSocketAddress addr = (InetSocketAddress) p.address();

				if(isOnline)
					System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
				else
					System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");
				
				if(settings.containsKey("outputFile"))
				{
					BufferedWriter w = null;
					outputFile = new File(settings.getProperty("outputFile"));
					
					try 
					{
						w = new BufferedWriter(new FileWriter(outputFile, true));
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
						
						System.exit(1);
					}
					
					try
					{
						w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
						w.newLine();
						w.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
				}
			}
			else
			{
				File socksFile = new File(Hydrazine.settings.getSetting("socksproxy"));
				
				if(socksFile.exists())
				{
					BufferedWriter w = null;
					FileFactory socksFactory = new FileFactory(socksFile);
					Proxy[] proxies = socksFactory.getProxies(Proxy.Type.SOCKS);
					
					if(settings.containsKey("outputFile"))
					{
						outputFile = new File(settings.getProperty("outputFile"));
						
						try 
						{
							w = new BufferedWriter(new FileWriter(outputFile, true));
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
							
							System.exit(1);
						}
					}
					
					for(Proxy p : proxies)
					{
						boolean isOnline = ProxyChecker.checkSocksProxy(p);
						InetSocketAddress addr = (InetSocketAddress) p.address();

						if(isOnline)
							System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
						else
							System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");
						
						if(settings.containsKey("outputFile"))
						{	
							try
							{
								w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
								w.newLine();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							
							System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
						}
					}
					
					if(w != null)
					{
						try 
						{
							w.close();
						}
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				}
				else
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch \'-sp\'");					
				}
			}
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "Missing proxy option (-ap or -sp)");
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
		String answer = ModuleSettings.askUser("Output file:");
		
		if(!(answer.equals("") || answer.isEmpty()))
		{
			settings.setProperty("outputFile", answer);
		}
		else
		{
			settings.remove("outputFile");
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
