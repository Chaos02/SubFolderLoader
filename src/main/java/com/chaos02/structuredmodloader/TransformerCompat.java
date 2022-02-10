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

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;

public class TransformerCompat extends ModDirTransformerDiscoverer implements ITransformerDiscoveryService {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = StructuredModLoader.LOGGER;
	// Set Filesystem seperator char.
	private static final char FSC = StructuredModLoader.FSC;
	// Get main class object
	private static final StructuredModLoader SML = new StructuredModLoader();
	
	// Constants
	final static File GAMEDIR = FMLPaths.GAMEDIR.get().toFile();
	final static File MODSDIR = FMLPaths.MODSDIR.get().toFile();
	
	// Variables
	public static List<NamedPath> transformers = new ArrayList<>();
	
	TransformerCompat() {
		LOGGER.info("Loaded SML TransformerDiscoveryService");
	}
	
	@Override
	public List<NamedPath> candidates(final Path gameDirectory) {
		scan(gameDirectory);
		return List.copyOf(found);
	}
	
	private final static List<NamedPath> found = new ArrayList<>();
	
	public static List<Path> allExcluded() {
		return found.stream().map(np -> np.paths()[0]).toList();
	}
	
	private static void scan(final Path gameDirectory) {
		
		SML.recurseLoader(MODSDIR, 0, transformers);
	}
	
	private static void visitFile(Path path) {
		if (!Files.isRegularFile(path))
			return;
		if (!path.toString().endsWith(".jar"))
			return;
		if (LamdbaExceptionUtils.uncheck(() -> Files.size(path)) == 0)
			return;
		try (ZipFile zf = new ZipFile(new File(path.toUri()))) {
			if (zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") != null) {
				found.add(new NamedPath(zf.getName(), path));
			} else if (zf.getEntry("META-INF/services/net.minecraftforge.forgespi.locating.IModLocator") != null) {
				found.add(new NamedPath(zf.getName(), path));
			}
		} catch (IOException ioe) {
			LogManager.getLogger().error("Zip Error when loading jar file {}", path, ioe);
		}
	}
	
}
