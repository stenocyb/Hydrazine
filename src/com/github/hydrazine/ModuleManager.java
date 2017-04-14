package com.github.hydrazine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This class starts jar files.
 *
 */
public class ModuleManager 
{

	private HashMap<String, Process> processes = new HashMap<String, Process>();
	
	public ModuleManager()
	{
		
	}
	
	/**
	 * Starts a jar file (a module)
	 * @param jarFile An executable jar file
	 * @return whether the operation succeeded or not
	 */
	public boolean launch(File jarFile)
	{
		try 
		{
			// Execute jar file
			Process p = Runtime.getRuntime().exec("java -jar " + jarFile.getAbsolutePath());
			
			processes.put(jarFile.getName(), p);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Stops a previously started module
	 * @param module The name of the module
	 * @return whether the operation succeeded or not
	 */
	public boolean stop(String module)
	{
		try
		{
			if(processes.containsKey(module))
			{
				Process p = processes.get(module);
				
				p.destroy();
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Stops all modules
	 * @return whether the operation succeeded or not
	 */
	public boolean stopAll()
	{
		try
		{
			for(String s : processes.keySet())
			{
				Process p = processes.get(s);
				
				p.destroy();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the jar files contained in a directory
	 * @param dir the module directory
	 * @return the jar files contained in the directory
	 */
	public File[] getModulesFromDir(File dir)
	{		
		ArrayList<File> modules = new ArrayList<File>();
		
		if(dir.isDirectory())
		{
			File[] files = dir.listFiles();
			
			for(File file : files)
			{
				if(file.getName().contains("jar"))
				{
					modules.add(file);
				}
			}
			
			return modules.toArray(new File[modules.size()]);
		}
		else
		{
			System.out.println("Invalid path. Module directory is not a directory.");
			
			return null;
		}
	}
	
}
