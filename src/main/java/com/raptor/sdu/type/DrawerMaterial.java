package com.raptor.sdu.type;

import static com.raptor.sdu.SDUConfig.isValidModID;

import java.util.stream.Stream;

import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockStandardDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockTrim;
import com.jaquadro.minecraft.storagedrawers.item.ItemDrawers;
import com.raptor.sdu.SDUCreativeTabs;
import com.raptor.sdu.StorageDrawersUnlimited;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class DrawerMaterial {
	private final String name;
	private final Material material;
	private final SoundType soundType;
	private final int lightLevel;
	public BlockDrawers block_drawers_full_1, block_drawers_full_2, block_drawers_full_4;
	public BlockDrawers block_drawers_half_1, block_drawers_half_2, block_drawers_half_4;
	public BlockTrim    block_trim;
	public ItemDrawers item_drawers_full_1, item_drawers_full_2, item_drawers_full_4;
	public ItemDrawers item_drawers_half_1, item_drawers_half_2, item_drawers_half_4;
	public BlockItem   item_trim;
	
	private DrawerMaterial(String name, Material material, SoundType soundType, int lightLevel) {
		this.name = name;
		this.material = material;
		this.soundType = soundType;
		this.lightLevel = lightLevel;
	}
	
	private boolean linked = false;
	
	void link(SupportedMod mod) {
		if(linked)
			return;
		linked = true;
		
		Block.Properties blockProperties = Block.Properties.create(material)
				.sound(soundType)
				.hardnessAndResistance(5.0f)
				.lightValue(lightLevel);

		block_drawers_full_1 = new BlockStandardDrawers(1, false, blockProperties);
		block_drawers_full_2 = new BlockStandardDrawers(2, false, blockProperties);
		block_drawers_full_4 = new BlockStandardDrawers(4, false, blockProperties);
		block_drawers_half_1 = new BlockStandardDrawers(1, true,  blockProperties);
		block_drawers_half_2 = new BlockStandardDrawers(2, true,  blockProperties);
		block_drawers_half_4 = new BlockStandardDrawers(4, true,  blockProperties);
		
		block_trim = new BlockTrim(blockProperties);
		
		block_drawers_full_1.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_' + name + "_full_drawers_1");
		block_drawers_full_2.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_full_drawers_2");
		block_drawers_full_4.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_full_drawers_4");
		block_drawers_half_1.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_half_drawers_1");
		block_drawers_half_2.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_half_drawers_2");
		block_drawers_half_4.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_half_drawers_4");
		
		block_trim.setRegistryName(StorageDrawersUnlimited.MODID, mod.getModID() + '_'+ name + "_trim");
		
		Item.Properties itemProperties = new Item.Properties()
							.group(SDUCreativeTabs.TAB);
		
		item_drawers_full_1 = new ItemDrawers(block_drawers_full_1, itemProperties);
		item_drawers_full_2 = new ItemDrawers(block_drawers_full_2, itemProperties);
		item_drawers_full_4 = new ItemDrawers(block_drawers_full_4, itemProperties);
		item_drawers_half_1 = new ItemDrawers(block_drawers_half_1, itemProperties);
		item_drawers_half_2 = new ItemDrawers(block_drawers_half_2, itemProperties);
		item_drawers_half_4 = new ItemDrawers(block_drawers_half_4, itemProperties);
		
		item_trim = new BlockItem(block_trim, itemProperties);
		
		item_drawers_full_1.setRegistryName(block_drawers_full_1.getRegistryName());
		item_drawers_full_2.setRegistryName(block_drawers_full_2.getRegistryName());
		item_drawers_full_4.setRegistryName(block_drawers_full_4.getRegistryName());
		item_drawers_half_1.setRegistryName(block_drawers_half_1.getRegistryName());
		item_drawers_half_2.setRegistryName(block_drawers_half_2.getRegistryName());
		item_drawers_half_4.setRegistryName(block_drawers_half_4.getRegistryName());
		
		item_trim.setRegistryName(block_trim.getRegistryName());
	}
	
	public String getName() {
		return name;
	}
	
	public Stream<Block> blocks() {
		return Stream.of(block_drawers_full_1, block_drawers_full_2, block_drawers_full_4, block_drawers_half_1, block_drawers_half_2, block_drawers_half_4, block_trim);
	}
	
	public Stream<Item> items() {
		return Stream.of(item_drawers_full_1, item_drawers_full_2, item_drawers_full_4, item_drawers_half_1, item_drawers_half_2, item_drawers_half_4, item_trim);
	}
	
	public Stream<BlockDrawers> drawers() {
		return Stream.of(block_drawers_full_1, block_drawers_full_2, block_drawers_full_4, block_drawers_half_1, block_drawers_half_2, block_drawers_half_4);
	}
	
	public static interface Creator {
		DrawerMaterial build();
	}
	
	static class MemoizedCreator implements Creator {
		private final Creator creator;
		private DrawerMaterial built;
		
		MemoizedCreator(Creator creator) {
			this.creator = creator;
		}
		
		@Override
		public DrawerMaterial build() {
			return built == null? built = creator.build() : built;
		}
		
//		public String getName() {
//			return creator.getName();
//		}
		
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements Creator {
		private String name;
		private Material material = Material.WOOD;
		private SoundType soundType = SoundType.WOOD;
		private int lightLevel = 0;
		
		public Builder name(String name) {
			if(built != null) {
				throw new IllegalStateException("The mod has already been built");
			}
			if(!isValidModID(name)) {
				throw new IllegalArgumentException("'" + name + "' is not a valid material name");
			}
			this.name = name;
			return this;
		}
		
		public Builder soundType(SoundType soundType) {
			this.soundType = soundType;
			return this;
		}
		
		public Builder material(Material material) {
			this.material = material;
			return this;
		}
		
		public Builder lightLevel(int lightLevel) {
			this.lightLevel = lightLevel;
			return this;
		}
		
//		public String getName() {
//			return name;
//		}
		
		private DrawerMaterial built;
		
		@Override
		public DrawerMaterial build() {
			if(built != null) {
				return built;
			}
			
			if(name == null) {
				throw new IllegalStateException("No name given");
			}
			
			return built = new DrawerMaterial(name, material, soundType, lightLevel);
		}
		
	}
	
}
