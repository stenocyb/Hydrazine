package com.github.hydrazine;

import java.io.File;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.hydrazine.minecraft.Server;
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
	/*
	 * Where everything begins...
	 */
	public static void main(String[] args)
	{
		Options options = new Options();
		
		registerOptions(options);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null); 			// Disable sorting of options
		
		try 
		{
			cmd = parser.parse(options, args);
		} 
		catch (ParseException e)
		{
			System.out.println("~ Missing host (-h) or invalid option passed.\n");
			// Print help when invalid syntax
			formatter.printHelp(100, "hydrazine [Options] -h SERVER", "\nOPTIONS:", options, "\nEXAMPLES:\n hydrazine -h www.example.com -p 30000 -m info -uf /path/to/usernames.txt");
						
			System.exit(0);
		}
		
		System.out.println("> Starting Hydrazine at " + new Date().toString() + "\n");
		
		// Storing the settings in there
		Settings settings = new Settings();
		
		// The target server
		Server server = new Server(cmd.getOptionValue('h'), 25565);
		
		// Validating options
		if(cmd.hasOption('p'))
		{
			server = new Server(cmd.getOptionValue('h'), Integer.parseInt(cmd.getOptionValue('p')));
		}
		else if(cmd.hasOption('m'))
		{
			settings.setModule(cmd.getOptionValue('m'));
		}
		else if(cmd.hasOption('l'))
		{
			// TODO: List modules
		}
		else if(cmd.hasOption("uf"))
		{
			settings.setUsernameFile(new File(cmd.getOptionValue("uf")));
		}
		else if(cmd.hasOption("cf"))
		{
			settings.setCredentialsFile(new File(cmd.getOptionValue("cf")));
		}
		else if(cmd.hasOption("ap"))
		{
			settings.setAuthProxyFile(new File(cmd.getOptionValue("ap")));
		}
		else if(cmd.hasOption("sp"))
		{
			settings.setSocksProxyFile(new File(cmd.getOptionValue("sp")));
		}
		else if(cmd.hasOption("d"))
		{
			settings.setDelay(Integer.parseInt(cmd.getOptionValue('d')));
		}
		
		settings.setServer(server);
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
		Option listOpt = new Option("l", "list", false, "List available modules");
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
		options.addOption(listOpt);
		options.addOption(usrOpt);
		options.addOption(accOpt);
		options.addOption(aProxyOpt);
		options.addOption(sProxyOpt);
		options.addOption(delayOpt);
	}
	
}
