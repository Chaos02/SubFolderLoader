package com.chaos02.structuredmodloader.SMLMod;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LogMarkers;

@Mod("SMLMod")
public class SMLMod {
	
	private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
	
	public SMLMod() {
		/* This doesnt work because of java.lang.NoClassDefFoundError: net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext */
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
		LOGGER.info(LogMarkers.SPLASH,
				"A HUGE THANK YOU! to stiebi99#2124 again because literally without him this mod would never exist!");
	}

}
