package com.github.hydrazine.minecraft;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class stores minecraft login credentials.
 */
public class Credentials 
{
	
	private String username, password;
	
	public Credentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	/**
	 * @return The username/email of the minecraft account
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * @return The password of the minecraft account
	 */
	public String getPassword()
	{
		return password;
	}

}
