package com.github.hydrazine;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleManager;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.module.builtin.AltCheckerModule;
import com.github.hydrazine.module.builtin.ChatModule;
import com.github.hydrazine.module.builtin.ChatReaderModule;
import com.github.hydrazine.module.builtin.ConsoleClientModule;
import com.github.hydrazine.module.builtin.CrackedFloodModule;
import com.github.hydrazine.module.builtin.IconGrabModule;
import com.github.hydrazine.module.builtin.InfoModule;
import com.github.hydrazine.module.builtin.MinecraftStatusModule;
import com.github.hydrazine.module.builtin.PremiumFloodModule;
import com.github.hydrazine.module.builtin.SkinStealerModule;
import com.github.hydrazine.module.builtin.UUIDGrabModule;
import com.github.hydrazine.util.Settings;

/**
 * 
 * @author xTACTIXzZ
 * 
 * Main class of the Hydrazine project
 *
 */
public class Hydrazine
{	
	// Logging prefixes
	public static final String infoPrefix = "+ ";
	public static final String errorPrefix = "Error: ";
	public static final String warnPrefix = "Warning: ";
	public static final String inputPrefix = "> ";
	
	// Module path environment variable
	public static final String modEnvVar = "HYDRAZINE";
	
	// Program version
	public static final double progVer = 1.1;
	
	// Program settings
	public static Settings settings = null;
		
	// Loaded modules
	public static ArrayList<Module> loadedModules = new ArrayList<Module>();
	
	/*
	 * Where everything begins...
	 */
	public static void main(String[] args)
	{
		System.out.println("      _    _           _               _            ");
		System.out.println("     | |  | |         | |             (_)           ");
		System.out.println("     | |__| |_   _  __| |_ __ __ _ _____ _ __   ___ ");
		System.out.println("     |  __  | | | |/ _` | '__/ _` |_  / | '_ \\ / _ \\");
		System.out.println("     | |  | | |_| | (_| | | | (_| |/ /| | | | |  __/     v" + progVer + " (Minecraft 1.12.1-1)");
		System.out.println("     |_|  |_|\\__, |\\__,_|_|  \\__,_/___|_|_| |_|\\___|");
		System.out.println("              __/ |                                 ");
		System.out.println("             |___/                                  \n");

		Options options = new Options();
		
		registerOptions(options);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		ModuleManager mm = new ModuleManager();
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null); // Disable sorting of options
		
		// Initialize built-in modules
		initializeBuiltinModules();
		
		// Load external modules
		loadExternalModules(mm);
		
		// Option(s) without argument
		for(String s : args)
		{
			// List modules when option "-l" has been passed
			if(s.equals("-l") || s.equals("--list"))
			{
				listModules();
				
				System.exit(0);
			}
		}
		
		// Parse options
		try 
		{
			cmd = parser.parse(options, args);
		} 
		catch (ParseException e)
		{
 			System.out.println(Hydrazine.errorPrefix + "Missing host (-h), missing module (-m) or invalid option passed.\n");			
			// Print help when invalid syntax
			formatter.printHelp(100, "hydrazine [Options] -h SERVER -m MODULE", "\nOPTIONS:", options, "\nEXAMPLE:\njava -jar Hydrazine.jar -h www.example.com -p 30000 -m chat -u username123");
			
			System.exit(0);
		}
		
		// Storing the settings in there
		settings = new Settings();
		
		// The target server
		Server server = new Server(cmd.getOptionValue('h'), 25565);
		
		// Validating options
		if(cmd.hasOption('p'))
		{
			int port = 25565;
			
			try
			{
				port = Integer.parseInt(cmd.getOptionValue("p"));
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "The specified port is not a valid number. Using default port. (25565)");
			}
			
