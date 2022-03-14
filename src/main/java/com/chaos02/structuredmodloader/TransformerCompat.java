package com.chaos02.structuredmodloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import cpw.mods.modlauncher.api.TypesafeMap;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraftforge.fml.loading.LogMarkers;

@SuppressWarnings("rawtypes")
// extends ModDirTransformerDiscoverer implements ITransformerDiscoveryService
public class TransformerCompat implements ITransformationService {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	// Set Filesystem seperator char.
	private static final char FSC = File.separatorChar;
	
	public static File									modRoot			= SH.getModsDir();
	private ArgumentAcceptingOptionSpec<String>	mcOption;
	private ArgumentAcceptingOptionSpec<String>	mcpOption;
	public static boolean								wasConstructed	= false;
	
	public TransformerCompat() {
		LOGGER.info("[SML] {} loaded.", this.getClass().getSimpleName());
		SH.setMCVers(IEnvironment.Keys.VERSION.get().toString()); /* Should get overridden anyways */
		LOGGER.info(LogMarkers.CORE, "[SML] Registered Minecraft version {}", SH.getMCVers());
		wasConstructed = true;
	}
	
	// private final static List<NamedPath> found = new ArrayList<>();
	
	private static void scan(final Path gameDirectory) {
		
		if (Config.getLoadOnlyVersDir()) {
			modRoot = new File(SH.getModsDir() + String.valueOf(FSC) + SH.getMCVers());
		}
		if (modRoot.exists()) {
			LOGGER.info("Setting {} as modroot!", StructuredModLoader.relPath(modRoot, SH.getGameDir()));
		} else {
			LOGGER.error("./mods/{}/ folder doesn't exist! Ignoring config value!", SH.getMCVers());
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
				LOGGER.info(LogMarkers.SCAN, "Found transformation service(s) in \"{}\":", StructuredModLoader.relPath(path.toFile(), SH.getModsDir()));
				ArrayList<String> transformerList = new ArrayList<String>(new BufferedReader(
						new InputStreamReader(zf.getInputStream(zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService")), "UTF-8"))
								.lines().toList());
				
				ArrayList<ITransformer> transformers = SH.getTransformers();
				for (int i = 0; i < transformerList.size(); i++) {
					LOGGER.info(LogMarkers.SCAN, "\"{}\"", transformerList.get(i).toString());
					Class<?> TransClass = null;
					try {
						TransClass = Class.forName(transformerList.get(i));
						try {
							transformers.add((ITransformer) TransClass.newInstance());
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/* TODO THIS IS A MESS... (See decompiled optifine.OptiFineTransformationService.class) */
					// javadecompilers.com
				}
				SH.setTransformers(transformers);
				
			}
			zf.close();
		} catch (IOException ioe) {
			LogManager.getLogger().error("Zip Error when loading jar file {}", path, ioe);
		}
	}
	
	public static boolean getRan() {
		return wasConstructed;
	}
	
	public String name() {
		return "SMLTransformer";
	}
	
	public void initialize(IEnvironment environment) {
		// TODO *shrug*
		// At this point, FML is ready!
		SH.setGameDir(environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).get().toFile());
		LOGGER.debug(LogMarkers.LOADING, "[SML] Got gameDir: {}", SH.getGameDir());
		
		if (!SH.getMCVers().matches("(.?[0-9])+(-(alpha)|(beta))?")) {
			LOGGER.error(LogMarkers.CORE, "[SML] Trying to get MC version from FML:");
			SH.setMCVers(environment.getProperty(IEnvironment.Keys.VERSION.get()).orElseThrow(() -> new RuntimeException("[SML] GOT NO GAME VERSION!")));
		}
		
		LOGGER.debug(LogMarkers.CORE, "[SML] Registered Minecraft version {}", SH.getMCVers());
	}
	
	@SuppressWarnings("unchecked")
	public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
		Config.configProvider();
		// Get MCVersion from here:
		final Optional<String> mcVer = env.getProperty((TypesafeMap.Key) IEnvironment.Keys.VERSION.get());
		if (!mcVer.isEmpty())
			SH.setMCVers(mcVer.get());
		else {
			LOGGER.error(LogMarkers.CORE, "[SML] CANT GET MC VERSION!");
		}
	}
	
	public void arguments(BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
		/* TODO get proper usage from Optifine reference
		mcOption		= argumentBuilder.apply("mcVersion", "Minecraft Version number").withRequiredArg().ofType(String.class).required();
		mcpOption	= argumentBuilder.apply("mcpVersion", "MCP Version number").withRequiredArg().ofType(String.class).required();
		*/
	}
	
	public void argumentValues(OptionResult option) {
		/*
		SH.setMCVers(option.value(mcOption));
		SH.setMCPVers(option.value(mcpOption));
		*/
	}
	
	public List<ITransformer> transformers() {
		LOGGER.info(LogMarkers.SCAN, "Starting Transformer scan");
		scan(SH.getGameDir().toPath());
		return SH.getTransformers();
	}
	
}