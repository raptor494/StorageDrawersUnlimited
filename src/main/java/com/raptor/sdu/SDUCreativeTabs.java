package com.raptor.sdu;

import static com.raptor.sdu.type.Mods.ENABLED_MODS;

import com.jaquadro.minecraft.storagedrawers.core.ModItemGroup;
import com.raptor.sdu.type.DrawerMaterial;
import com.raptor.sdu.type.SupportedMod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SDUCreativeTabs {

	public static final ItemGroup TAB = new ItemGroup(SDUnlimited.MODID) {	
		{
			setNoTitle();
			setBackgroundImageName("item_search.png");
		}
		
		@Override
        @OnlyIn(Dist.CLIENT)
		public ItemStack createIcon() {
			for(SupportedMod mod : ENABLED_MODS) {
				DrawerMaterial material = mod.getDefaultMaterial();
				if(material != null)
					return new ItemStack(material.item_drawers_full_1, 1);
					//return new ItemStack(material.getDrawerItem(), 1, EnumBasicDrawer.FULL1.getMetadata());
			}
			return ModItemGroup.STORAGE_DRAWERS.getIcon();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void fill(NonNullList<ItemStack> items) {
			for(SupportedMod mod : ENABLED_MODS) {
				for(Item item : mod.getItemsIterable()) {
					item.fillItemGroup(this, items);
				}
			}
		}
		
		@Override
		public boolean hasSearchBar() {
			return true;
		}
		
	};
	
}
