package com.raptor.sdu.event;

import static com.raptor.sdu.type.Mods.BLOCKS;
import static com.raptor.sdu.type.Mods.ENABLED_MODS;
import static com.raptor.sdu.type.Mods.ITEMS;
import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.config.ConfigManager;
import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.block.BlockUnlimitedDrawers;
import com.raptor.sdu.block.BlockUnlimitedTrim;
import com.raptor.sdu.item.ItemUnlimitedDrawers;
import com.raptor.sdu.item.ItemUnlimitedTrim;
import com.raptor.sdu.type.DrawerMaterial;
import com.raptor.sdu.type.DrawerMaterial.AbstractItemStack;
import com.raptor.sdu.type.Mod;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEvents {

	@SubscribeEvent
	public void onBlockRegistryEvent(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		ConfigManager config = StorageDrawers.config;
		boolean trimEnabled = config.isBlockEnabled("trim");
		boolean anyDrawerEnabled = false;
		for(EnumBasicDrawer type : EnumBasicDrawer.values()) {
			if(config.isBlockEnabled(type.getUnlocalizedName())) {
				anyDrawerEnabled = true;
				break;
			}
		}
		for(Block block : BLOCKS) {
			boolean isDrawer = block instanceof BlockUnlimitedDrawers;
			boolean isTrim = block instanceof BlockUnlimitedTrim;
			if((!isDrawer || anyDrawerEnabled)
					&& (!isTrim || trimEnabled)) {
				registry.register(block);
				if(isDrawer)
					OreDictionary.registerOre("drawerBasic", new ItemStack(block, 1, WILDCARD_VALUE));
				else if(isTrim)
					OreDictionary.registerOre("drawerTrim", new ItemStack(block, 1, WILDCARD_VALUE));
			}
		}
	}

	@SubscribeEvent
	public void onItemRegistryEvent(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		ConfigManager config = StorageDrawers.config;
		boolean trimEnabled = config.isBlockEnabled("trim");
		boolean anyDrawerEnabled = false;
		for(EnumBasicDrawer type : EnumBasicDrawer.values()) {
			if(config.isBlockEnabled(type.getUnlocalizedName())) {
				anyDrawerEnabled = true;
				break;
			}
		}
		for(Item item : ITEMS) {
			if((!(item instanceof ItemUnlimitedDrawers) || anyDrawerEnabled)
					&& (!(item instanceof ItemUnlimitedTrim) || trimEnabled)) {
				registry.register(item);
			}
		}
	}

	@SubscribeEvent
	public void onCraftingRegistryEvent(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> registry = event.getRegistry();
		for(Mod mod : ENABLED_MODS) {
			for(DrawerMaterial material : mod) {
				int count = 0;
				List<ItemStack> planks = new ArrayList<>(material.planks.length);
				for(AbstractItemStack plankAbstract : material.planks) {
					ItemStack plank = plankAbstract.toItemStack(mod);
					if(plank.isEmpty()) {
						SDUnlimited.logger.warn("One of the planks for drawer material " + material.getName() + " is missing: " + plankAbstract);
					} else {
						registerPlankRecipes(registry, material.getDrawersBlock(), plank, count++);
						planks.add(plank);
					}
				}
				count = 0;
				for(AbstractItemStack slabAbstract : material.slabs) {
					ItemStack slab = slabAbstract.toItemStack(mod);
					if(slab.isEmpty()) {
						SDUnlimited.logger.warn("One of the slabs for drawer material " + material.getName() + " is missing: " + slabAbstract);
					} else {
						registerSlabRecipes(registry, material.getDrawersBlock(), slab, count++);
					}
				}
				if(StorageDrawers.config.isBlockEnabled("trim")) {
					count = 0;
					for(ItemStack plank : planks) {
						registerTrimRecipe(registry, material.getTrimBlock(), plank, count++);
					}
				}
			}
		}
		
	}

	@Nonnull
	public static ItemStack makeBasicDrawerItemStack(EnumBasicDrawer info, BlockUnlimitedDrawers block, int count) {
		return new ItemStack(block, count, info.getMetadata());
	}
	
	private static final ResourceLocation EMPTY_GROUP = new ResourceLocation("", "");
	
	public static void registerPlankRecipes(IForgeRegistry<IRecipe> registry, BlockUnlimitedDrawers block, ItemStack plankStack, int count) {
		ConfigManager config = StorageDrawers.config;
		
		if(config.isBlockEnabled(EnumBasicDrawer.FULL1.getUnlocalizedName()) && !plankStack.isEmpty()) {
			ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL1, block,
					config.getBlockRecipeOutput(EnumBasicDrawer.FULL1.getUnlocalizedName()));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "xxx", " y ", "xxx", 'x', plankStack, 'y', "chestWood")
							.setRegistryName(result.getItem().getRegistryName() + "_"
									+ EnumBasicDrawer.FULL1.getUnlocalizedName() + "_" + block.mod + "_" + block.material + "." + count));
		}
		if(config.isBlockEnabled(EnumBasicDrawer.FULL2.getUnlocalizedName()) && !plankStack.isEmpty()) {
			ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL2, block,
					config.getBlockRecipeOutput(EnumBasicDrawer.FULL2.getUnlocalizedName()));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "xxx", "xyx", 'x', plankStack, 'y', "chestWood")
							.setRegistryName(result.getItem().getRegistryName() + "_"
									+ EnumBasicDrawer.FULL2.getUnlocalizedName() + "_" + block.mod + "_" + block.material + "." + count));
		}
		if(config.isBlockEnabled(EnumBasicDrawer.FULL4.getUnlocalizedName()) && !plankStack.isEmpty()) {
			@Nonnull
			ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL4, block,
					config.getBlockRecipeOutput(EnumBasicDrawer.FULL4.getUnlocalizedName()));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "yxy", "xxx", "yxy", 'x', plankStack, 'y', "chestWood")
							.setRegistryName(result.getItem().getRegistryName() + "_"
									+ EnumBasicDrawer.FULL4.getUnlocalizedName() + "_" + block.mod + "_" + block.material + "." + count));
		}
	}
	
	public static void registerSlabRecipes(IForgeRegistry<IRecipe> registry, BlockUnlimitedDrawers block, ItemStack slabStack, int count) {
		ConfigManager config = StorageDrawers.config;
		
		if(config.isBlockEnabled(EnumBasicDrawer.HALF2.getUnlocalizedName()) && !slabStack.isEmpty()) {
			ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.HALF2, block,
					config.getBlockRecipeOutput(EnumBasicDrawer.HALF2.getUnlocalizedName()));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "xxx", "xyx", 'x', slabStack, 'y', "chestWood")
							.setRegistryName(result.getItem().getRegistryName() + "_"
									+ EnumBasicDrawer.HALF2.getUnlocalizedName() + "_" + block.mod + "_" + block.material + "." + count));
		}
		if(config.isBlockEnabled(EnumBasicDrawer.HALF4.getUnlocalizedName()) && !slabStack.isEmpty()) {
			ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.HALF4, block,
					config.getBlockRecipeOutput(EnumBasicDrawer.HALF4.getUnlocalizedName()));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "yxy", "xxx", "yxy", 'x', slabStack, 'y', "chestWood")
							.setRegistryName(result.getItem().getRegistryName() + "_"
									+ EnumBasicDrawer.HALF4.getUnlocalizedName() + "_" + block.mod + "_" + block.material + "." + count));
		}
	}
	
	public static void registerTrimRecipe(IForgeRegistry<IRecipe> registry, BlockUnlimitedTrim trim, ItemStack plankStack, int count) {
		ConfigManager config = StorageDrawers.config;
		
		if(!plankStack.isEmpty()) {
			ItemStack result = new ItemStack(trim, config.getBlockRecipeOutput("trim"));
			registry.register(
					new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "yyy", "xyx", 'x', "stickWood", 'y', plankStack)
							.setRegistryName(result.getItem().getRegistryName() + "." + count));
		}
	}

}