			server = new Server(cmd.getOptionValue('h'), port);
		}
		if(cmd.hasOption('m'))
		{
			settings.setSetting("module", cmd.getOptionValue('m'));
		}
		if(cmd.hasOption("gu"))
		{
			settings.setSetting("genuser", cmd.getOptionValue("gu"));
		}
		if(cmd.hasOption("u"))
		{
			settings.setSetting("username", cmd.getOptionValue("u"));
		}
		if(cmd.hasOption("cr"))
		{
			settings.setSetting("credentials", cmd.getOptionValue("cr"));
		}
		if(cmd.hasOption("ap"))
		{
			settings.setSetting("authproxy", cmd.getOptionValue("ap"));
		}
		if(cmd.hasOption("sp"))
		{
			settings.setSetting("socksproxy", cmd.getOptionValue("sp"));
		}
		
		settings.setSetting("host", server.getHost());
		settings.setSetting("port", String.valueOf(server.getPort()));
				
		System.out.println(Hydrazine.infoPrefix + "Starting Hydrazine v" + Hydrazine.progVer + " at " + new Date().toString() + "\n");
		
		// Start internal module
		if(!settings.getSetting("module").contains(".jar"))
		{
			boolean foundModule = false;
			
			for(Module m : loadedModules)
			{
				if(m.getName().equalsIgnoreCase(settings.getSetting("module")))
				{
					if(cmd.hasOption('c')) // Configure module if '-c' switch is present
					{
						m.configure();
						
						boolean answer = ModuleSettings.askUserYesNo("Start module \'" + m.getName() + "\'?");
						
						if(answer)
						{							
							m.start();
						}
					}
					else // Start module if '-c' switch is not present
					{
						m.start();				
					}
						
					foundModule = true;
					
					break;
				}
			}
			
			if(!foundModule)
			{
				System.out.println(Hydrazine.warnPrefix + "Couldn't find module \"" + settings.getSetting("module") + "\""); 
			}
		}
		else // Start external module
		{
			Module m = null;
			
			try 
			{
				m = mm.getModuleFromJar(settings.getSetting("module"));
			} 
			catch (Exception e) 
			{
				System.out.println(Hydrazine.warnPrefix + "Couldn't find module \"" + settings.getSetting("module") + "\"");
			}
			
			if(m != null)
			{
				if(cmd.hasOption('c')) // Configure module if '-c' switch is present
				{
					m.configure();
					
					boolean answer = ModuleSettings.askUserYesNo("Start module \'" + m.getName() + "\'?");
					
					if(answer)
					{
						m.start();
					}
				}
				else // Start module if '-c' switch is not present
				{
					m.start();
				}
			}
		}		
	}
	
	/*
	 * Register options
	 */
	private static void registerOptions(Options options)
	{
		// Declare options
		Option hostOpt = new Option("h", "host", true, "Target server to attack (ip or domain)");
		hostOpt.setArgName("string");
		hostOpt.setRequired(false);
		Option portOpt = new Option("p", "port", true, "Port of the target server (default: 25565)");
		portOpt.setArgName("int");
		Option modOpt = new Option("m", "module", true, "Module to execute");
		modOpt.setArgName("module");
		modOpt.setRequired(true);
		Option confOpt = new Option("c", "configure", false, "Configure a module");
		Option listOpt = new Option("l", "list", false, "List available modules");
		Option genUsrOpt = new Option("gu", "gen-user", true, "Generate username (random, natural, const:%username%)");
		genUsrOpt.setArgName("method");
		Option usrOpt = new Option("u", "username", true, "A minecraft username");
		usrOpt.setArgName("name");
		Option accOpt = new Option("cr", "credentials", true, "Credentials of a minecraft account. (Format: username/email:password)");
		accOpt.setArgName("creds");
		Option aProxyOpt = new Option("ap", "auth-proxy", true, "A proxy used for authentication. Format: host:port (https)");
		aProxyOpt.setArgName("proxy");
		Option sProxyOpt = new Option("sp", "socks-proxy", true, "A proxy used to connect to a server. Format: host:port (socks)");
		sProxyOpt.setArgName("proxy");

		// Add options
		options.addOption(hostOpt);
		options.addOption(portOpt);
		options.addOption(modOpt);
		options.addOption(confOpt);
		options.addOption(listOpt);
		options.addOption(genUsrOpt);
		options.addOption(usrOpt);
		options.addOption(accOpt);
		options.addOption(aProxyOpt);
		options.addOption(sProxyOpt);
	}
	
	/*
	 * Output available modules
	 */
	private static void listModules()
	{
		System.out.println(Hydrazine.infoPrefix + "Built-in modules (" + loadedModules.size() + "):\n");
		
		for(Module m : loadedModules)
		{
			System.out.println("> " + m.getName() + " - " + m.getDescription());
		}
		
		System.out.println("\n" + Hydrazine.infoPrefix + "To run an external module, please specify the path of the jar file by using the switch \"-m\"");
	}
	
	/*
	 * Check if environment variable exists
	 */
	private static boolean hasEnvVar()
	{
		String myEnv = System.getenv(Hydrazine.modEnvVar);
				
		if(myEnv == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	/*
	 * Gets the modules from the directory, specified by the "hydrazine" environment variable
	 */
	private static Module[] getModulesFromEnvVar(ModuleManager mm)
	{
		ArrayList<Module> modules = new ArrayList<Module>();
		String env = System.getenv(Hydrazine.modEnvVar);
		File dir = new File(env);
		
		for(File f : ModuleManager.getJarFilesFromDir(dir))
		{
			try 
			{
				Module m = mm.getModuleFromJar(f.getAbsolutePath());
				modules.add(m);
			} 
			catch (Exception e) 
			{
				System.out.println(Hydrazine.warnPrefix + f.getAbsolutePath() + " isn't a valid module, you should remove it.");
			}
		}
		
		return modules.toArray(new Module[modules.size()]);
	}
	
	/*
	 * Load external modules
	 */
	private static void loadExternalModules(ModuleManager mm)
	{		
		if(hasEnvVar())
		{
			Module[] extModules = getModulesFromEnvVar(mm);
			
			if(extModules != null)
			{
				for(Module m : extModules)
				{
					loadedModules.add(m);
				}
			}
		}
	}
	
	/*
	 * Initialize built-in modules
	 */
	private static void initializeBuiltinModules()
	{
		InfoModule infoM = new InfoModule();
		loadedModules.add(infoM);
		
		IconGrabModule iconM = new IconGrabModule();
		loadedModules.add(iconM);
		
		ChatReaderModule chatReaderM = new ChatReaderModule();
		loadedModules.add(chatReaderM);
		
		ChatModule chatM = new ChatModule();
		loadedModules.add(chatM);
		
		CrackedFloodModule crackedFloodM = new CrackedFloodModule();
		loadedModules.add(crackedFloodM);
		
		PremiumFloodModule premiumFloodM = new PremiumFloodModule();
		loadedModules.add(premiumFloodM);
		
		ConsoleClientModule consoleClientM = new ConsoleClientModule();
		loadedModules.add(consoleClientM);
		
		MinecraftStatusModule minecraftStatusM = new MinecraftStatusModule();
		loadedModules.add(minecraftStatusM);
		
		UUIDGrabModule uuidGrabM = new UUIDGrabModule();
		loadedModules.add(uuidGrabM);
		
		AltCheckerModule altCheckerM = new AltCheckerModule();
		loadedModules.add(altCheckerM);
		
		SkinStealerModule skinStealerM = new SkinStealerModule();
		loadedModules.add(skinStealerM);
	}

}
