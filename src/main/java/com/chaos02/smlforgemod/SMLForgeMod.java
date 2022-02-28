package com.chaos02.smlforgemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LogMarkers;

@Mod("smlforgemod")
public class SMLForgeMod {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public SMLForgeMod() {
		LOGGER.debug(LogMarkers.LOADING, "Initializing SML Forge mod");
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		// FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		// FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		
		// Register ourselves for server and other game events we are interested
		// in
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		LOGGER.info(LogMarkers.LOADING, "Hello world from [SML]!!!");
		LOGGER.info(LogMarkers.SPLASH, "A HUGE THANK YOU! to stiebi99#2124 again because literally without him SML would never exist!");
	}
	
}
