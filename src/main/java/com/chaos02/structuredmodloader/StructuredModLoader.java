package com.chaos02.structuredmodloader;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("structuredmodloader")
public class StructuredModLoader extends net.minecraftforge.fml.loading.moddiscovery.MinecraftLocator {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public StructuredModLoader() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("StructuredModLoader installed!");
        LOGGER.info("Loading Config now!");
        
        ModLoadingContext.get().registerConfig(Type.COMMON, SMLConfig.SML_SPEC, "StructuredModLoader.toml");
        
        // Cast Config List to Array
    	String[] ignorePath = new String[SMLConfig.ignoreDir.get().size()];
    	for(int i = 0; i < SMLConfig.ignoreDir.get().size(); i++) ignorePath[i] = SMLConfig.ignoreDir.get().get(i);
    	
        LOGGER.info("Ignoring >" + ignorePath.toString() + "< keyworsds	");
        
        LOGGER.info("Loading subfolders that are NOT in config!");
        
        recurseJarLoader(FMLPaths.MODSDIR.get().toFile());

    }
    
    public List<IModFile> ownOtherMods;
    
    private void jarLoader(File file) {
    	ownOtherMods.add((IModFile) file);
    }
    
    @Override
    public List<IModFile> scanMods() {
    	List<IModFile> artifacts = super.scanMods();
    	artifacts.addAll(ownOtherMods);
    	return artifacts;
    }
    
    private void recurseJarLoader(File dir) {
    	
    	if (dir != FMLPaths.MODSDIR.get().toFile()) {
	    	File[] subFiles = dir.listFiles(File::isFile);
	    	for (int i2 = 0; i2 < subFiles.length; i2++) {
	    		jarLoader(subFiles[i2]);
	    	}
    	}
    	
    	File[] subDirs = dir.listFiles(File::isDirectory);
    	if (subDirs.length != 0) {
	    	for (int j = 0 ; j < subDirs.length; j++) {
	    		if (!SMLConfig.ignoreDir.get().contains(subDirs[j].toString())) {
	    			LOGGER.info("Searching for mods in >" + subDirs[j] + "<");
			        recurseJarLoader(subDirs[j]);
	    		} else {
			    	LOGGER.info("Skipping >" + subDirs[j] + "< because of config");
			    }
		    }
    	} else {
    		LOGGER.debug(">" + dir + "< contains no sub directories.");
    	}
    }
        	

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("Server starting.");
    }

}