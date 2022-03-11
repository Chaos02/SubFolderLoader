package com.chaos02.structuredmodloader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.modlauncher.api.NamedPath;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.forgespi.locating.IModLocator;

public class StructuredModLoader extends AbstractJarFileLocator implements IModLocator {
	// Directly reference a log4j logger.
	static final Logger LOGGER = LogManager.getLogger();
	// Set Filesystem seperator char.
	static final char FSC = File.separatorChar;
	
	// Paths
	final static File				CONFIGFILE					= new File(FMLPaths.CONFIGDIR.get() + String.valueOf(FSC) + "StructuredModLoader.toml");
	final static List<String>	DEFAULTDIRS					= Arrays.asList("ignore", "unstable", "disable");
	final static int				DEFAULTDEPTH				= 3;
	final static int				MAXDEPTH						= 5;
	final static boolean			DEFAULTVERSIONDIRONLY	= false;
	final static File				GAMEDIR						= FMLPaths.GAMEDIR.get().toFile();
	final static File				MODSDIR						= FMLPaths.MODSDIR.get().toFile();
	
	// Variables
	public static List<NamedPath>	transformers	= new ArrayList<>();
	public static List<Path>		mods				= new ArrayList<>();
	static File							modRoot			= MODSDIR;
	static String						MCVers			= "ERROR";
	
