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
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.fml.loading.moddiscovery.ModJarMetadata;
import net.minecraftforge.fml.loading.moddiscovery.ExplodedDirectoryLocator.ExplodedMod;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.stream.Stream;

@Mod("structuredmodloader")
public class StructuredModLoader implements IModLocator {
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
    	/*
        LOGGER.info("StructuredModLoader installed!");
        LOGGER.info("Loading Config now!");
        
        ModLoadingContext.get().registerConfig(Type.COMMON, SMLConfig.SML_SPEC, "StructuredModLoader.toml");
        
        // Cast Config List to Array
    	String[] ignorePath = new String[SMLConfig.ignoreDir.get().size()];
    	for(int i = 0; i < SMLConfig.ignoreDir.get().size(); i++) ignorePath[i] = SMLConfig.ignoreDir.get().get(i);
    	
        LOGGER.info(LogMarkers.SCAN, "Ignoring >" + String.join(",", ignorePath) + "< keyworsds	");
        LOGGER.info(LogMarkers.SCAN, "Loading subfolders that are NOT in config!");
        */
    	LOGGER.info(LogMarkers.SCAN, "Successfully recursively loaded {}", String.join(", ", ownOtherMods.toArray().toString()));
    }
    
    
    List<Path> ownOtherMods;
    
    private void recurseJarLoader(File dir) {
    	
    	if (dir != FMLPaths.MODSDIR.get().toFile()) {
    		LOGGER.info(LogMarkers.SCAN, "Getting Files in {}", dir.toString());
	    	File[] subFiles = dir.listFiles(File::isFile);
	    	for (int i2 = 0; i2 < subFiles.length; i2++) {
	    		if (subFiles[i2].toString().toLowerCase().endsWith(".jar")) {
	    			ownOtherMods.add(subFiles[i2].toPath());
	    		} else {
	    			LOGGER.info(LogMarkers.SCAN, "Skipped {} because of file extension", subFiles[i2].toString());
	    		}
	    	}
	    	LOGGER.info(LogMarkers.SCAN, "Finished loading {}", dir.toString());
    	} else {
    		LOGGER.info(LogMarkers.SCAN, "Started at {}", dir.toString());
    	}
    	
    	File[] subDirs = dir.listFiles(File::isDirectory);
    	if (subDirs.length != 0) {
	    	for (int j = 0 ; j < subDirs.length; j++) {
	    		if (!SMLConfig.ignoreDir.get().contains(subDirs[j].toString())) {
	    			LOGGER.info(LogMarkers.SCAN, "Searching for mods in >" + subDirs[j] + "<");
			        recurseJarLoader(subDirs[j]);
	    		} else {
			    	LOGGER.info(LogMarkers.SCAN, "Skipping >" + subDirs[j] + "< because of config");
			    }
		    }
    	} else {
    		LOGGER.info(LogMarkers.SCAN, ">" + dir + "< contains no sub directories.");
    	}
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.debug("[SML] Server starting.");
    }
    
    public Stream<Path> scanCandidates() {
    	
    	LOGGER.info("StructuredModLoader installed!");
        LOGGER.info("Loading Config now!");
        
        ModLoadingContext.get().registerConfig(Type.COMMON, SMLConfig.SML_SPEC, "StructuredModLoader.toml");
        
        // Cast Config List to Array
    	String[] ignorePath = new String[SMLConfig.ignoreDir.get().size()];
    	for(int i = 0; i < SMLConfig.ignoreDir.get().size(); i++) ignorePath[i] = SMLConfig.ignoreDir.get().get(i);
    	
        LOGGER.info("Ignoring >" + String.join(", ", ignorePath) + "< keyworsds");
        LOGGER.info("Loading subfolders that are NOT in config!");
    
    	LOGGER.info(LogMarkers.SCAN, "Initiating recursive directory search:");
        recurseJarLoader(FMLPaths.MODSDIR.get().toFile());
    	
    	return ownOtherMods.stream();
    }
    
    //@Override
    public List<IModFile> scanMods() {
		return null;
    	/*LOGGER.error("StructuredModLoader.scanMods()");
    	
        try {
			return AbstractJarFileLocator.scanMods();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    }

    @Override
    public String name() {
        return "[SML]StructuredModLoader";
    }

    public void initArguments(final Map<String, ?> arguments) { // unused
    }

	@Override
	public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
		LOGGER.error("StructuredModLoader.scanFile()");
	}

	@Override
	public boolean isValid(IModFile modFile) {
		LOGGER.error("StructuredModLoader.isValid()");
		return true;
	}

}
