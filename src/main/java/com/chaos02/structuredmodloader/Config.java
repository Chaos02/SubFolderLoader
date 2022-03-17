package com.chaos02.structuredmodloader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public class Config {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	// Set Filesystem seperator char.
	private static final char FSC = File.separatorChar;
	
	/*                   Get Main class constants                       */
	// Mod paths
	private final static Path				CONFIGFILE					= StructuredModLoader.CONFIGFILE;
	private final static List<String>	DEFAULTDIRS					= StructuredModLoader.DEFAULTDIRS;
	private final static int				DEFAULTDEPTH				= StructuredModLoader.DEFAULTDEPTH;
	private final static int				MAXDEPTH						= StructuredModLoader.MAXDEPTH;
	private final static boolean			DEFAULTVERSIONDIRONLY	= StructuredModLoader.DEFAULTVERSIONDIRONLY;
	
	private static Path GAMEDIR = null;
	
	// Variables
	final static char[] badChars = { FSC };
	
	// Config Value Variables
	public static List<String>	ignoreWords			= null;
	public static boolean		loadOnlyVersDir	= false;
	public static int				depth					= 0;
	
	public static List<String> getIgnoreWords() {
		return ignoreWords;
	}
	
	public static boolean getLoadOnlyVersDir() {
		return loadOnlyVersDir;
	}
	
	public static int getDepth() {
		return depth;
	}
	
	// Config init
	private static CommentedFileConfig	config	= CommentedFileConfig.of(CONFIGFILE);
	static ConfigSpec							smlSpec	= new ConfigSpec();
	
	private static final Predicate<Object> KeywordValidator = s -> {
		return ((s instanceof String) && (!StringUtils.containsAnyIgnoreCase(s.toString(), badChars.toString())));
	};
	
	public static void configProvider() {
		GAMEDIR = Paths.get(SH.getGameDir().toString());
		LOGGER.debug("[SML] Loading Config now! ({})", StructuredModLoader.relPath(CONFIGFILE, GAMEDIR));
		
		smlSpec.defineInRange("recurseDepth", DEFAULTDEPTH, 0, MAXDEPTH);
		smlSpec.defineList("ignoredKeywords", DEFAULTDIRS, KeywordValidator);
		smlSpec.define("loadOnlyVersionFolders", DEFAULTVERSIONDIRONLY);
		
		config.load();
		if (!smlSpec.isCorrect(config)) {
			CorrectionListener corrector = (action, path, incorrectValue, correctedValue) -> {
				String pathString = String.join(",", path);
				LOGGER.error("Corrected {}: was {}, is now {}", pathString, incorrectValue, correctedValue);
				config.save();
			};
			LOGGER.error("Config file is incorrect or missing, correcting!");
			int corrections = smlSpec.correct(config, corrector);
			LOGGER.info("Corrected {} errors.", corrections);
		}
		config.setComment("recurseDepth", "How deep the folder structure inside the mods folder may be (max 5)");
		config.setComment("ignoredKeywords", "A folder containing one or more of these keywords will be skipped");
		config.setComment("loadOnlyVersionFolders", "Wether to set the MC version as the initial directory to scan");
		config.save();
		
		loadOnlyVersDir = config.get("loadOnlyVersionFolders");
		if (loadOnlyVersDir) {
			LOGGER.info("[SML] Attempting to load only MC version directory!");
		}
		
		depth = config.get("recurseDepth");
		LOGGER.info("[SML] Loading subfolders that are NOT in config to a depth of {}!", depth);
		ignoreWords = config.get("ignoredKeywords");
		if (ignoreWords == null) {
			// TODO why ignoreWords == null??? fixed?
			LOGGER.error("ignoreWords empty blyat ({})", ignoreWords.toString());
			ignoreWords = DEFAULTDIRS;
		}
		ignoreWords.removeAll(Arrays.asList("", null)); // removes empty fields
		LOGGER.info("[SML] Ignoring keywords: \"{}\"", String.join(", ", ignoreWords));
		
	}
}