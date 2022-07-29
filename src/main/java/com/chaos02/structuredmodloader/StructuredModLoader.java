package com.chaos02.structuredmodloader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import net.minecraftforge.forgespi.locating.IModLocator;

@Mod("structuredmodloader")
public class StructuredModLoader extends AbstractJarFileModLocator implements IModLocator {
	// Directly reference a log4j logger.
	private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
	
	// Mod paths
	final static File CONFIGFILE = new File(FMLPaths.CONFIGDIR.get() + "\\StructuredModLoader.toml");
	final static List<String> DEFAULTDIRS = Arrays.asList("ignore", "unstable", "disable");
	final static int DEFAULTDEPTH = 3;
	
	final static File GAMEDIR = FMLPaths.GAMEDIR.get().toFile();
	final static File MODSDIR = FMLPaths.MODSDIR.get().toFile();
	public List<Path> mods;
	private List<String> ignoreWords = null;
	private int depth = 0;
	private CommentedFileConfig config = CommentedFileConfig.of(CONFIGFILE);
	ConfigSpec smlSpec = new ConfigSpec();
	
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
	
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		LOGGER.info(LogMarkers.LOADING, "Hello world from [SML]!!!");
		LOGGER.info(LogMarkers.SPLASH,
				"A HUGE THANK YOU! to stiebi99#2124 again because literally without him this mod would never exist!");
	}
	
	public class KeywordValidator {
		public boolean test(String toValidate) {
			return true;
			
		}
	}
	
	private static final Predicate<Object> KeywordValidator = s -> {
		return (s instanceof String);
	};
	
	public void configProvider() {
		LOGGER.info("Loading Config now! ({})", CONFIGFILE.toString());
		
		smlSpec.defineInRange("recurseDepth", 3, 0, 5);
		smlSpec.defineList("ignoredKeywords", DEFAULTDIRS, KeywordValidator);
		
		config.load();
		if (!smlSpec.isCorrect(config)) {
			CorrectionListener corrector = (action, path, incorrectValue, correctedValue) -> {
				String pathString = String.join(",", path);
				LOGGER.error("Corrected {}: was {}, is now {}", pathString, incorrectValue, correctedValue);
				
				config.setComment("recurseDepth", "How deep the folder structure inside the mods folder may be (max 5)");
				config.setComment("ignoredKeywords", "A folder containing one or more of these keywords will be skipped");
				config.save();
			};
			LOGGER.error("Config file is incorrect or missing, correcting!");
			int corrections = smlSpec.correct(config, corrector);
			LOGGER.info("Corrected {} errors.", corrections);
		}
		
		depth = config.get("recurseDepth");
		LOGGER.info("Loading subfolders that are NOT in config to a depth of {}!", depth);
		ignoreWords = config.get("ignoredKeywords");
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
			LOGGER.info(LogMarkers.SCAN, "Getting Files in \"{}\"", relPath(dir, MODSDIR));
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
	public Stream<Path> scanCandidates() {
		LOGGER.debug("SML.scanCandidates()");
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
		return mods.stream();
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