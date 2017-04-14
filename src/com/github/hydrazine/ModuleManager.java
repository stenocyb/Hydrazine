package com.github.hydrazine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This class starts jar files.
 *
 */
public class ModuleManager 
{

	private ArrayList<Process> processes = new ArrayList<Process>();
	
	public ModuleManager()
	{
		
	}
	
	/**
	 * Starts a jar file (a module)
	 * @param jarFile
	 * @return whether the operation succeeded or not
	 */
	public boolean launch(File jarFile)
	{
		try 
		{
			// Execute jar file
			Process p = Runtime.getRuntime().exec("java -jar " + jarFile.getAbsolutePath());
			
			processes.add(p);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Stop all modules
	 */
	public void stopAll()
	{
		for(Process p : processes)
		{
			p.destroy();
		}
	}
	
	public void list(File dir)
	{
		if(dir.isDirectory())
		{
			
		}
		else
		{
			System.out.println("Invalid path. Module directory is not a directory.");
		}
	}
	
}
