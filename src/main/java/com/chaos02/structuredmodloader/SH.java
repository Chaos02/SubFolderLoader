package com.chaos02.structuredmodloader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.modlauncher.api.ITransformer;
import net.minecraftforge.fml.loading.LogMarkers;

public class SH {
	// Directly reference a log4j logger.
	private static final Logger	LOGGER	= LogManager.getLogger();
	private static final char		FSC		= File.separatorChar;
	
	// Constants
	private static Path	gameDir	= null;
	private static Path	modsDir	= null;
	
	// Variables
	@SuppressWarnings("rawtypes")
	private static ArrayList<ITransformer>	transformers	= new ArrayList<>();
	private static List<Path>					mods				= new ArrayList<>();
	private static Path							modRoot			= modsDir;
	private static String						MCVers			= "ERROR";
	private static String						MCPVers			= "ERROR";
	
	public SH() {
		LOGGER.debug(LogMarkers.LOADING, "[SML] Initialized Variable storage.");
	}
	
	public static Path getGameDir() {
		return gameDir;
	}
	
	public static void setGameDir(Path path) {
		gameDir = path;
	}
	
	public static Path getModsDir() {
		return modsDir;
	}
	
	public static void setModsDir(Path mODSDIR) {
		modsDir = mODSDIR;
	}
	
	@SuppressWarnings("rawtypes")
	public static ArrayList<ITransformer> getTransformers() {
		return transformers;
	}
	
	@SuppressWarnings("rawtypes")
	public static void setTransformers(ArrayList<ITransformer> transformers) {
		SH.transformers = transformers;
	}
	
	public static List<Path> getMods() {
		return mods;
	}
	
	public static void setMods(List<Path> mods) {
		SH.mods = mods;
	}
	
	public static Path getModRoot() {
		return modRoot;
	}
	
	public static void setModRoot(Path modRoot) {
		SH.modRoot = modRoot;
	}
	
	public static String getMCVers() {
		return MCVers;
	}
	
	public static void setMCVers(String mCVers) {
		MCVers = mCVers;
	}
	
	public static String getMCPVers() {
		return MCPVers;
	}
	
	public static void setMCPVers(String mCPVers) {
		MCPVers = mCPVers;
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
}
