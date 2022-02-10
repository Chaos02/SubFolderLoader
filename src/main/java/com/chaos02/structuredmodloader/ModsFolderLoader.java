package com.chaos02.structuredmodloader;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;

public class ModsFolderLoader extends ModsFolderLocator {
	
	private static final String SUFFIX = ".jar";
	private static final Logger LOGGER = LogManager.getLogger();
	private final Path modFolder;
	private final String customName;
	
	public ModsFolderLoader() {
		this(FMLPaths.MODSDIR.get());
	}
	
	ModsFolderLoader(Path modFolder) {
		this(modFolder, "mods folder");
	}
	
	ModsFolderLoader(Path modFolder, String name) {
		this.modFolder = modFolder;
		this.customName = name;
	}
	
	@Override
	public Stream<Path> scanCandidates() {
		LOGGER.info(LogMarkers.SCAN, "Disabling mods scan in \"{}\"", this.modFolder);
		
		return null;
	}
	
}
