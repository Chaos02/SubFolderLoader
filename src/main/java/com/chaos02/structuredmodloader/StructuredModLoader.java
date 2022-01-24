package com.chaos02.structuredmodloader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//read toml file yourself lazy bitch
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

@Mod("structuredmodloader")
public class StructuredModLoader implements IModLocator {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	// Mod paths
	private List<Path> mods;
	final static File CONFIGFILE = new File("config/StructuredModLoader.json");
	final static List<String> DEFAULTDIRS = Arrays.asList("ignore", "unstable", "disable", "");
	final static int DEFAULTDEPTH = 3;
	final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	SMLConfig configRW = null;
	
	public StructuredModLoader() {
		mods = new ArrayList<>();
	}
	
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		
	}
	
	public void configProvider() {
		LOGGER.info("StructuredModLoader installed!");
		LOGGER.info("Loading Config now!");
		
		// ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
		// SMLConfig.SML_SPEC, "StructuredModLoader.toml");
		
		/*
			test if config exist && valid
				
			else
				writeConfig();
		 
		 */
		
		/*
		String[] ignorePath = new String[SMLConfig.ignoreDir.get().size()];
		for (int i = 0; i < SMLConfig.ignoreDir.get().size(); i++)
			ignorePath[i] = SMLConfig.ignoreDir.get().get(i);
		LOGGER.info("Ignoring >" + String.join(",", ignorePath) + "< keyworsds	"); */
		LOGGER.info("Loading subfolders that are NOT in config!");
		LOGGER.info("Successfully recursively loaded {}", String.join(", ", mods.toArray().toString()));
		
	}
	
	public SMLConfig readConfig() {
		SMLConfig configRead = null;
		try (Reader configReader = new FileReader(CONFIGFILE)) {
			configRead = GSON.fromJson(configReader, SMLConfig.class);
		} catch (IOException e) {
			LOGGER.error("FAILED TO READ CONFIG; RETURNING NULL");
			e.printStackTrace();
		}
		return configRead;
	}
	
	public void writeConfig() {
		writeConfig(SMLConfig.class);
	}
	
	public void writeConfig(Class ConfigClass) { // fertig
		@SuppressWarnings("deprecation")
		Object obj;
		try {
			obj = ConfigClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOGGER.info("Assuming default config");
			obj = SMLConfig.class;
		}
		
		LOGGER.info("Writing CONFIGFILE to {}", CONFIGFILE.toString());
		configRW = createConfObj();
		try (FileWriter configWriter = new FileWriter(CONFIGFILE)) {
			GSON.toJson(configRW, configWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static SMLConfig createConfObj() { // fertig
		SMLConfig configWrite = new SMLConfig();
		configWrite.setDirs(DEFAULTDIRS);
		configWrite.setDepth(DEFAULTDEPTH);
		return configWrite;
	}
	
	// uses
	
	private static boolean isJsonValid(final JsonReader jsonReader) throws IOException {
		try {
			JsonToken token;
			loop: while ((token = jsonReader.peek()) != JsonToken.END_DOCUMENT && token != null) {
				switch (token) {
					case BEGIN_ARRAY:
						jsonReader.beginArray();
						break;
					case END_ARRAY:
						jsonReader.endArray();
						break;
					case BEGIN_OBJECT:
						jsonReader.beginObject();
						break;
					case END_OBJECT:
						jsonReader.endObject();
						break;
					case NAME:
						jsonReader.nextName();
						break;
					case STRING:
					case NUMBER:
					case BOOLEAN:
					case NULL:
						jsonReader.skipValue();
						break;
					case END_DOCUMENT:
						break loop;
					default:
						throw new AssertionError(token);
				}
			}
			return true;
		} catch (final MalformedJsonException ignored) {
			return false;
		}
	}
	
	// Implements
	
	public Stream<Path> scanCandidates() {
		LOGGER.debug("SML.scanCandidates()");
		return new ArrayList<Path>().stream();
	}
	
	@Override
	public List<IModFile> scanMods() {
		LOGGER.debug("SML.scanMods()");
		configProvider();
		return new ArrayList<IModFile>();
	}
	
	@Override
	public String name() {
		return "[SML]StructuredModLoader";
	}
	
	@Override
	public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
		// TODO Auto-generated method stub
		LOGGER.debug("SML.scanFile()");
	}
	
	@Override
	public void initArguments(Map<String, ?> arguments) {
		// TODO Auto-generated method stub
		LOGGER.debug("SML.initArguments() {}", arguments);
	}
	
	@Override
	public boolean isValid(IModFile modFile) {
		// TODO Auto-generated method stub
		LOGGER.debug("SML.isValid()");
		return true;
	}
	
}