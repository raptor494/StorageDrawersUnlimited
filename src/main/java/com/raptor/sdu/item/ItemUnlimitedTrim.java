package com.raptor.sdu.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.raptor.sdu.block.BlockUnlimitedTrim;
import com.raptor.sdu.type.DrawerMaterial;
import com.raptor.sdu.type.Mod;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUnlimitedTrim extends ItemBlock {
	public final Mod mod;
	public final DrawerMaterial material;
	
	public ItemUnlimitedTrim(BlockUnlimitedTrim block) {
		super(block);

		setRegistryName(block.getRegistryName());
		
		this.mod = block.mod;
		this.material = block.material;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format("storagedrawers.trim", I18n.format("storagedrawers.material." + this.mod + '.' + this.material), super.getItemStackDisplayName(stack));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack itemStack, @Nullable World world, List<String> list, ITooltipFlag advanced) {
		if(advanced.isAdvanced())
			list.add(I18n.format("storagedrawers.mod", mod.getModName()));
	}
}
