package com.github.hydrazine.module;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This interface represents a module
 *
 */
public interface Module extends Runnable
{
	
	public String getModuleName();
	
	public String getDescription();
	
	public void start();
	
	public void stop(String cause);
	
	public void configure();
	
}
