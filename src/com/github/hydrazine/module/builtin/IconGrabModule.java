package com.github.hydrazine.module.builtin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module grabs the icon from a server and saves it locally.
 *
 */
public class IconGrabModule implements Module
{

	private boolean hasRetrieved = false;
	
	@Override
	public String getModuleName() 
	{
		return "icongrab";
	}

	@Override
	public String getDescription() 
	{
		return "This module grabs the icon from a server and saves it to your computer.";
	}

	@Override
	public void start() 
	{
		if(!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null)
		{
			System.out.println(Hydrazine.errorPrefix + "You have to specify a server to grab the icon from (-h)");
			
			System.exit(1);
		}
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("> Enter the path where the icon should be saved:");
		String path = sc.nextLine();
		sc.close();
		
		File outputFile = new File(path + ".jpg");
		if(!outputFile.exists())
		{
			try 
			{
				outputFile.createNewFile();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				
				System.out.println(Hydrazine.errorPrefix + "Couldn't create output file.");
				
				return;
			}
		}
		
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
				
		MinecraftProtocol protocol = new MinecraftProtocol(SubProtocol.STATUS);
        Client client = new Client(server.getHost(), server.getPort(), protocol, new TcpSessionFactory());
                
        client.getSession().setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, new ServerInfoHandler() 
        {
            @Override
            public void handle(Session session, ServerStatusInfo info) 
            {
            	BufferedImage icon = info.getIcon();
            	BufferedImage newIcon = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            	
            	for(int i = 0; i < icon.getWidth(); i++)
            	{
            		for(int y = 0; y < icon.getHeight(); y++)
            		{
            			int rgb = icon.getRGB(i, y);
            			newIcon.setRGB(i, y, rgb);
            		}
            	}
            	
            	try 
            	{
					ImageIO.write(newIcon, "jpg", outputFile);
				} 
            	catch (IOException e) 
            	{
            		e.printStackTrace();
            		
            		System.out.println(Hydrazine.errorPrefix + "Could not write to file.");
            		
            		return;
				}
                
                hasRetrieved = true;
            }
        });
        
        client.getSession().connect();
        
        while(!hasRetrieved)
        {
        	try 
        	{
				Thread.sleep(5);
			} 
        	catch (InterruptedException e) 
        	{
				e.printStackTrace();
			}
        }
        
        System.out.println(Hydrazine.infoPrefix + "The icon has been saved to: " + outputFile.getAbsolutePath());
        
        client.getSession().disconnect("finished.");
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
		System.out.println(Hydrazine.infoPrefix + "This module can't be configured.");
	}

	@Override
	public void run() 
	{
		start();
	}
	
}
