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
import com.github.hydrazine.module.builtin.IconGrabModule;
import com.github.hydrazine.module.builtin.InfoModule;
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
	
	// Module path environment variable
	public static final String modEnvVar = "HYDRAZINE";
	
	// Program version
	public static final double progVer = 1.0;
	
	// Program settings
	public static Settings settings = null;
		
	// Loaded modules
	public static ArrayList<Module> loadedModules = new ArrayList<Module>();
	
	/*
	 * Where everything begins...
	 */
	public static void main(String[] args)
	{
		Options options = new Options();
		
		registerOptions(options);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		ModuleManager mm = new ModuleManager();
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null); 			// Disable sorting of options
		
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
			System.out.println(Hydrazine.errorPrefix + "Missing host (-h) or missing module (-m) or invalid option passed.\n");
			// Print help when invalid syntax
			formatter.printHelp(100, "hydrazine [Options] -h SERVER", "\nOPTIONS:", options, "\nEXAMPLES:\n hydrazine -h www.example.com -p 30000 -m info -uf /path/to/usernames.txt");
			
			System.exit(0);
		}
		
		// Storing the settings in there
		settings = new Settings();
		
		// The target server
		Server server = new Server(cmd.getOptionValue('h'), 25565);
		
		// Validating options
		if(cmd.hasOption('p'))
		{
			server = new Server(cmd.getOptionValue('h'), Integer.parseInt(cmd.getOptionValue('p')));
		}
		if(cmd.hasOption('m'))
		{
			settings.setModule(cmd.getOptionValue('m'));
		}
		if(cmd.hasOption("gu"))
		{
			settings.setGenerateUsernamesMethod(cmd.getOptionValue("gu"));
		}
		if(cmd.hasOption("uf"))
		{
			settings.setUsernameFile(new File(cmd.getOptionValue("uf")));
		}
		if(cmd.hasOption("cf"))
		{
			settings.setCredentialsFile(new File(cmd.getOptionValue("cf")));
		}
		if(cmd.hasOption("ap"))
		{
			settings.setAuthProxyFile(new File(cmd.getOptionValue("ap")));
		}
		if(cmd.hasOption("sp"))
		{
			settings.setSocksProxyFile(new File(cmd.getOptionValue("sp")));
		}
		if(cmd.hasOption("d"))
		{
			settings.setDelay(Integer.parseInt(cmd.getOptionValue('d')));
		}
		
		settings.setServer(server);
		
		System.out.println(Hydrazine.infoPrefix + "Starting Hydrazine " + Hydrazine.progVer + " at " + new Date().toString() + "\n");
		
		// Start internal module
		if(!settings.getModule().contains(".jar"))
		{
			boolean foundModule = false;
			
			for(Module m : loadedModules)
			{
				if(m.getName().equalsIgnoreCase(settings.getModule()))
				{
					if(cmd.hasOption('c'))	// Configure module if '-c' switch is present
					{
						m.configure();
					}
					else					// Start module if '-c' switch is not present
					{
						m.start();
					}
						
					foundModule = true;
					
					break;
				}
			}
			
			if(!foundModule)
			{
				System.out.println(Hydrazine.warnPrefix + "Couldn't find module \"" + settings.getModule() + "\""); 
			}
		}
		else // Start external module
		{
			Module m = null;
			
			try 
			{
				m = mm.getModuleFromJar(settings.getModule());
			} 
			catch (Exception e) 
			{
				System.out.println(Hydrazine.warnPrefix + "Couldn't find module \"" + settings.getModule() + "\"");
			}
			
			if(m != null)
			{
				if(cmd.hasOption('c')) 	// Configure module if '-c' switch is present
				{
					m.configure();
				}
				else					// Start module if '-c' switch is not present
				{
					m.start();
				}
			}
		}
		
		// TODO is there something left to do? idk, i'll check tomorrow
	}
	
	/*
	 * Register options
	 */
	private static void registerOptions(Options options)
	{
		// Declare options
		Option hostOpt = new Option("h", "host", true, "Target server to attack (ip or domain)");
		hostOpt.setArgName("string");
		hostOpt.setRequired(true);
		Option portOpt = new Option("p", "port", true, "Port of the target server (default: 25565)");
		portOpt.setArgName("int");
		Option modOpt = new Option("m", "module", true, "Module to execute");
		modOpt.setArgName("module");
		modOpt.setRequired(true);
		Option confOpt = new Option("c", "configure", false, "Configure a module");
		Option listOpt = new Option("l", "list", false, "List available modules");
		Option genUsrOpt = new Option("gu", "gen-user", true, "Generate usernames (random, natural, const:%username%)");
		genUsrOpt.setArgName("method");
		Option usrOpt = new Option("uf", "user-file", true, "File containing usernames");
		usrOpt.setArgName("file");
		Option accOpt = new Option("cf", "cred-file", true, "File containing valid minecraft login credentials");
		accOpt.setArgName("file");
		Option aProxyOpt = new Option("ap", "auth-proxy", true, "File containing authentication proxies [http(s)] (host:port)");
		aProxyOpt.setArgName("file");
		Option sProxyOpt = new Option("sp", "socks-proxy", true, "File containing socks proxies (host:port)");
		sProxyOpt.setArgName("file");
		Option delayOpt = new Option("d", "delay", true, "Delay before connection; in milliseconds");
		delayOpt.setArgName("millis");
		
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
		options.addOption(delayOpt);
	}
	
	/*
	 * Output available modules
	 */
	private static void listModules()
	{
		System.out.println(Hydrazine.infoPrefix + "Built-in modules:\n");
		
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
		
		for(File f : mm.getJarFilesFromDir(dir))
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
	}

}
