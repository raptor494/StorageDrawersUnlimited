package com.raptor.sdu.block;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.INetworked;
import com.raptor.sdu.SDUCreativeTabs;
import com.raptor.sdu.type.DrawerMaterial;
import com.raptor.sdu.type.Mod;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockUnlimitedTrim extends Block implements INetworked {

	public final Mod mod;
	public final DrawerMaterial material;
	private boolean isGrass = false;
	
	public BlockUnlimitedTrim(DrawerMaterial material) {
		super(Material.WOOD);

		this.mod = material.getMod();
		this.material = material;

		setTranslationKey(StorageDrawers.MOD_ID + ".trim");
		setRegistryName("trim_" + mod + "_" + material);
		setHardness(5f);
		setSoundType(SoundType.WOOD);
		setCreativeTab(SDUCreativeTabs.TAB);
	}
	
	public BlockUnlimitedTrim setMadeOfGrass() {
		setSoundType(SoundType.PLANT);
		setHardness(3F);
		isGrass = true;
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public Material getMaterial(IBlockState state) {
		return isGrass? Material.GRASS : super.getMaterial(state);
	}

}
