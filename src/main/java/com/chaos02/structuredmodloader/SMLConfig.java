package com.chaos02.structuredmodloader;

import java.util.Arrays;
import java.util.List;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import org.apache.logging.log4j.LogManager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
//import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;


public class SMLConfig {
	
	public static final ForgeConfigSpec GENERAL_SPEC;
	
	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();
	}
	
	public static ConfigValue<List<? extends String>> ignoreDir;
	
	private static void setupConfig(ForgeConfigSpec.Builder builder) {
		
        builder.comment("Server configuration settings")
               .push("server");
        
        ignoreDir = builder
        		.comment("Define keywords in directory names to be skipped on mod load:")
        		.translation("forge.configgui.ignoreDir")
        		.defineList("ignored_dir", Arrays.asList("ignore","unstable","disable"), entry -> true);

        builder.pop();
    }
	
	public SMLConfig(final FMLCommonSetupEvent evt) {
		ModLoadingContext.get().registerConfig(Type.COMMON, GENERAL_SPEC, "StructuredModLoader.toml");
	}

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        LogManager.getLogger().debug("SML", "Loaded SML config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        LogManager.getLogger().debug("SML", "Forge config just got changed on the file system!");
    }

}
