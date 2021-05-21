package com.github.hydrazine.util;

public enum OperatingSystem
{
	
	WINDOWS,
	LINUX,
	MAC;
	
	OperatingSystem()
	{
		
	}
	
	public static boolean isWindows(String osName)
	{
		return osName.toLowerCase().contains("win");
	}
	
	public static boolean isLinux(String osName)
	{
		return (osName.toLowerCase().contains("nix") || osName.toLowerCase().contains("nux") || osName.toLowerCase().contains("aix"));
	}

	public static boolean isMac(String osName)
	{
		return osName.toLowerCase().contains("mac");
	}
}
