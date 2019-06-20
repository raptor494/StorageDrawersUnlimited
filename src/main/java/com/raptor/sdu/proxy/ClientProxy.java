package com.raptor.sdu.proxy;

import com.raptor.sdu.event.RenderEvents;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		
		MinecraftForge.EVENT_BUS.register(new RenderEvents());
	}
	
}
