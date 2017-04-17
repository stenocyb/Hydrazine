package com.github.hydrazine.util;

import java.util.HashMap;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class stores the settings/options specified by the user
 *
 */
public class Settings 
{
	
	private HashMap<String, String> settings;
	
	public Settings()
	{
		settings = new HashMap<String, String>();
	}
	
	/**
	 * Returns the setting corresponding to the key
	 */
	public String getSetting(String key)
	{
		return settings.get(key);
	}
	
	/**
	 * Sets a setting
	 */
	public void setSetting(String key, String value)
	{
		if(settings.containsKey(key))
		{
			settings.remove(key);
		}
		
		settings.put(key, value);
	}
	
	/**
	 * Checks if the setting exists
	 */
	public boolean hasSetting(String key)
	{
		return settings.containsKey(key);
	}
	
}
