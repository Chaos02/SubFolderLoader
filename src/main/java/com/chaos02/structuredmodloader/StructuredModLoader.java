package com.chaos02.structuredmodloader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import com.electronwill.nightconfig.core.file.FileConfig;

import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.forgespi.locating.IModLocator;

@Mod("structuredmodloader")
public class StructuredModLoader extends AbstractJarFileLocator implements IModLocator {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	// Mod paths
	final static File CONFIGFILE = new File(FMLPaths.CONFIGDIR + "StructuredModLoader.toml");
	final static List<String> DEFAULTDIRS = Arrays.asList("ignore", "unstable", "disable", "");
	final static int DEFAULTDEPTH = 3;
	
	private static final Predicate<Object> KeywordValidator = s -> s instanceof String;
	public List<Path> mods;
	private List<String> ignoreWords = null;
	private int depth = 0;
	private FileConfig config = FileConfig.of(CONFIGFILE);
	ConfigSpec smlSpec = new ConfigSpec();
	
	public StructuredModLoader() {
		mods = new ArrayList<>();
	}
	
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		
	}
	
	public class KeywordValidator {
		public boolean test(String toValidate) {
			return true;
			
		}
	}
	
	public void configProvider() {
		LOGGER.info("Loading Config now!");
		
		smlSpec.defineInRange("recurseDepth", 3, 0, 5);
		smlSpec.defineList("ignoreKeywords", DEFAULTDIRS, KeywordValidator);
		
		config.load();
		if (!smlSpec.isCorrect(config)) {
			CorrectionListener corrector = (action, path, incorrectValue, correctedValue) -> {
				String pathString = String.join(",", path);
				LOGGER.error("Config file is incorrect, correcting!");
				LOGGER.error("Corrected {}: was {}, is now {}", pathString, incorrectValue, correctedValue);
				config.save();
			};
			int corrections = smlSpec.correct(config, corrector);
			LOGGER.info("Corrected {} errors.", corrections);
		}
		
		depth = config.get("recurseDepth");
		ignoreWords = config.get("ignoredKeyWords");
		
		ignoreWords.removeAll(Arrays.asList("", null)); // removes empty fields
		
		LOGGER.info("Ignoring >" + String.join(",", ignoreWords) + "< keyworsds	");
		LOGGER.info("Loading subfolders that are NOT in config!");
		
	}
	
	private void recurseJarLoader(File dir, int depth) {
		
		if (dir != FMLPaths.MODSDIR.get().toFile()) {
			LOGGER.info("Getting Files in {}", dir.toString());
			File[] subFiles = dir.listFiles(File::isFile);
			for (int i2 = 0; i2 < subFiles.length; i2++) {
				if (subFiles[i2].toString().toLowerCase().endsWith(".jar")) {
					mods.add(subFiles[i2].toPath());
				} else {
					LOGGER.info("Skipped {} because of file extension", subFiles[i2].toString());
				}
			}
			LOGGER.info("Finished loading {}", dir.toString());
		} else {
			LOGGER.info("Started at {}", dir.toString());
		}
		
		File[] subDirs = dir.listFiles(File::isDirectory);
		if (subDirs.length > 0) {
			if (depth >= 0) {
				for (int j = 0; j < subDirs.length; j++) {
					if (!ignoreWords.stream().anyMatch(subDirs[j].toString()::contains)) {
						LOGGER.info("Searching for mods in >" + subDirs[j] + "<");
						recurseJarLoader(subDirs[j], depth - 1);
					} else {
						LOGGER.info("Skipping \"{}\" because of config", subDirs[j]);
					}
				}
			} else {
				LOGGER.error("Skipping subdirectories of \"{}\". Please rearrange or configure higher depth!", dir);
			}
		} else {
			LOGGER.debug("\"{}\" contains no sub directories.", dir);
		}
		
	}
	
	// Implements
	
	@Override
	public Stream<Path> scanCandidates() {
		LOGGER.debug("SML.scanCandidates()");
		LOGGER.info("Structured Mod Loader installed!");
		configProvider();
		recurseJarLoader(new File(FMLPaths.MODSDIR.toString()), depth);
		
		LOGGER.info("Successfully recursively loaded:");
		String modlist = "";
		for (int i = 0; i < mods.size(); i++) {
			modlist += mods.get(i).toAbsolutePath().getName(mods.get(i).toAbsolutePath().getNameCount() - 1) + "\n";
		}
		LOGGER.info(modlist);
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