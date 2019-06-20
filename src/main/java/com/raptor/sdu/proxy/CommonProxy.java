package com.raptor.sdu.proxy;

import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.event.RegistryEvents;
import com.raptor.sdu.tileentity.SDUTileEntities;
import com.raptor.sdu.tileentity.TileEntityDrawersUnlimited;
import com.raptor.sdu.type.Mods;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		Mods.init();
		
		GameRegistry.registerTileEntity(TileEntityDrawersUnlimited.Slot1.class, new ResourceLocation(SDUnlimited.MODID, "basicdrawers.1"));
        GameRegistry.registerTileEntity(TileEntityDrawersUnlimited.Slot2.class, new ResourceLocation(SDUnlimited.MODID, "basicdrawers.2"));
        GameRegistry.registerTileEntity(TileEntityDrawersUnlimited.Slot4.class, new ResourceLocation(SDUnlimited.MODID, "basicdrawers.4"));
		
		MinecraftForge.EVENT_BUS.register(new RegistryEvents());
	}
	
	public void init(FMLInitializationEvent event) {
		SDUTileEntities.init();
	}
	
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public void serverInit(FMLServerStartingEvent event) {
		
	}
	
	
}
