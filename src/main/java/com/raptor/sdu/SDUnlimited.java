package com.raptor.sdu;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.raptor.sdu.proxy.ClientProxy;
import com.raptor.sdu.proxy.CommonProxy;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(SDUnlimited.MODID)
public class SDUnlimited {
	public static final String MODID = "storagedrawersunlimited";
	
	public static final CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	public static final Logger logger = LogManager.getLogger(MODID);
	
	private static File configFolder;
	public static File getConfigFolder() {
		if(configFolder == null) {
			configFolder = FMLPaths.CONFIGDIR.get().resolve("storagedrawersunlimited").toFile();
		}
		return configFolder;
	}
	
	public SDUnlimited() {		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SDUConfig.spec);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	@SubscribeEvent
	public void setup(FMLCommonSetupEvent event) {
		if(!getConfigFolder().exists() || !configFolder.isDirectory())
			configFolder.mkdirs();
		proxy.setup(event);
	}
	
}
