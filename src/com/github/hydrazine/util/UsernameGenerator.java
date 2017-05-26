package com.github.hydrazine.util;

import java.util.Random;

import com.github.hydrazine.Hydrazine;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This class generates valid minecraft usernames
 *
 */
public class UsernameGenerator 
{
	
	private final String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	
	public UsernameGenerator()
	{
		/*
		 * Minecraft usernames:
		 *   - min length: 3
		 *   - max length: 16
		 *   - allowed chars: abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_
		 */
	}
	
	/**
	 * Delivers a username by specifying the generation method
	 * @param method the generation method
	 * @return A username
	 */
	public String deliverUsername(String method)
	{
		if(method.equalsIgnoreCase("random"))
		{
			return generateRandom();
		}
		else if(method.equalsIgnoreCase("natural"))
		{
			return generateNatural();
		}
		else if(method.startsWith("const"))
		{
			String[] parts = method.split(":");
			String base = parts[1];
			
			return constantUsername(base);
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "Invalid username generation method specified.");
		}
		
		return null;
	}
	
	/**
	 * Generates a completely random username 
	 * @param length The desired length the username should have
	 * @return A generated, valid minecraft username
	 */
	public String generateRandom()
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 3+(int)(Math.random()*14); i++)
		{
			sb.append(allowedChars.split("")[(int)(Math.random() * allowedChars.length())]);
		}
			
		return sb.toString();
	}
	
	/**
	 * Generates a somewhat legitimate appearing username
	 * @return A generated, valid minecraft username
	 */
	public String generateNatural()
	{
		String[] parts1 = {"Hero", "Player", "Fighter", "Flame", "Hunter", "Alpha", "Slayer", "Creeper", "Creator", "Spider"}; // Word length: 3-7
		String[] parts2 = {"Maker", "Boss", "Killer", "Builder", "Star", "Friend", "Enemy", "Smoke", "Shadow", "Miner"}; // Word length: 3-7
		
		StringBuilder sb = new StringBuilder(16);
		Random r = new Random();
		
		sb.append(parts1[r.nextInt(parts1.length)]);
		sb.append(parts2[r.nextInt(parts2.length)]);
		
		// Append suffix
		int suffix = r.nextInt(100);
		
		if(suffix < 10)
			suffix += 10;
		
		sb.append(suffix);
		
		return sb.toString();
	}
	
	/**
	 * Generates a username by appending a random suffix to the base username
	 * @param username The base username
	 * @return A generated, valid minecraft username
	 */
	public String constantUsername(String username)
	{
		StringBuilder sb = new StringBuilder(16);
		Random r = new Random();

		int len = username.length();
		
		if(len < 3 || len > 15) // 15 because we have to add at least one char to the username
		{
			System.out.println(Hydrazine.errorPrefix + len + " is not a valid minecraft username length.");
			
			return null;
		}
		else
		{
			int a = 16 - len;
			
			sb.append(username); // Appending the base username
			
			for(int i = 0; i < a; i++)
			{
				sb.append(r.nextInt(10)); // Appending random numbers until we've reached 16 chars
			}
			
			return sb.toString();
		}		
	}

}
