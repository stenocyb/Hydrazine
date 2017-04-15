package com.github.hydrazine.module;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.github.hydrazine.Hydrazine;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class manages modules.
 * 
 */
public class ModuleManager 
{
	
	private ArrayList<Module> activeModules = new ArrayList<Module>();
	
	public ModuleManager()
	{
		
	}
	
	/**
	 * Starts a module
	 * @param m the module
	 */
	public void start(Module m)
	{
		m.start();
		
		// Add module to the active modules list
		if(!activeModules.contains(m))
		{
			activeModules.add(m);
		}
		
		System.out.println(Hydrazine.infoPrefix + "Starting module " + m.getName().toUpperCase());
	}
	
	/**
	 * Stops a module
	 * @param m the mpdule
	 */
	public void stop(Module m)
	{
		m.stop();
		
		// Remove module from the active modules list
		if(activeModules.contains(m))
		{
			activeModules.remove(m);
		}
		
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + m.getName().toUpperCase());
	}
	
	/**
	 * Stops all active modules
	 */
	public void stopAll()
	{
		for(Module m : activeModules)
		{
			m.stop();
		}
	}
	
	/**
	 * Returns a module extracted from a jar file.
	 * @param path the filename of the module
	 * @return a module
	 * @throws Exception if something goes wrong, an exception is thrown
	 */
	public Module getModuleFromJar(String path) throws Exception
	{
		Module m = null;
		
		JarFile modFile = new JarFile(path);
		Manifest mf = modFile.getManifest();
		Attributes attr = mf.getMainAttributes();
		String mainClass = attr.getValue(Attributes.Name.MAIN_CLASS);
		
		Class c = new URLClassLoader(new URL[]{ new File(path).toURI().toURL() }).loadClass(mainClass);
		
		Class[] interfaces = c.getInterfaces();
		
		boolean isPlugin = false;
		
		for(int i = 0; i < interfaces.length && !isPlugin; i++)
		{
			if(interfaces[i].getName().equals("com.github.hydrazine.module.Module"))
			{
				isPlugin = true;
			}
		}
		
		if(isPlugin)
		{
			m = (Module) c.newInstance();
		}
		else
		{
			System.out.println(Hydrazine.warnPrefix + "\"" + path +  "\" isn't a valid module. You should remove it from the directory.");
		}
		
		modFile.close();
		
		return m;
	}
	
	/**
	 * Returns an array of jar files contained in a directory
	 * @param dir the directory
	 * @return see method description ;)
	 */
	public File[] getJarFilesFromDir(File dir)
	{
		ArrayList<File> jarFiles = new ArrayList<File>();
		
		for(File f : dir.listFiles())
		{
			if(f.getName().endsWith(".jar"))
			{
				jarFiles.add(f);
			}
		}
		
		return jarFiles.toArray(new File[jarFiles.size()]);
	}
	
}
