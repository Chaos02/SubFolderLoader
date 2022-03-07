package com.chaos02.structuredmodloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LogMarkers;

// extends ModDirTransformerDiscoverer implements ITransformerDiscoveryService
public class TransformerCompat implements ITransformationService {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = StructuredModLoader.LOGGER;
	// Set Filesystem seperator char.
	private static final char FSC = File.separatorChar;
	
	// Constants
	private final static File GAMEDIR = new File(IEnvironment.Keys.GAMEDIR.get().toString());
	private static File MODSDIR = new File(GAMEDIR.toString() + String.valueOf(FSC) + FMLPaths.MODSDIR.toString());
	
	// Variables
	@SuppressWarnings("rawtypes")
	public static ArrayList<ITransformer> transformers = new ArrayList<>();
	public static List<Path> mods = new ArrayList<>();
	public static File modRoot = MODSDIR;
	public static String MCVers = "ERROR";
	public static String MCPVers = "ERROR";
	private ArgumentAcceptingOptionSpec<String> mcOption;
	private ArgumentAcceptingOptionSpec<String> mcpOption;
	public static boolean wasConstructed = false;
	
	TransformerCompat() {
		LOGGER.info("Loaded SML TransformerDiscoveryService");
		MCVers = IEnvironment.Keys.VERSION.get().toString(); /* Should get overriden anyways */
		LOGGER.info(LogMarkers.CORE, "[SML] Registered Minecraft version {}", MCVers);
		wasConstructed = true;
	}
	
	@SuppressWarnings("rawtypes")
	public static List<ITransformer> getTransformers() {
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
	
	// private final static List<NamedPath> found = new ArrayList<>();
	
	private static void scan(final Path gameDirectory) {
		
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
			StructuredModLoader.recurseLoader(modRoot, Config.getIgnoreWords(), Config.getDepth(), "transformer");
		} catch (IOException e) {
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
				// read file contents, add class in contents to list.
				LOGGER.info(LogMarkers.SCAN, "Found transformation service(s) in \"{}\":", StructuredModLoader.relPath(path.toFile(), MODSDIR));
				ArrayList<String> transformerList = new ArrayList<String>(new BufferedReader(
						new InputStreamReader(zf.getInputStream(zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService")), "UTF-8"))
								.lines().toList());
				for (int i; i < transformerList.size(); i++) {
					LOGGER.info(LogMarkers.SCAN, "\"{}\"", transformerList.get(i).toString());
					transformers.add(Class.forName(transformerList.get(i)));
					/* TODO THIS IS A MESS... (See decompiled optifine.OptiFineTransformationService.class) */
					// javadecompilers.com
				}
				
			}
			zf.close();
		} catch (IOException ioe) {
			LogManager.getLogger().error("Zip Error when loading jar file {}", path, ioe);
		}
	}
	
	public static boolean getRan() {
		return wasConstructed;
	}
	
	@Override
	public String name() {
		return "structuredmodmoader";
	}
	
	@Override
	public void initialize(IEnvironment environment) {
		// TODO *shrug*
	}
	
	@Override
	public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
		// TODO There should be no incompatibilities...?
		
	}
	
	@Override
	public void arguments(BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
		mcOption = argumentBuilder.apply("mcVersion", "Minecraft Version number").withRequiredArg().ofType(String.class).required();
		mcpOption = argumentBuilder.apply("mcpVersion", "MCP Version number").withRequiredArg().ofType(String.class).required();
	}
	
	@Override
	public void argumentValues(OptionResult option) {
		MCVers = option.value(mcOption);
		MCPVers = option.value(mcpOption);
	}
	
	@SuppressWarnings("rawtypes")
	public List<ITransformer> transformers() {
		LOGGER.info(LogMarkers.SCAN, "Starting Transformer scan");
		scan(GAMEDIR.toPath());
		transformers.add((ITransformer) transformers);
		return transformers;
	}
	
}