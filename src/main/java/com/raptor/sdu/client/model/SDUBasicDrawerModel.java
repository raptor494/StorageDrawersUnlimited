package com.raptor.sdu.client.model;

import static com.jaquadro.minecraft.storagedrawers.client.model.BasicDrawerModel.Register.replaceBlock;
import static com.raptor.sdu.StorageDrawersUnlimited.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.raptor.sdu.StorageDrawersUnlimited;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class SDUBasicDrawerModel {

	@EventBusSubscriber(modid = StorageDrawersUnlimited.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	public static class Register {
		
		@SubscribeEvent
		public static void registerTextures(TextureStitchEvent.Pre event) {
			if(event.getMap() != Minecraft.getInstance().getTextureMap())
				return;
			
			BlockModel unbakedModel = getBlockModel(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/full_drawers_lock.json"));

            for (String x : unbakedModel.textures.values()) {
                event.addSprite(new ResourceLocation(x));
            }
            
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_icon_area_1.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_count_area_1.json"),
                DRAWERS_FULL_1);
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_icon_area_2.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_count_area_2.json"),
                DRAWERS_FULL_2);
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_icon_area_4.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/full_drawers_count_area_4.json"),
                DRAWERS_FULL_4);
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_icon_area_1.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_count_area_1.json"),
                DRAWERS_HALF_1);
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_icon_area_2.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_count_area_2.json"),
                DRAWERS_HALF_2);
            populateGeometryData(new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_icon_area_4.json"),
                new ResourceLocation(StorageDrawers.MOD_ID, "models/block/geometry/half_drawers_count_area_4.json"),
                DRAWERS_HALF_4);
		}
		
		private static BlockModel getBlockModel(ResourceLocation location) {
			IResource iresource = null;
			Reader reader = null;
			try {
				iresource = Minecraft.getInstance().getResourceManager().getResource(location);
				reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
				return BlockModel.deserialize(reader);
			}
			catch(IOException e) {
				return null;
			}
			finally {
				IOUtils.closeQuietly(reader);
				IOUtils.closeQuietly(iresource);
			}
		}

		private static void populateGeometryData(ResourceLocation locationIcon, ResourceLocation locationCount, Iterable<BlockDrawers> blocks) {
			BlockModel slotInfo = getBlockModel(locationIcon);
			BlockModel countInfo = getBlockModel(locationCount);
			for(BlockDrawers block : blocks) {
				if(block == null)
					continue;

				for(int i = 0; i < block.getDrawerCount(); i++) {
					Vector3f from = slotInfo.getElements().get(i).positionFrom;
					Vector3f to = slotInfo.getElements().get(i).positionTo;
					block.labelGeometry[i] = new AxisAlignedBB(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
				}
				for(int i = 0; i < block.getDrawerCount(); i++) {
					Vector3f from = countInfo.getElements().get(i).positionFrom;
					Vector3f to = countInfo.getElements().get(i).positionTo;
					block.countGeometry[i] = new AxisAlignedBB(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
				}
			}
		}
		
		@SubscribeEvent
        public static void registerModels(ModelBakeEvent event) {
			for(BlockDrawers block : DRAWERS) {
				replaceBlock(event, block);
			}
		}
	}
	
}
