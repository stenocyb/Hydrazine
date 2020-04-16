package com.github.hydrazine.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import com.github.hydrazine.Hydrazine;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class handles everything related to module configuration
 *
 */
public class ModuleSettings extends Properties
{
	
	private static final long serialVersionUID = 8876149310557054184L;
	
	private File configFile = null;
	private static Scanner scanner = new Scanner(System.in);
	
	public ModuleSettings(File configFile)
	{
		this.configFile = configFile;
	}
	
	/**
	 * @return the config file
	 */
	public File getConfigFile()
	{
		return configFile;
	}
	
	/**
	 * Set the config file
	 * @param configFile the config file
	 */
	public void setConfigFile(File configFile)
	{
		this.configFile = configFile;
	}
	
	/**
	 * Creates the config file if it is not existing
	 * @return whether the creation suceeded or not
	 */
	public boolean createConfigFile()
	{		
		if(!configFile.exists())
		{
			try 
			{
				configFile.createNewFile();				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Unable to create configuration file.");
				
				return false;
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Loads the properties from the config file
	 */
	public void load()
	{
		if(configFile.exists())
		{
			try 
			{
				super.load(new FileInputStream(configFile));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Unable to load properties from file.");
			}
		}
	}
	
	/**
	 * Stores the properties into the config file
	 */
	public void store()
	{
		if(configFile.exists())
		{
			try
			{
				super.store(new FileOutputStream(configFile), null);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Unable to store configurations");
			}
		}
	}
	
	/**
	 * Asks the user a question
	 * @param question the question to ask
	 * @return the answer from the user
	 */
	public static String askUser(String question)
	{		
		System.out.println(Hydrazine.inputPrefix + question);
		
		String reply = scanner.nextLine();
				
		return reply;
	}
	
	/**
	 * Asks the user a yes/no question
	 * @param question the question to ask
	 * @return the answer from the user, true=yes; false=no
	 */
	public static boolean askUserYesNo(String question)
	{
		System.out.print(Hydrazine.inputPrefix + question + " [Yes/No]: ");
		
		String reply = scanner.nextLine();
				
		// Check if answer is yes
		if(reply.equalsIgnoreCase("y") || reply.equalsIgnoreCase("yes") || reply.equalsIgnoreCase("yeah")) // ;)
		{
			return true;
		}
		else // Treat any other answer as a no
		{
			return false;
		}
	}

}
