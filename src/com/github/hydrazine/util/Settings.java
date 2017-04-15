package com.github.hydrazine.util;

import java.io.File;

import com.github.hydrazine.minecraft.Server;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class stores the settings/options specified by the user
 *
 */
public class Settings 
{
	
	private Server server;
	private String module;
	private String genUserMethod;
	private File userFile;
	private File credFile;
	private File authProxyFile;
	private File socksProxyFile;
	private int delay;
	
	public Settings()
	{
		
	}
	
	public void setServer(Server server)
	{
		this.server = server;
	}
	
	public Server getServer()
	{
		return server;
	}
	
	public void setModule(String module)
	{
		this.module = module;
	}

	public String getModule()
	{
		return module;
	}
	
	public void setGenerateUsernamesMethod(String method)
	{
		this.genUserMethod = method;
	}
	
	public String getGenerateUsernamesMethod()
	{
		return genUserMethod;
	}
	
	public void setUsernameFile(File file)
	{
		this.userFile = file;
	}
	
	public File getUsernameFile()
	{
		return userFile;
	}
	
	public void setCredentialsFile(File file)
	{
		this.credFile = file;
	}
	
	public File getCredentialsFile()
	{
		return credFile;
	}
	
	public void setAuthProxyFile(File file)
	{
		this.authProxyFile = file;
	}
	
	public File getAuthProxyFile()
	{
		return authProxyFile;
	}
	
	public void setSocksProxyFile(File file)
	{
		this.socksProxyFile = file;
	}
	
	public File getSocksProxyFile()
	{
		return socksProxyFile;
	}
	
	public void setDelay(int delay)
	{
		this.delay = delay;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
}
