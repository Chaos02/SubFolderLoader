package com.chaos02.structuredmodloader;

//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
//import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
//import java.lang.module.Configuration;
//import java.util.stream.Collectors;

import com.chaos02.structuredmodloader.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("structuredmodloader")
public class StructuredModLoader {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public StructuredModLoader() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("StructuredModLoader installed!");
        LOGGER.info("Loading Config now!");
        
        
        LOGGER.info("Loading subfolders not in config!");
        
        try {
        	
        	// Cast Config List to Array
        	String[] ignorePath = new String[SMLConfig.ignoreDir.get().size()];
        	for(int i = 0; i < SMLConfig.ignoreDir.get().size(); i++) ignorePath[i] = SMLConfig.ignoreDir.get().get(i);
        	
        	// wenn ordnername nicht in array ist
        	File[] subDirs = new File("mods/").listFiles(File::isDirectory);
        	
        	for (int j = 0; j < subDirs.length; j++) {
        		if (!Arrays.stream(ignorePath).anyMatch(subDirs[j]::equals)) {
			        // Get all the files in mod folder
			        File[] mods = new File("mod").listFiles();
			
			        for (int i=0; i<mods.length; i++) {
			            // Skip if the file is not a jar
			            if (!mods[i].getName().endsWith(".jar"))
			                continue;
			
			            // Create a JarFile
			            JarFile jarFile = new JarFile(mods[i]);
			
			            // Get the entries
			            Enumeration e = jarFile.entries();
			
			            // Create a URL for the jar
			            URL[] urls = { new URL("jar:file:" + mods[i].getAbsolutePath() +"!/") };
			            URLClassLoader cl = URLClassLoader.newInstance(urls); // auto added URLClassLoader type??
			
			            while (e.hasMoreElements()) {
			                JarEntry je = (JarEntry) e.nextElement();
			
			                // Skip directories
			                if(je.isDirectory() || !je.getName().endsWith(".class")) {
			                    continue;
			                }
			
			                // -6 because of .class
			                String className = je.getName().substring(0,je.getName().length()-6);
			                className = className.replace('/', '.');
			
			                // Load the class
			                Class c = cl.loadClass(className);
			            }
			        }
			    } else {
			    	LOGGER.info("Skipping " + subDirs[j] + " because of config");
			    }
		    }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("Server starting.");
    }

    /*
	public class CommonProxy {
		static String[] configStrings;

		private static void preInit(FMLCommonSetupEvent evt) {
			Configuration config = new Configuration(event.getSuggestedConfigurationFile());
			config.load();
			
			String comment = "Default subdirectories to be ignored:";
			
			String[] defaultIgnore = new String[3];
			
			defaultIgnore[0] = "ignore";
			defaultIgnore[1] = "*unstable*";
			defaultIgnore[2] = "*disable*";
			
			configStrings = config.getStringList("ConfigItemName", "ConfigCategoryName", defaultIgnore, comment);
				 
			config.save();
		}
	}
*/

/*

*/
}