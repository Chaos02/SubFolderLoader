package com.chaos02.structuredmodloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;

// extends ModDirTransformerDiscoverer
public class TransformerCompat extends ModDirTransformerDiscoverer implements ITransformerDiscoveryService {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = StructuredModLoader.LOGGER;
	// Set Filesystem seperator char.
	private static final char FSC = File.separatorChar;
	
	// Constants
	private final static File GAMEDIR = new File(IEnvironment.Keys.GAMEDIR.get().toString());
	static File MODSDIR = null;
	
	// Variables
	public static List<NamedPath> transformers = new ArrayList<>();
	public static List<Path> mods = new ArrayList<>();
	public static File modRoot = MODSDIR;
	public static String MCVers = "ERROR";
	
	TransformerCompat() {
		LOGGER.info("Loaded SML TransformerDiscoveryService");
		MCVers = IEnvironment.Keys.VERSION.get().toString();
		LOGGER.info(LogMarkers.CORE, "[SML] Registered Minecraft version {}", MCVers);
	}
	
	public static List<NamedPath> getTransformers() {
		return transformers;
	}
	
	public static List<Path> getMods() {
		return mods;
	}
	
	public static void setMods(List<Path> mods) {
		TransformerCompat.mods = mods;
	}
	
	public static File getModRoot() {
		return modRoot;
	}
	
	public static String getMCVers() {
		return MCVers;
	}
	
	public void setMCVers(String MCVers) {
		TransformerCompat.MCVers = MCVers;
	}
	
	@Override
	public List<NamedPath> candidates(final Path gameDirectory) {
		scan(gameDirectory);
		return transformers;
	}
	
	// private final static List<NamedPath> found = new ArrayList<>();
	
	public static List<Path> allExcluded() {
		return transformers.stream().map(np -> np.paths()[0]).toList();
	}
	
	private static void scan(final Path gameDirectory) {
		
		if (!MODSDIR.exists()) {
			// Default to /mods
			// TODO Try get actual mods dir?
			MODSDIR = new File(GAMEDIR + String.valueOf(FSC) + "mods");
		}
		
		Config.configProvider();
		if (Config.getLoadOnlyVersDir()) {
			modRoot = new File(MODSDIR + String.valueOf(FSC) + MCVers);
		}
		if (modRoot.exists()) {
			LOGGER.info("Setting {} as modroot!", StructuredModLoader.relPath(modRoot, GAMEDIR));
		} else {
			LOGGER.error("{} mods folder doesn't exist! Ignoring config value!", MCVers);
		}
		
		try {
			StructuredModLoader.recurseLoader(modRoot, Config.getIgnoreWords(), Config.getDepth(), "transformers");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void visitFile(Path path) {
		if (!Files.isRegularFile(path))
			return;
		if (!path.toString().endsWith(".jar"))
			return;
		if (LamdbaExceptionUtils.uncheck(() -> Files.size(path)) == 0)
			return;
		try (ZipFile zf = new ZipFile(new File(path.toUri()))) {
			if (zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") != null) {
				transformers.add(new NamedPath(zf.getName(), path));
			} else if (zf.getEntry("META-INF/services/net.minecraftforge.forgespi.locating.IModLocator") != null) {
				transformers.add(new NamedPath(zf.getName(), path));
			}
		} catch (IOException ioe) {
			LogManager.getLogger().error("Zip Error when loading jar file {}", path, ioe);
		}
	}
	
}