	public StructuredModLoader() {
		LOGGER.info("[SML] {} loaded.", this.getClass().getSimpleName());
		if (!TransformerCompat.getRan()) {
			new RuntimeException("SML Init didn't run during transformer setup!");
		}
		
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
	
	/*
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		LOGGER.info(LogMarkers.LOADING, "Hello world from [SML]!!!");
		LOGGER.info(LogMarkers.SPLASH,
				"A HUGE THANK YOU! to stiebi99#2124 again because literally without him this mod would never exist!");
	}
	*/
	
	public static String relPath(File file, File root) {
		return file.toString().substring(root.toString().length());
	}
	
	/**
	 * 
	 * @param dir   Directory to recurse through
	 * @param depth Limits recursion.
	 * @param mode  transformer or forgemod
	 * @throws IOException When File mysteriously disappeared
	 */
	public static void recurseLoader(File dir, List<String> ignoreWords, int depth, String mode) throws IOException {
		if (dir != MODSDIR) {
			LOGGER.info(LogMarkers.SCAN, "Getting Files in \"{}\"", relPath(dir, MODSDIR));
			File[] subFiles = dir.listFiles(File::isFile);
			for (int i2 = 0; i2 < subFiles.length; i2++) {
				if (subFiles[i2].toString().toLowerCase().endsWith(".jar")) {
					LOGGER.info("Found \"{}\"", relPath(subFiles[i2], MODSDIR));
					/* if (LamdbaExceptionUtils.uncheck(() -> Files.size(subFiles[i2].toPath())) == 0) { */
					if (Files.size(subFiles[i2].toPath()) == 0) {
						LOGGER.info("\"{}\": size == 0", relPath(subFiles[i2], MODSDIR));
					} else {
						
						switch (mode.toLowerCase()) {
							case "transformer":
								// Add to Transformer list
								
								TransformerCompat.visitFile(subFiles[i2].toPath());
								
								// TODO Translate from lambda to normal?
								/* if (LamdbaExceptionUtils.uncheck(Files.size(subFiles[i2].toPath()) == 0) return; */
								/*
								try (ZipFile zf = new ZipFile(new File(subFiles[i2].toURI()))) {
									if (zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") != null) {
										LOGGER.info(LogMarkers.SCAN, "Adding \"{}\" as a Transformer.", subFiles[i2]);
										transformers.add(new NamedPath(zf.getName(), subFiles[i2].toPath()));
									} else if (zf.getEntry("META-INF/services/net.minecraftforge.forgespi.locating.IModLocator") != null) {
										LOGGER.info(LogMarkers.SCAN, "Adding \"{}\" as a ModLocator.", subFiles[i2]);
										transformers.add(new NamedPath(zf.getName(), subFiles[i2].toPath()));
									}
								} catch (IOException ioe) {
									LOGGER.error("Zip Error when loading jar file {}\n{}", subFiles[i2], ioe);
								}
								*/
								
								break;
							case "forgemod":
								
								// Add to Forge mod list
								mods.add(subFiles[i2].toPath());
								
								break;
							default:
								LOGGER.error("WRONG MODE ARGUMENT AT \"recurseLoader({},{},{})\"!", dir, depth, mode);
								break;
						}
					}
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
					if (!Config.getIgnoreWords().stream().anyMatch(subDirs[j].toString()::contains)) {
						LOGGER.info("Searching for mods in \"{}\"", relPath(subDirs[j], MODSDIR));
						recurseLoader(subDirs[j], ignoreWords, depth - 1, mode);
					} else {
						LOGGER.info("Skipping \"{}\" because of config", relPath(subDirs[j], MODSDIR));
					}
				}
			} else {
				LOGGER.error("Skipping subdirectories of \"{}\". Please rearrange or configure higher depth!", relPath(dir, MODSDIR));
			}
		} else {
			LOGGER.info("\"{}\" contains no sub directories.", relPath(dir, MODSDIR));
		}
		
	}
	
	// Implements
	
	// to disable normal mod scanning:
	/*
	 @Override ModFolderLocator.
	 public Stream<Path> scanCandidates() {
	     LOGGER.debug(LogMarkers.SCAN,"Scanning mods dir {} for mods", this.modFolder);
	     var excluded = ModDirTransformerDiscoverer.allExcluded();
	
	     return uncheck(()-> Files.list(this.modFolder))
	             .filter(p-> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX))
	             .sorted(Comparator.comparing(path-> StringUtils.toLowerCase(path.getFileName().toString())));
	 } 
	 
	 */
	
	//
	
	@Override
	public Stream<Path> scanCandidates() {
		// LOGGER.debug("SML.scanCandidates()");
		LOGGER.info("Structured Mod Loader installed!");
		// Config.configProvider();
		if (!TransformerCompat.getRan()) {
			LOGGER.error(LogMarkers.LOADING, "TRANSFORMER LOCATOR DIDNT RUN! Error WILL arise!");
		}
		
		modRoot = SH.getModRoot();
		if (modRoot.exists()) {
			LOGGER.info("Setting {} as modroot!", relPath(modRoot, GAMEDIR));
		} else {
			LOGGER.error("{} mods folder doesn't exist! Ignoring config value!", MCVers);
		}
		
		String SMLstr = null;
		try {
			SMLstr = java.net.URLDecoder.decode(StructuredModLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
					StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// not going to happen - value came from JDK's own StandardCharsets
		}
		File SMLJAR = new File(SMLstr.substring(0, SMLstr.length() - 5));
		// mods.add(SMLJAR.toPath());
		if (mods.contains(SMLJAR.toPath())) {
			LOGGER.info("Added SML to Forge mod list. ({})", relPath(SMLJAR, GAMEDIR));
		}
		try {
			recurseLoader(modRoot, Config.getIgnoreWords(), Config.getDepth(), "forgemod");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		LOGGER.info("Successfully recursively loaded:");
		String modlist = "";
		for (int i = 0; i < mods.size(); i++) {
			modlist += mods.get(i).toAbsolutePath().getName(mods.get(i).toAbsolutePath().getNameCount() - 1) + "\n";
		}
		LOGGER.info(modlist);
		*/
		SH.setMods(mods);
		return mods.stream();
	}
	
	@Override
	public String name() {
		return "[SML]StructuredModLoader";
	}
	
	@Override
	public void initArguments(Map<String, ?> arguments) {
		// LOGGER.debug("SML.initArguments({})", arguments);
		// MCVers = arguments.get("mcVersion").toString();
		MCVers = SH.getMCVers();
	}
	
}