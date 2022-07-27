package com.chaos02.structuredmodloader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaos02.structuredmodloader.SMLCore.SMLCore;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.discovery.ITypeDiscoverer;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.*;

@Mod(modid = "structuredmodloader", name = "StructuredModLoader", version = "1.0")
public  class StructuredModLoader extends JarDiscoverer implements ITypeDiscoverer {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	// Mod paths
	public File MODDIR = SMLCore.mcLocation;
	
	final static List<String> DEFAULTDIRS = Arrays.asList("ignore", "unstable", "disable");
	final static int DEFAULTDEPTH = 3;
	
	
	
	public List<Path> mods;
	private List<String> ignoreWords = null;
	private int depth = 0;

	
	public StructuredModLoader() {
		mods = new ArrayList<>();
		
		/* This doesnt work because of java.lang.NoClassDefFoundError: net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext */
		// Register the setup method for modloading
		// FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		// FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		// FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		
		// Register ourselves for server and other game events we are interested
		// in
		// MinecraftForge.EVENT_BUS.register(this);
	}
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		// some preinit code
		LOGGER.info("Hello world from [SML]!!!");
		LOGGER.info("A HUGE THANK YOU! to stiebi99#2124 again because literally without him this mod would never exist!");
	}
	
	public class KeywordValidator {
		public boolean test(String toValidate) {
			return true;
			
		}
	}
	
	private static final Predicate<Object> KeywordValidator = s -> {
		return (s instanceof String);
	};
	
	/**
	 * Config adaptation using the 1.12.2 config system because it should be available earlier
	 * 
	 */
	@Config(modid = "structuredmodloader", name = "StructuredModLoader", category = "")
	@RequiresMcRestart
	public class legacyConfig {
		@Comment({
			"How deep the folder structure inside the mods folder may be (max 5)"
		})
		@RangeInt(min = 0, max = 5)
		public static int recurseDepth = 3;
		
		@Comment({
			"A folder containing one or more of these keywords will be skipped"
		})
		public static List<String> ignoredKeywords = DEFAULTDIRS;
	}
	
	public void configProvider() {
		LOGGER.info("Loading Config now! ({})", "<>");
		
		depth = legacyConfig.recurseDepth;
		LOGGER.info("Loading subfolders that are NOT in config to a depth of {}!", depth);
		ignoreWords = legacyConfig.ignoredKeywords;
		if (ignoreWords == null) {
			// TODO why ignoreWords == null??? fixed?
			LOGGER.error("ignoreWords empty blyat ({})", ignoreWords.toString());
			ignoreWords = DEFAULTDIRS;
		}
		ignoreWords.removeAll(Arrays.asList("", null)); // removes empty fields
		LOGGER.info("Ignoring keywords: \"{}\"", String.join(", ", ignoreWords));
		
	}
	
	private String relPath(File file, File root) {
		return file.toString().substring(root.toString().length());
	}
	
	private void recurseJarLoader(File dir, int depth) {
		
		if (dir != MODSDIR) {
			LOGGER.info("Getting Files in \"{}\"", relPath(dir, MODSDIR));
			File[] subFiles = dir.listFiles(File::isFile);
			for (int i2 = 0; i2 < subFiles.length; i2++) {
				if (subFiles[i2].toString().toLowerCase().endsWith(".jar")) {
					LOGGER.info("Found \"{}\"", relPath(subFiles[i2], MODSDIR));
					mods.add(subFiles[i2].toPath());
				} else {
					LOGGER.error("Skipped \"{}\" because of file extension", subFiles[i2].getName());
				}
			}
			// LOGGER.info("Finished loading \"{}\"", relPath(dir, MODSDIR));
		} else {
			LOGGER.info("Skipping root {}", relPath(dir, GAMEDIR)); /* usually no path seen */
		}
		File[] subDirs = dir.listFiles(File::isDirectory);
		if (subDirs.length > 0) {
			if (depth >= 0) {
				for (int j = 0; j < subDirs.length; j++) {
					if (!ignoreWords.stream().anyMatch(subDirs[j].toString()::contains)) {
						LOGGER.info("Searching for mods in \"{}\"", relPath(subDirs[j], MODSDIR));
						recurseJarLoader(subDirs[j], depth - 1);
					} else {
						LOGGER.info("Skipping \"{}\" because of config", relPath(subDirs[j], MODSDIR));
					}
				}
			} else {
				LOGGER.error("Skipping subdirectories of \"{}\". Please rearrange or configure higher depth!",
						relPath(dir, MODSDIR));
			}
		} else {
			LOGGER.debug("\"{}\" contains no sub directories.", relPath(dir, MODSDIR));
		}
		
	}
	
	// Implements
	
	@Override
	public List<IModFile> scanMods() {
		LOGGER.debug("SML.scanMods()");
		LOGGER.info("Structured Mod Loader installed!");
		configProvider();
		recurseJarLoader(MODSDIR, depth);
		/*
		LOGGER.info("Successfully recursively loaded:");
		String modlist = "";
		for (int i = 0; i < mods.size(); i++) {
			modlist += mods.get(i).toAbsolutePath().getName(mods.get(i).toAbsolutePath().getNameCount() - 1) + "\n";
		}
		LOGGER.info(modlist);
		*/
		
		// Source: https://github.com/MinecraftForge/MinecraftForge/blob/1.16.x/src/fmllauncher/java/net/minecraftforge/fml/loading/moddiscovery/ModsFolderLocator.java#L47
		return mods.stream().map(p->ModFile.newFMLInstance(p, this))
				.peek(f->modJars.compute(f, (mf, fs)->createFileSystem(mf)))
				.collect(Collectors.toList());
	}
	
	@Override
	public String name() {
		return "[SML]StructuredModLoader";
	}
	
	@Override
	public void initArguments(Map<String, ?> arguments) {
		// TODO Auto-generated method stub
		LOGGER.debug("SML.initArguments({})", arguments);
	}

	
}