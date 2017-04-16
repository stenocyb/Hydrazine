package com.github.hydrazine.module;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This interface represents a module
 *
 */
public interface Module
{
	
	public String getName();
	
	public String getDescription();
		
	public void start();
	
	public void stop();
	
	public void configure();
	
}
