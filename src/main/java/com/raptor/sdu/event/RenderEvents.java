package com.raptor.sdu.event;

import static com.raptor.sdu.type.Mods.DRAWERS_BLOCKS;
import static com.raptor.sdu.type.Mods.TRIM_ITEMS;

import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.resources.ModelRegistry;
import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.block.BlockUnlimitedDrawers;
import com.raptor.sdu.block.model.UnlimitedDrawerModel;
import com.raptor.sdu.item.ItemUnlimitedTrim;
import com.raptor.sdu.render.tileentity.TileEntityDrawersUnlimitedRenderer;
import com.raptor.sdu.tileentity.TileEntityDrawersUnlimited;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEvents {

	@SubscribeEvent
	public void onModelRegistryEvent(ModelRegistryEvent event) {
		for(ItemUnlimitedTrim trim : TRIM_ITEMS) {
			registerItem(trim);
		}
		
		ModelRegistry modelRegistry = Chameleon.instance.modelRegistry;
		for(BlockUnlimitedDrawers drawers : DRAWERS_BLOCKS) {
			//registerBlock(drawers);
			drawers.initDynamic();
			modelRegistry.registerModel(new UnlimitedDrawerModel.Register(drawers));
		}
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDrawersUnlimited.class, new TileEntityDrawersUnlimitedRenderer());
	}
	
	public static void registerItem(Item item) {
		registerItem(item, 0);
	}
	
	public static void registerItem(Item item, int meta) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	public static void registerBlock(BlockUnlimitedDrawers block) {
		ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(SDUnlimited.MODID + ":basicdrawers_" + block.mod + "_" + block.material, "block=" + state.getValue(BlockUnlimitedDrawers.BLOCK) + ",facing=" + state.getValue(BlockUnlimitedDrawers.FACING));
			}
		});
	}
	
}
