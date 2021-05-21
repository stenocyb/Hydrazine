package com.github.hydrazine.module.builtin;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module steals the skin of a minecraft player
 *
 */
public class SkinStealerModule implements Module
{

	// Create new file where the configuration will be stored (Same folder as jar file)
	private File configFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private ModuleSettings settings = new ModuleSettings(configFile);
	
	// Output File
	private File outputFile;
	
	@Override
	public String getModuleName() 
	{
		return "skinstealer";
	}

	@Override
	public String getDescription()
	{
		return "Steals the skin of a player and saves it to your computer.";
	}

	@Override
	public void start()
	{
		if(!configFile.exists())
		{
			settings.createConfigFile();
		}
		
		settings.load();
		
		String path = settings.getProperty("outputFile");
		
		if(Hydrazine.settings.hasSetting("username"))
		{
			if(path == null)
			{
				outputFile = new File(Hydrazine.settings.getSetting("username") + ".png");
			}
			else
			{
				outputFile = new File(path + ".png");
			}
			
			String username = Hydrazine.settings.getSetting("username");
			String uuid;
			long timestamp = System.currentTimeMillis() / 1000L;
						
			URL capeUrl = null, uuidUrl = null;
			
			try 
			{
				 uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + timestamp);
			} 
			catch (MalformedURLException e) 
			{
				e.printStackTrace();
				
				System.exit(1);
			}
			
			URLConnection connection;
			BufferedReader br = null;
			String inputLine = null;
			
			try 
			{
				connection = uuidUrl.openConnection();
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				inputLine = br.readLine();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.exit(1);
			}
			
			if(inputLine == null)
			{	
				System.out.println(Hydrazine.infoPrefix + username + " does not exist.");
				
				return;
			}
			
			uuid = inputLine.split(",")[0];
			uuid = uuid.split(":")[1];
			uuid = uuid.replace("\"", "");
			uuid = uuid.replace(" ", "");
						
			try 
			{
				br.close();
				capeUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.exit(1);
			}
			
			URLConnection connection2;
			BufferedReader br2 = null;
			String inputLine2 = null;
			
			try 
			{
				connection2 = capeUrl.openConnection();
				br2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
				inputLine2 = br2.readLine();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.exit(1);
			}		
						
			String parts[] = inputLine2.split(",");
			
			if(parts.length > 3)
			{
				String skinValue = parts[3];
				String base64Value = skinValue.split(":")[1];
				
				base64Value = base64Value.replace("\"", "");
				base64Value = base64Value.replace("]", "");
				base64Value = base64Value.replace("}", "");
				
				byte[] decodedValue = Base64.getDecoder().decode(base64Value);
				String decodedText = null;
				
			    try 
			    {
			    	decodedText = new String(decodedValue, StandardCharsets.UTF_8.toString());
				} 
			    catch (UnsupportedEncodingException e)
			    {
			    	e.printStackTrace();
					
			    	System.exit(1);
				} 
			    	            
			    String[] subParts = decodedText.split(",");
			    
			    for(String s : subParts)
			    {
			    	if(s.toLowerCase().contains("textures.minecraft.net"))
			    	{
			    		String[] subSubParts = s.split(":");
			    		String rawUrl = subSubParts[subSubParts.length - 1];
			    		
			    		rawUrl = rawUrl.replace("}", "");
			    		rawUrl = rawUrl.replace("\"", "");
			    		rawUrl = "http:" + rawUrl;
			    		
			    		System.out.println(Hydrazine.infoPrefix + "Skin: " + rawUrl + "\n");
			    		
			    		BufferedImage image;
			    		URL skinUrl;
			    		
			    		try 
			    		{
							skinUrl = new URL(rawUrl);
							image = ImageIO.read(skinUrl);
							
				            ImageIO.write(image, "png", outputFile);
				            
				            System.out.println(Hydrazine.infoPrefix + "Saved skin to " + outputFile.getAbsolutePath());
						} 
			    		catch (Exception e) 
			    		{
							e.printStackTrace();
							
							System.exit(1);
						}
			    	}
			    }
			}
			else
			{
				System.out.println(Hydrazine.infoPrefix + username + " does not seem to have a skin.");
			}
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "Missing username option (-u)");
		}
	}

	@Override
	public void stop(String cause)
	{
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
		
		System.exit(0);
	}

	@Override
	public void configure()
	{
		String answer = ModuleSettings.askUser("Output file:");
		
		if(!(answer.equals("") || answer.isEmpty()))
		{
			settings.setProperty("outputFile", answer);
		}
		else
		{
			settings.remove("outputFile");
		}
				
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			boolean success = settings.createConfigFile();
			
			if(!success)
			{
				return;
			}
		}
		
		// Store configuration variables
		settings.store();
	}

	@Override
	public void run() 
	{
		start();
	}

}
