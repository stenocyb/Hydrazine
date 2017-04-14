package com.github.hydrazine;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author xTACTIXzZ
 * 
 * <br><br>This class starts jar files.
 *
 */
public class ModuleManager 
{

	public ModuleManager()
	{
		
	}
	
	/**
	 * Starts a jar file (a module)
	 * @param jarFile
	 * @return
	 */
	public boolean launch(File jarFile)
	{
		try 
		{
			// Execute jar file
			Runtime.getRuntime().exec("java -jar " + jarFile.getAbsolutePath());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
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
